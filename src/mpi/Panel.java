package mpi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class Panel extends JFrame {
	protected Game gm;
	 public Panel(Game game) {
		 this.gm = game;
	        initUI();
	    }

	 private void initUI() {

	        final Surface surface = new Surface(gm);
	        add(surface);

	        addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowClosing(WindowEvent e) {
	            	
	            }
	        });

	        setTitle("Game of life Using MPI");
	        setSize(1000, 1000);
	        setLocationRelativeTo(null);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	 }
}
