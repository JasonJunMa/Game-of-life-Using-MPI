#include <iostream>
#include "mpi.h"
#include "cell.h"
using namespace std;

int main(int argc, char *argv[])
{
    int size, rank;
    Cell board[1000][1000];
    MPI_Status Stat;
    srand(time(NULL));

    /**
     * Initilized a board
     * Set random state for each cell
     * 0 for dead 1 for live
     */
    for (int i = 0; i < 1000; i++)
    {
        for (int j = 0; j < 1000; j++)
        {
            board[i][j].x = i;
            board[i][j].y = j;
            board[i][j].state = rand() % 2;
            board[i][j].previousState = board[i][j].state;
        }
    }

    /**
     * Init MPI
     */
    int rc = MPI_Init(&argc, &argv);
    if (rc != 0)
    {
        cout << "Error starting MPI." << endl;
        MPI_Abort(MPI_COMM_WORLD, rc);
    }
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    return 0;
}
