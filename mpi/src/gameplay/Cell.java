package gameplay;

public class Cell {
	 private int x;
	 private int y;
	 private int state ;
	 private int previousState;
	  
	 public Cell() {
		  this.x = 0;
		  this.y = 0;
		  this.state = 0;
		  this.previousState = 0;
	  }
	 
	  void newState(int newState) {
		  state = newState;
	  }
	  
	  void savePrevious() {
		    previousState = state; 
	  }
	  
	  void setX(int xAxis) {
		  this.x =xAxis;
	  }
	  
	  void setY(int yAxis) {
		  this.y =yAxis;
	  }
	  
	  int getState() {
		  return state;
	  }
	  
	  int getPrevious() {
		  return previousState;
	  }
}
