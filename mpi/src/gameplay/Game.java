package gameplay;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import message.Message;
import message.MessageHandler;
import message.MessageListener;
import message.MessageProducer;
import mpi.*;


public class Game extends JFrame implements MessageProducer {
	public Cell board[][] ; 
	private MessageHandler messageHandler;    // delegate to handle messages
	public int boardSize;
	
	public Game(int x, int y) {
		boardSize = x;
		board = new Cell[x][y];
		this.messageHandler = new MessageHandler();
		this.initBoard();
		this.initUI();
	}
	
	public void initBoard() {
	    Random rnd = new Random();
	    rnd.setSeed(System.currentTimeMillis());
	    for (int i = 0; i < 1000; i++)
	    {
	        for (int j = 0; j < 1000; j++)
	        {
	        		board[i][j] = new Cell();
	        		board[i][j].setX(i);
	            board[i][j].setY(j);
	            board[i][j].newState(rnd.nextInt(2)) ;
	            board[i][j].savePrevious();
	        }
	    }
	}
	
	
	protected void initUI() {

		JFrame f = new JFrame("The Game of life");
		final Surface surface = new Surface(this);
		addMessageListener(surface);
		
		f.add(surface);		
		f.setSize(1000, 1000);
		f.setLocationRelativeTo(null);
		f.setVisible(true);		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void start(String []args) {
		
		MPI.Init(args);
		int size, rank, sliceNum;
		rank = MPI.COMM_WORLD.getRank() ;
		size = MPI.COMM_WORLD.getSize() ;
		if(rank == 0) {
			
			//slice into rectangle
			sliceNum = boardSize / size;
			
			int info[] = new int [2];
			info[0]=boardSize; info[1]=sliceNum;
			for (int dest=0; dest<size; dest++) 
				MPI.COMM_WORLD.send(info, 2, MPI.INT, dest, 1); //send info
			int slice[][] = new int [sliceNum][boardSize];	
			for (int z=0; z<size; z++)
			{
				for (int k=0; k<sliceNum; k++) 
					for (int l=0; l<boardSize; l++) 
						slice[k][l]=board[k+(z*sliceNum)][l].getPrevious();	//cut a slice from the the board
				MPI.COMM_WORLD.send(slice, boardSize*sliceNum, MPI.INT, z, 1);	//and send it
			}
		}
		
		int localinfo[] = new int[2];
		MPI.COMM_WORLD.recv(localinfo, 2, MPI.INT, 0, 1);	//receive info
		int myslice[][] = new int[localinfo[0]][localinfo[1]]; //my own slice of the board
		MPI.COMM_WORLD.recv(myslice, localinfo[0]*localinfo[1], MPI.INT, 0, 1);	//receive slice
		boardSize = localinfo[0];
		sliceNum = localinfo[1];
		
		int todown[] = new int[boardSize];	
		int toup[] = new int[boardSize];
		int fromdown[] = new int[boardSize];
		int fromup[] =new int[boardSize]; //arrays to send and to receive
		
		while(true)
		{
			if (rank!=size-1) // all except for last send down
			{
				for (int j=0; j<boardSize; j++) 
					todown[j]=myslice[sliceNum-1][j];
				MPI.COMM_WORLD.send(todown, boardSize, MPI.INT, rank+1, 1);

			} else {
				for (int k=0; k<boardSize; k++) 
					fromdown[k]=0; 
			} // last one generates empty stripe "from down"

			if (rank!=0) // all except for first receive from up
			{
				MPI.COMM_WORLD.recv(fromup, boardSize, MPI.INT, rank-1, 1);	

			} else { 
				for (int k=0; k<boardSize; k++) 
					fromup[k]=0;
			} // first one generats empty line "from up"	
		
			if (rank!=0) // all except for first send up
			{
				for (int j=0; j<boardSize; j++) 
					toup[j]=myslice[0][j];
				MPI.COMM_WORLD.send(toup, boardSize, MPI.INT, rank-1, 1);
			}
		
			if (rank!=size-1) // all except for last receive from down
			{
				MPI.COMM_WORLD.recv(fromdown, boardSize, MPI.INT, rank+1, 1);
			}
			
			int sum=0; // sum of neighbours
			int mynewslice[][] =  new int[sliceNum][boardSize];
			for (int x=0; x<sliceNum; x++) //for each row
			{	
				for (int y=0; y<boardSize; y++) //for each column
				{
					if (x==0 && y==0) //upper-left cell
						sum = myslice[x+1][y]+myslice[x+1][y+1]+myslice[0][y+1]+fromup[0]+fromup[1];
					else if (x==0 && y==boardSize-1) //upper-right cell
						sum = myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y]+fromup[boardSize-1]+fromup[boardSize-2];
					else if (x==sliceNum-1 && y==0) //lower-left cell
						sum = myslice[x][y+1]+myslice[x-1][y+1]+myslice[x-1][y]+fromdown[0]+fromdown[1];
					else if (x==sliceNum-1 && y==boardSize-1) //lower-right cell
						sum = myslice[x-1][y]+myslice[x-1][y-1]+myslice[x][y-1]+fromdown[boardSize-1]+fromdown[boardSize-2];
					else // not corner cells    
					{
						if (y==0) // leftmost line, not corner
							sum=myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x+1][y+1]+myslice[x+1][y];
						else if (y==boardSize-1) //rightmost line, not corner
							sum=myslice[x-1][y]+myslice[x-1][y-1]+myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y];
						else if (x==0) //uppermost line, not corner
							sum=myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y]+myslice[x+1][y+1]+myslice[x][y+1]+fromup[y-1]+fromup[y]+fromup[y+1];
						else if (x==sliceNum-1) //lowermost line, not corner
							sum=myslice[x-1][y-1]+myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x][y-1]+fromdown[y-1]+fromdown[y]+fromdown[y+1];
						else //general case, any cell within
							sum=myslice[x-1][y-1]+myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x+1][y+1]+myslice[x+1][y]+myslice[x+1][y-1]+myslice[x][y-1];
					}
					
					//PUT THE NEW VALUE OF A CELL
					if (myslice[x][y]==1 && (sum==2 || sum==3)) mynewslice[x][y]=1;
					else if (myslice[x][y]==1 && sum>3) mynewslice[x][y]=0;
					else if (myslice[x][y]==1 && sum<1) mynewslice[x][y]=0;
					else if (myslice[x][y]==0 && sum==3) mynewslice[x][y]=1;
			 		else mynewslice[x][y]=0;
				
				}
			}
			
			for (int x=0; x<sliceNum; x++)
				for (int y=0; y<boardSize; y++)
					myslice[x][y]=mynewslice[x][y];
			
			//combine all slice together
			if (rank==0) 
			{
				int aBoard[][] = new int[sliceNum][boardSize];
				
				for (int x=0; x<sliceNum; x++) //put your own slice
				{
					for (int y=0; y<boardSize; y++) {
						board[x][y].newState(myslice[x][y]);
					}
					
				}
				for (int i=1; i<size; i++)
				{
					MPI.COMM_WORLD.recv(aBoard, boardSize*sliceNum, MPI.INT, i, 1); //receive all others'
					for (int x=0; x<sliceNum; x++)
					{
						for (int y=0; y<boardSize; y++) {
							board[x+i*sliceNum][y].newState(aBoard[x][y]);
						}
					}
				}
			}
			else {
				MPI.COMM_WORLD.send(myslice, boardSize*sliceNum, MPI.INT, 0,1);
			}
		
		    //Save the state as previous state
		    for (int x = 1; x < 1000 - 1; x++)
		    {
		        for (int y = 1; y < 1000 - 1; y++)
		        { 	
		        		board[x][y].savePrevious();
		        }
		    }
		    
		    sendMessage(new Message(1));
	    }
		MPI.Finalize();
	}
	
	@Override
	public void addMessageListener(MessageListener listener) {
		// TODO Auto-generated method stub
		messageHandler.addListener(listener);
	}

	@Override
	public void removeMessageListener(MessageListener listener) {
		// TODO Auto-generated method stub
		 messageHandler.removeListener(listener);
	}

	@Override
	public void sendMessage(Message message) {
		// TODO Auto-generated method stub
		messageHandler.sendMessage(message);
	}
	
	
    public static void main(String[] args)  {
		Game gm = new Game(1000,1000);
		gm.start(args);
    }
}
