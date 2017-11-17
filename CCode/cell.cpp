#include "cell.h"

// stateFlag can be 0 or 1
void Cell::newState(short stateFlag)
{
    previousState = state;
    state = stateFlag;
}
