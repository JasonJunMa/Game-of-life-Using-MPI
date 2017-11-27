package mpi;

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

public class Game extends JFrame implements MessageProducer {
	public Cell board[][];
	private MessageHandler messageHandler; // delegate to handle messages

	public Game(int x, int y) {
		board = new Cell[x][y];
		this.messageHandler = new MessageHandler();
		this.initBoard();
		this.initUI();
	}

	public void initBoard() {
		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		for (int i = 0; i < 1000; i++) {
			for (int j = 0; j < 1000; j++) {
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
		f.setSize(1000, 1000);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void start() {

		for (int kk = 1; kk < 10000; kk++) {
			for (int x = 1; x < 1000 - 1; x++) {
				for (int y = 1; y < 1000 - 1; y++) {
					int neighbors = 0;
					for (int i = -1; i <= 1; i++) {
						for (int j = -1; j <= 1; j++) {
							neighbors += board[x + i][y + j].getPrevious();
						}
					}
					neighbors -= board[x][y].getPrevious();

					if ((board[x][y].getState() == 1) && (neighbors < 2))
						board[x][y].newState(0);
					else if ((board[x][y].getState() == 1) && (neighbors == 2 || neighbors == 3))
						board[x][y].newState(1);
					else if ((board[x][y].getState() == 1) && (neighbors > 3))
						board[x][y].newState(0);
					else if ((board[x][y].getState() == 0) && (neighbors == 3))
						board[x][y].newState(1);
				}
			}

			//Save the state as previous state
			for (int x = 1; x < 1000 - 1; x++) {
				for (int y = 1; y < 1000 - 1; y++) {
					board[x][y].savePrevious();
				}
			}

			sendMessage(new Message(1));
		}
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

	public static void main(String[] args) {
		Game gm = new Game(1000, 1000);
		gm.start();
	}
}
