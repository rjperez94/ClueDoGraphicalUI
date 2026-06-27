package model;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Set;

import view.GUICanvas;
import main.Loader;

/**
 * A Square is the actual object at row,col of a board
 * @author  Ronni Perez
 *
 */
public class Square {
	// public because final
	public final int row; // coordinates in array
	public final int col;
	public final Type kind; // room or playArea
	public final Set<int[]> neighbours; // COORDINATES of surrounding Squares of
										// this Square in row,col format
	// protected because used by subclasses
	protected String name; // long name of this Square, no name if playArea
	protected String occupied; // String representation of the occupying Player
								// of this Square
	//private because not meant to be edited illegally
	private String code; // String representation of this Square ()

	public Square(int row, int col, Type kind, String code) {
		this.row = row;
		this.col = col;
		this.kind = kind;
		this.name = " ";
		this.code = code;
		this.neighbours = new HashSet<int[]>();
		this.occupied = null;
	}

	/**
	 * Supplementary method used by Board.validNeigbours(row, col) to add
	 * neighbors
	 * 
	 * @param newRow
	 * @param newCol
	 * @param board
	 */
	public void addNeighbour(int newRow, int newCol, Square[][] board) {
		// check for bounds
		if (newRow >= 0 && newRow < board.length && newCol >= 0
				&& newCol < board[0].length) {
			// add if newRow,newCol is playArea ONLY
			if (board[newRow][newCol].kind == Type.PLAYAREA)
				neighbours.add(new int[] { newRow, newCol });
		}
	}

	/**
	 * Supplementary method used by Board.roomNeigbours(row, col) and
	 * Board.connectToDoorways() to add neighbors
	 * 
	 * @param coords
	 *            -- coordinates of neighbors in row,col format (ALWAYS even number of elements) 
	 *            e.g. new int [] {1,4,5,5,8,4,5,4} means neighbors are: {1,4} {5,5} {8,4} {5,4}
	 */
	public void addNeighbour(Board board, int[] coords) {
		for (int i = 0; i < coords.length; i += 2) {
			Square sq = board.getSquare(coords[i], coords[i + 1]);
			if (sq.kind == Type.ROOM) {
				floodFill(board,sq.name);
			} else {
				neighbours.add(new int[] { coords[i], coords[i + 1] });
			}
		}
	}
	
	/**
	 * Checks if row,col is a neighbor of this Square
	 * @param row
	 * @param col
	 * @return true iff this square has Square at row,col as it's neighbor
	 */
	public boolean hasNeigbour(int row, int col) {
		for (int[] pair : neighbours) {		//each row,col coordinates
			if (pair[0] == row && pair[1] == col) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if this Square has a "Room" Square neighbor i.e. is a doorway
	 * @param board -- the game board
	 */
	public void changeToDoorway(Board board) {
		for (int[] pair : neighbours) {		//get row,col coordinates
			Square sq = board.getSquare(pair[0], pair[1]);
			if (sq.kind == Type.ROOM ) {
				floodFill(board, sq.name);	//ADDED LINE
				this.code = "dw";		//change code i.e. string output to dw
				return;
			}
		}
	}

	@Override
	public String toString() {
		if (occupied != null) {
			return occupied.toString() + " ";
		}
		
		if (this.kind == Type.ROOM) {		//if room, then use two-letter code for output
			return code + " ";
		} else if (this.kind == Type.NULL) {		//if null type, then use two-letter code for output
			return code + " ";
		} else if (this.code.equals("dw")) {		//if doorway, then use two-letter code for output
			return code + " ";
		} else {			//else  use double underscores ("__")
			return "__ ";
		}
	}

	// GETTERS
	public String getName() {
		return name;
	}

	public String getOccupied() {
		return occupied;
	}
	
	public String getCode () {
		return code;
	}

	//2 kinds of Square, (part of a) Room or (normal) playarea
	public enum Type {
		ROOM, PLAYAREA, NULL
	}
	
	//ADDED METHODS
		/**
		 * </br>Supplementary method used by changeToDoorway()
		 * Makes an Square doorway be a neighbor to all roomSquares it connects to
		 * @param board -- the game board
		 * @param roomName -- name of room that we will 'floodFill'
		 */
		/*
		 * 	02 03 04		e.g. if {3,2} is a DoorWay which leads to KitChen,
		 		.............			I only need to assign dw once to KC and this
	02 .....  KC KC KC 			method ensures that ALL Squares with KC is a
	03 .....  __ dw __ 			neighbor of dw
	04 .....  __ __ __ 
		 */
		private void floodFill(Board board, String roomName) {
			for (int row = 0; row < Loader.rows(); row++) {
				for (int col = 0; col < Loader.cols(); col++) {
					if (board.getSquare(row, col).name.equals(roomName)) {
						this.neighbours.add(new int[] { row, col });
					}
				}
			}
			
		}
		
		/**
		 * Draw neighbors of this Square to Canvas
		 * @param g -- graphics context for Canvas
		 * @param canvas -- board canvas
		 * @param board -- underlying game structure
		 * @param remaining -- move steps left for Player in this Square
		 * @param visited -- visited Squares of Player in this Square
		 */
		public void drawNeighbours(Graphics2D g, GUICanvas canvas, Board board, int remaining, Set<int[]> visited) {
			if (remaining <=0) {return;}		//if none left

			Player p = board.getPlayer(Integer.parseInt(""+occupied.charAt(1)));	//get occupying Player
			Stroke stk = g.getStroke();		//save current 'border' thickness and set 'border' thickness to 5
			g.setStroke(new BasicStroke((int) (canvas.getSquareWidth()/8)));
			for (int [] pair: neighbours) {		//for each neighbor
				if (board.isValid(p.ID, pair[0], pair[1], visited)) {		//if not visited, mean can visit so...
					g.setColor(p.color.brighter().brighter().brighter());		//draw using player color but lighter
					g.drawOval(canvas.getXFromBoardCol(pair[1])+11, canvas.getYFromBoardRow(pair[0])+10, (int) canvas.getSquareWidth()/2, (int) canvas.getSquareHeight()/2);
				}
			}
			g.setStroke(stk);		//set 'border' thickness back
		}
}
