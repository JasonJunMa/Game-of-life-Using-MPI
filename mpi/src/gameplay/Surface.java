package gameplay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import message.Message;
import message.MessageListener;

class Surface extends JPanel implements MessageListener, ActionListener {
	private int signal;
	protected Game gm;
	private Graphics wnd;
	private int boardSize;

	public Surface(Game game) {
		this.gm = game;
		this.boardSize = game.boardSize;
	}

	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		g2d.setPaint(Color.blue);

		int w = getWidth();
		int h = getHeight();
		// g2d.scale(w, h);

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (gm.board[i][j].getState() == 1) {
					int x = i % w;
					int y = j % h;
					g2d.setColor(Color.BLUE);
					g2d.fillRect(i, j, 1, 1);
				}
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		wnd = g;
		super.paintComponent(g);
		doDrawing(g);
	}

	public void messageReceived(Message message) {
		signal = message.getSignal();
		switch (signal) {
		case 1: {
			// this.revalidate();
			this.repaint();
			break;
		}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		repaint();
	}
}
