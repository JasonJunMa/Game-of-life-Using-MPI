package gameplay;

import java.util.Random;
import javax.swing.JFrame;
import message.*;
import mpi.*;

public class Game extends JFrame implements MessageProducer {
	public Cell board[][];
	private MessageHandler messageHandler; // delegate to handle messages
	public int boardSize;

	public Game(int x) {
		boardSize = x;
	}

	public void init() {
		board = new Cell[boardSize][boardSize];
		this.messageHandler = new MessageHandler();
		this.initBoard();
		this.initUI();
	}

	public void initBoard() {
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				board[i][j] = new Cell();
				board[i][j].setX(i);
				board[i][j].setY(j);
				board[i][j].newState(rnd.nextInt(2));
				board[i][j].savePrevious();
			}
		}
	}

	protected void initUI() {
		JFrame f = new JFrame("The Game of life");
		final Surface surface = new Surface(this);
		addMessageListener(surface);
		f.add(surface);
		f.setSize(boardSize, boardSize);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void start(String[] args) throws MPIException {

		MPI.Init(args);
		int generations = 10000;
		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int sliceNum = boardSize / size;
		int tag = 10;
		int myslice[][] = new int[sliceNum][boardSize];

		if (rank == 0) {
			init();
			for (int k = 0; k < sliceNum; k++)
				for (int l = 0; l < boardSize; l++)
					myslice[k][l] = board[k][l].getPrevious();
			int slice[][] = new int[sliceNum][boardSize];
			Object[] sendObjectArray = new Object[1];
			for (int z = 1; z < size; z++) {
				for (int k = 0; k < sliceNum; k++)
					for (int l = 0; l < boardSize; l++)
						slice[k][l] = board[k + (z * sliceNum)][l].getPrevious(); // cut a slice from the the board
				sendObjectArray[0] = (Object) slice;
				MPI.COMM_WORLD.Send(sendObjectArray, 0, 1, MPI.OBJECT, z, tag); // and send it
			}
		} else {
			Object receivedArray[] = new Object[1];
			MPI.COMM_WORLD.Recv(receivedArray, 0, 1, MPI.OBJECT, 0, tag); // receive slice
			myslice = (int[][]) receivedArray[0];
		}

		int todown[] = new int[boardSize];
		int toup[] = new int[boardSize];
		int fromdown[] = new int[boardSize];
		int fromup[] = new int[boardSize]; // arrays to send and to receive
		int toDownRank, toUpRank;
		int fromDownRank, fromUpRank;
		int g = 0;

		while (g < generations) {
			toDownRank = (rank != size - 1) ? rank + 1 : 0;
			for (int j = 0; j < boardSize; j++)
				todown[j] = myslice[sliceNum - 1][j];
			MPI.COMM_WORLD.Isend(todown, 0, boardSize, MPI.INT, toDownRank, tag);

			fromUpRank = (rank != 0) ? rank - 1 : size - 1;
			MPI.COMM_WORLD.Irecv(fromup, 0, boardSize, MPI.INT, fromUpRank, tag);

			toUpRank = (rank != 0) ? rank - 1 : size - 1;
			for (int j = 0; j < boardSize; j++)
				toup[j] = myslice[0][j];
			MPI.COMM_WORLD.Isend(toup, 0, boardSize, MPI.INT, toUpRank, tag);

			fromDownRank = (rank != size - 1) ? rank + 1 : 0;
			MPI.COMM_WORLD.Irecv(fromdown, 0, boardSize, MPI.INT, fromDownRank, tag);
			int sum = 0; // sum of neighbours
			int mynewslice[][] = new int[sliceNum][boardSize];
			for (int x = 0; x < sliceNum; x++) // for each row
			{
				for (int y = 0; y < boardSize; y++) // for each column
				{
					if (x == 0 && y == 0) //upper-left cell
						sum = myslice[x + 1][y] + myslice[x + 1][y + 1] + myslice[0][y + 1] + myslice[0][boardSize - 1]
								+ myslice[1][boardSize - 1] + fromup[0] + fromup[1] + fromup[boardSize - 1];
					else if (x == 0 && y == boardSize - 1) //upper-right cell
						sum = myslice[x][y - 1] + myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][0]
								+ myslice[x][0] + fromup[boardSize - 1] + fromup[boardSize - 2] + fromup[0];
					else if (x == sliceNum - 1 && y == 0) //lower-left cell
						sum = myslice[x][y + 1] + myslice[x - 1][y + 1] + myslice[x - 1][y]
								+ myslice[x - 1][boardSize - 1] + myslice[x][boardSize - 1] + fromdown[boardSize - 1]
								+ fromdown[0] + fromdown[1];
					else if (x == sliceNum - 1 && y == boardSize - 1) //lower-right cell
						sum = myslice[x][0] + myslice[x - 1][0] + myslice[x - 1][y] + myslice[x - 1][y - 1]
								+ myslice[x][y - 1] + fromdown[boardSize - 2] + fromdown[boardSize - 1] + fromdown[0];
					else // not corner cells
					{
						if (y == 0) // leftmost line, not corner
							sum = myslice[x + 1][y] + myslice[x - 1][y + 1] + myslice[x - 1][y]
									+ myslice[x - 1][boardSize - 1] + myslice[x][boardSize - 1]
									+ myslice[x + 1][boardSize - 1] + myslice[x][y + 1] + myslice[x + 1][y + 1];
						else if (y == boardSize - 1) //rightmost line, not corner
							sum = myslice[x][0] + myslice[x - 1][0] + myslice[x - 1][y] + myslice[x - 1][y - 1]
									+ myslice[x][y - 1] + myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][0];
						else if (x == 0) //uppermost line, not corner
							sum = myslice[x][y + 1] + fromup[y + 1] + fromup[y] + fromup[y - 1] + myslice[x][y - 1]
									+ myslice[x + 1][y - 1] + myslice[x + 1][y] + myslice[x + 1][y + 1];
						else if (x == sliceNum - 1) //lowermost line, not corner
							sum = myslice[x][y + 1] + myslice[x - 1][y + 1] + myslice[x - 1][y] + myslice[x - 1][y - 1]
									+ myslice[x][y - 1] + fromdown[y - 1] + fromdown[y] + fromdown[y + 1];
						else //general case, any cell within
							sum = myslice[x - 1][y - 1] + myslice[x - 1][y] + myslice[x - 1][y + 1] + myslice[x][y + 1]
									+ myslice[x + 1][y + 1] + myslice[x + 1][y] + myslice[x + 1][y - 1]
									+ myslice[x][y - 1];
					}

					// PUT THE NEW VALUE OF A CELL
					if (myslice[x][y] == 1 && (sum == 2 || sum == 3))
						mynewslice[x][y] = 1;
					else if (myslice[x][y] == 1 && sum > 3)
						mynewslice[x][y] = 0;
					else if (myslice[x][y] == 1 && sum < 1)
						mynewslice[x][y] = 0;
					else if (myslice[x][y] == 0 && sum == 3)
						mynewslice[x][y] = 1;
					else
						mynewslice[x][y] = 0;

				}
			}

			for (int x = 0; x < sliceNum; x++)
				for (int y = 0; y < boardSize; y++)
					myslice[x][y] = mynewslice[x][y];

			// combine all slice together
			if (rank == 0) {
				int aBoard[][] = new int[sliceNum][boardSize];

				for (int x = 0; x < sliceNum; x++) // put your own slice
				{
					for (int y = 0; y < boardSize; y++) {
						board[x][y].newState(myslice[x][y]);
					}

				}
				for (int i = 1; i < size; i++) {
					MPI.COMM_WORLD.Recv(aBoard, 0, sliceNum, MPI.OBJECT, i, tag); // receive all others'
					for (int x = 0; x < sliceNum; x++) {
						for (int y = 0; y < boardSize; y++) {
							board[x + i * sliceNum][y].newState(aBoard[x][y]);
						}
					}
				}
			} else {
				MPI.COMM_WORLD.Send(myslice, 0, sliceNum, MPI.OBJECT, 0, tag);
			}

			if (rank == 0) {
				// Save the state as previous state
				for (int x = 1; x < boardSize - 1; x++) {
					for (int y = 1; y < boardSize - 1; y++) {
						board[x][y].savePrevious();
					}
				}
				sendMessage(new Message(1));
			}
			g = g + 1;
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
}
