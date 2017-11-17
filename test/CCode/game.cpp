#include <iostream>
#include "mpi.h"
#include "cell.h"
#include <OpenGL/gl.h>

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
        //slice the board here
    }

    /**
     * Count how many neighbors of a cell
     * Need to apply to each slice
     * 000
     * 010
     * 101
     */
    for (int x = 1; x < columns - 1; x++)
    {
        for (int y = 1; y < rows - 1; y++)
        {

            int neighbors = 0;
            for (int i = -1; i <= 1; i++)
            {
                for (int j = -1; j <= 1; j++)
                {
                    neighbors += board[x + i][y + j].previousState;
                }
            }
            neighbors -= board[x][y].previousState;

            if ((board[x][y].state == 1) && (neighbors < 2))
                board[x][y].newState(0);
            else if ((board[x][y].state == 1) && (neighbors > 3))
                board[x][y].newState(0);
            else if ((board[x][y].state == 0) && (neighbors == 3))
                board[x][y].newState(1);
        }
    }

    // Finish the MPI
    MPI_Finalize();

    return 0;
}
