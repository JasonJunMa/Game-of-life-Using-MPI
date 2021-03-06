#include "mpi.h"
#include <iostream>
#include <string>
#include <fstream>
#include <stdlib.h>
using namespace std;

int main(int argc, char* argv[])
{
	int size, rank, tag, rc, N, generations, outPoints, s;

	MPI_Status Stat;
	ofstream output("output.txt"); 	//output file
	rc=MPI_Init(&argc,&argv);
	if (rc!=0) {cout << "Error starting MPI." << endl; MPI_Abort(MPI_COMM_WORLD, rc);}
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	printf("%d size\n", size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	printf("%d rank\n", rank);

	if (rank==0){
		// I am processor 0
		if (argc<2) {
			cout << "Input file not specified" << endl;
			printf("This is bad \n");
			MPI_Finalize();
			return -1;
			}
			
		ifstream file(argv[1]);	//input file
		printf("YO 1 \n");

		if (!file)
		{
			cout << "Error opening file"<<endl;
			MPI_Finalize();
			return -1;
		}
		printf("PreFile\n");
		file >> N >> generations >> outPoints;	//first three variables from file
		printf("File Read\n");	
		printf("%d\n", N);
		printf("%d\n", generations);
		printf("%d\n", outPoints);
//	N = 20;
//		generations = 2;
//		outPoints = 10;
		s=N/size;	//how many slices 
		int theBoard[N][N];	
		for (int i=0;i<N;i++){	//read file into array
			string temp;
			file >> temp;
			for (int j=0; j<N; j++){

				theBoard[i][j]=temp[j]-'0';
				printf("%d", theBoard[i][j]);
			}
		}
		file.close();
		printf("yo  \n");

		//SENDING INITIAL INFORMATION (N, k, #generations, output points) TO EVERYONE
		int info[4];
		info[0]=N; info[1]=s; info[2]=generations; info[3]=outPoints;
		for (int dest=0; dest<size; dest++) {
//			MPI_Send(&info[0], 1, MPI_INT, dest, 1, MPI_COMM_WORLD);
			printf("\nSending to %d of %d\n", dest, size);
			if(dest != 0)
				MPI_Send(&info, 4, MPI_INT, dest, 1, MPI_COMM_WORLD); //send inifo
			printf("\nSent\n");
		}
		int slice[N/size][N];	
		for (int z=1; z<size; z++)
		{
			for (int k=0; k<s; k++)
			{ 
				for (int l=0; l<N; l++) 
				{
					slice[k][l]=theBoard[k+(z*s)][l];	//cut a slice from the the board
					printf("%d\n",slice[k][l]);
				}
			}
			printf("\nSending Slice to %d\n", z);
			MPI_Send(&slice, N*s, MPI_INT, z, 1, MPI_COMM_WORLD);	//and send it
			printf("\nSent\n");
		}

	} // end of processor 0 code
	printf("\nRecieving\n");
	//RECEIVED INITIAL INFORMATION
	int localinfo[4];		// local info for initial information
	MPI_Recv(&localinfo, 4, MPI_INT, 0, 1, MPI_COMM_WORLD, &Stat);	//receive info
	int myslice[localinfo[1]][localinfo[0]]; //my own slice of the board
	MPI_Recv(&myslice, localinfo[0]*localinfo[1], MPI_INT, 0, 1, MPI_COMM_WORLD, &Stat);	//receive slice
	N = localinfo[0];			//assign variables
	s = localinfo[1];			//
	generations=localinfo[2];	//
	outPoints=localinfo[3];		//
	
	int todown[N];	int toup[N]; int fromdown[N]; int fromup[N]; //arrays to send and to receive
	for (int g=1; g<=generations; g++) //generations forloop
	{	
		printf("hi");
		if (rank!=size-1) // all except for last send down
		{
			printf("\nsenddown to %d\n", rank);
			for (int j=0; j<N; j++) 
				todown[j]=myslice[s-1][j];
			MPI_Send(&todown, N, MPI_INT, rank+1, 1, MPI_COMM_WORLD);
			printf("\nsent to rank %d\n", rank-1);

		} else {
			for (int k=0; k<N; k++)
			{
				 fromdown[k]=0; 
			} // last one generates empty stripe "from down"
		}

		if (rank!=0) // all except for first receive from up
		{
			printf("\nrecieving from up %d\n", rank-1);
			MPI_Recv(&fromup, N, MPI_INT, rank-1, 1, MPI_COMM_WORLD, &Stat);
			printf("\nrecieved\n");	

		} else { 
			for (int k=0; k<N; k++) 
				fromup[k]=0; 
			} // first one generats empty line "from up"	
	
		if (rank!=0) // all except for first send up
		{
			printf("\nSending up from %d\n", rank);
			for (int j=0; j<N; j++) 
				toup[j]=myslice[0][j];
			MPI_Send(&toup, N, MPI_INT, rank-1, 1, MPI_COMM_WORLD);
			printf("\nSent up to %d\n", rank-1);
		}
	
		if (rank!=size-1) // all except for last receive from down
		{
			printf("\nRecieving from down %d\n", rank+1);
			MPI_Recv(&fromdown, N, MPI_INT, rank+1, 1, MPI_COMM_WORLD, &Stat);
		}

		//COUNTING NEIGHBORS
		int sum=0; // sum of neighbours
		int mynewslice[s][N];
		printf("Starting update\n");
		
		for(int i = 0; i < s; i++)
		{
			for(int j = 0; j < N; j++)
			{
		//		mynewslice[i][j]=0;
				printf("%d ", myslice[i][j]);
			}
			printf("\n");
		}

		for (int x=0; x<s; x++) //for each row
		{	
			printf("\ndd\n");
			for (int y=0; y<N; y++) //for each column
			{
				if (x==0 && y==0) //upper-left cell
					sum = myslice[x+1][y]+myslice[x+1][y+1]+myslice[0][y+1]+fromup[0]+fromup[1];
				else if (x==0 && y==N-1) //upper-right cell
					sum = myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y]+fromup[N-1]+fromup[N-2];
				else if (x==s-1 && y==0) //lower-left cell
					sum = myslice[x][y+1]+myslice[x-1][y+1]+myslice[x-1][y]+fromdown[0]+fromdown[1];
				else if (x==s-1 && y==N-1) //lower-right cell
					sum = myslice[x-1][y]+myslice[x-1][y-1]+myslice[x][y-1]+fromdown[N-1]+fromdown[N-2];
				else // not corner cells    
				{
					if (y==0) // leftmost line, not corner
						sum=myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x+1][y+1]+myslice[x+1][y];
					else if (y==N-1) //rightmost line, not corner
						sum=myslice[x-1][y]+myslice[x-1][y-1]+myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y];
					else if (x==0) //uppermost line, not corner
						sum=myslice[x][y-1]+myslice[x+1][y-1]+myslice[x+1][y]+myslice[x+1][y+1]+myslice[x][y+1]+fromup[y-1]+fromup[y]+fromup[y+1];
					else if (x==s-1) //lowermost line, not corner
						sum=myslice[x-1][y-1]+myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x][y-1]+fromdown[y-1]+fromdown[y]+fromdown[y+1];
					else //general case, any cell within
						sum=myslice[x-1][y-1]+myslice[x-1][y]+myslice[x-1][y+1]+myslice[x][y+1]+myslice[x+1][y+1]+myslice[x+1][y]+myslice[x+1][y-1]+myslice[x][y-1];
				}
	//			printf("\nSUM: %d\n", sum);
				//PUT THE NEW VALUE OF A CELL
				if (myslice[x][y]==1 && (sum==2 || sum==3)) mynewslice[x][y]=1;
				else if (myslice[x][y]==1 && sum>3) mynewslice[x][y]=0;
				else if (myslice[x][y]==1 && sum<1) mynewslice[x][y]=0;
				else if (myslice[x][y]==0 && sum==3) mynewslice[x][y]=1;
		 		else mynewslice[x][y]=0;
			
			}
		}
		
		printf("Copying new Slice\n");
		// copy new slice onto myslice
		for (int x=0; x<s; x++)
		{
			for (int y=0; y<N; y++)
			{
				myslice[x][y]=mynewslice[x][y];
				printf("%d ", myslice[x][y]);
			}
			printf("\n");
		}

		printf("printing to file");
		//PRINTING THE RESULT TO FILE
		if (g%outPoints==0) //s-th generation, send everything to node 0
		{
			if (rank==0) 
			{
				printf("\nprinting...\n");
				int aBoard[s][N];
				output << "Generation " << g << ":" << endl;
				for (int x=0; x<s; x++) //put your own slice
				{
					for (int y=0; y<N; y++)	output << myslice[x][y];
					output << endl;
				}
				for (int i=1; i<size; i++)
				{
					MPI_Recv(&aBoard, N*s, MPI_INT, i, 1, MPI_COMM_WORLD, &Stat); //receive all others'
					for (int x=0; x<s; x++)
					{
						for (int y=0; y<N; y++) output << aBoard[x][y];
						output << endl;
					}
				}
				output << endl << endl;
			}
			else 
			{
				printf("\nSending to 0 prime\n");
				MPI_Send(&myslice, N*s, MPI_INT, 0,1, MPI_COMM_WORLD);
				printf("\nSent to 0 prime\n");
			}
		}	
	} // end of generation loop

output.close();
MPI_Finalize();
}

