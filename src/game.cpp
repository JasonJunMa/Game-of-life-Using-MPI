#include <iostream>
#include "mpi.h"
#include "cell.h"
using namespace std;

#define sizeOfBoard 1000

int main(int argc, char *argv[])
{
    /**
     * Initilized a board
     * Set random state for each cell
     * 0 for dead 1 for live
     */
    Cell board[sizeOfBoard][sizeOfBoard];
    srand(time(NULL));
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
    int size, rank;
    MPI_Status Stat;
    int rc = MPI_Init(&argc, &argv);
    if (rc != 0)
    {
        cout << "Error starting MPI." << endl;
        MPI_Abort(MPI_COMM_WORLD, rc);
    }
    // Get the size of the processor
    MPI_Comm_size(MPI_COMM_WORLD, &size);
    // Get the current processor rank
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);

    if (rank == 0)
    {
    }

    // Finish the MPI
    MPI_Finalize();

    return 0;
}
