package view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import main.Loader;
import model.Board;
import model.Player;
import model.Square.Type;

/**
 * The Board Canvas
 * @author Ronni Perez
 *
 */
public class GUICanvas extends Canvas {
	private GUI ui;		//main GUI
	private Image boardImage;	//assets/board.png
	private Board board;	//underlying data structure
	private Map<String, Point> weaponPrintLoc;		//location where to print particular weapons in certain rooms

	private int squareWidth;		//one Square size, changes depending on frame size
	private int squareHeight;
	
	public final int delay = 9;		//move animation delay
	public boolean isReady;		//true iff this canvas is ready to be drawn

	private static final long serialVersionUID = -413343824269512630L;
	
	/**
	 * Constructor
	 * @param ui -- parent component
	 * @param width
	 * @param height
	 */
	public GUICanvas(GUI ui, int sqWidth, int sqHeight) {
		new HoverListener();		//listens to mouse hovers
		
		this.boardImage = makeImage(GUICanvas.class.getResourceAsStream("assets/board.png"));
		this.squareWidth = sqWidth;
		this.squareHeight = sqHeight;
		this.ui = ui;
		this.weaponPrintLoc = printLocations();
		this.isReady = false;
	}

	/**
	 * Assign a game board to this canvas
	 * @param board
	 */
	public void setBoard(Board board) {
		this.board = board;
	}
	
	/**
	 * HELPER 
	 * </br> Used to generate Image from image filename
	 * @param inputStream
	 * @return
	 */
	private BufferedImage makeImage(InputStream inputStream) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	/**
	 * HELPER
	 * </br>Location where to print particular weapons in certain rooms
	 * Represented as: print at BoardRow, BoardCol === new Point(Col, Row)
	 * @return
	 */
	private Map<String, Point> printLocations() {
		Map<String, Point> roomToLoc = new HashMap<String, Point>();
		roomToLoc.put("Kitchen", new Point(1, 1));
		roomToLoc.put("Ball Room", new Point(8, 1));
		roomToLoc.put("Conservatory", new Point(15, 1));
		roomToLoc.put("Billiard Room", new Point(14, 8));
		roomToLoc.put("Library", new Point(15, 11));
		roomToLoc.put("Study", new Point(15, 16));
		roomToLoc.put("Hall", new Point(8, 15));
		roomToLoc.put("Lounge", new Point(1, 15));
		roomToLoc.put("Dining Room", new Point(1, 7));
		return roomToLoc;
	}

	@Override
	public void paint(Graphics g) {
		if (!isReady) {return;}

		Graphics2D g2d = (Graphics2D) g;
		// for antialising geometric shapes
		g2d.addRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		// for antialiasing text
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2d.drawImage(boardImage, 0, 0, this.getWidth(), this.getHeight(), null); // draw board TODO EDITED

		Map<String, String> weaponsInRoom = ui.getWeapons();
		for (Map.Entry<String, Point> e : weaponPrintLoc.entrySet()) { 
			//for each weapon
			String weapon = weaponsInRoom.get(e.getKey());
			if (weapon != null) {
				g2d.setColor(Color.BLACK);
				g2d.drawString("(" + weapon + ")",
						getXFromBoardCol(e.getValue().x),
						getYFromBoardRow(e.getValue().y));
			}
		}

		for (int ID = 1; ID <= 6; ID++) { // draw each player
			Player p = board.getPlayer(ID);
			if (p != null) { // which exists
				if (p.isPlaying()) { // and is playing/active
					p.draw(g2d, this);
					if (p.ID == ui.getCurrent()) {	//draw available places to move for current Player
						p.getLocation().drawNeighbours(g2d,this, board, ui.getRemaining(), ui.visited);
					}
				}
			}
		}

	}

	/**
	 * Canvas 'corner' x given board column
	 * @param col -- board column
	 * @return
	 */
	public int getXFromBoardCol(int col) {
		return col * squareWidth;
	}

	/**
	 * Canvas 'corner' y given board row
	 * @param row -- board row
	 * @return
	 */
	public int getYFromBoardRow(int row) {
		return row * squareHeight;
	}
	
	//GETTERS
	public double getSquareWidth() {
		return squareWidth;
	}

	public double getSquareHeight() {
		return squareHeight;
	}
	
	//SETTERS
	public void setSquareWidth(int newWidth) {
		this.squareWidth = newWidth;
	}

	public void setSquareHeight(int newHeight) {
		this.squareHeight = newHeight;
	}

	/**
	 * Manages GUI mouse hover input/output
	 * @author Ronni Perez
	 *
	 */
	private class HoverListener implements MouseMotionListener {
		private GUICanvas parent;
		
		/**
		 * Constructor
		 */
		public HoverListener() {
			parent = GUICanvas.this;
			parent.addMouseMotionListener(this);
		}

		/**
		 * Manages status bar output on hover
		 * @param p -- Point that mouse is moved
		 */
		private void showToolTip(Point p) {
			if(!parent.isReady) {return;}	//parent component i.e. GUI must be ready to draw
			
			//point to board row, col
			int row = parent.ui.getBoardRowFromY(p.y);
			int col = parent.ui.getBoardColFromX(p.x);
			if (row <0 || row >= Loader.rows() || col < 0 || col >= Loader.cols()) {
				return;
			}
			
			if (parent.board.getSquare(row, col).getOccupied() == null) {		//if not occupied by any player
				if (parent.board.getSquare(row, col).kind == Type.ROOM) {		//if part of a room, output room
					String roonName = parent.board.getSquare(row, col).getName();
					parent.ui.status.setText("MOUSE HOVERING at r: "+row+" c: "+col+". This is part of "+roonName);
				} else if (parent.board.getSquare(row, col).kind == Type.PLAYAREA){		//if not part of a room, output row,col
					parent.ui.status.setText("MOUSE HOVERING at r: "+row+" c: "+col);
				} else {			//if CentreRoom/logo
					parent.ui.status.setText("MOUSE HOVERING at r: "+row+" c: "+col+" This point is out of bounds");
				}
			} else {		//if occupied by whoever player
				int ID = Integer.parseInt(""+parent.board.getSquare(row, col).getOccupied().charAt(1));
				Player pl = parent.board.getPlayer(ID);
				parent.ui.status.setText("This is player '"+pl.name+"' playing the role of "+pl.character);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			showToolTip(e.getPoint());		//get where mouse was moved
		}

		//TODO		//USELESS METHODS HERE TO FULFILL INTERFACE
		@Override
		public void mouseDragged(MouseEvent e) {
		}
	}
	
}