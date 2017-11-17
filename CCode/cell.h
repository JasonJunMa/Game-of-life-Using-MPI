#ifndef CELL_H
#define CELL_H

/**
 *  Class for a cell
 *  x and y are the cordinate in the board
 *  state, previousState: 0 for dead, 1 for live
 *  newState() change the state of the cell
 */

class Cell
{
public:
  short x = 0, y = 0;
  short state = 0, previousState = 0;
  void display();
  void newState(short state);
};

#endif
