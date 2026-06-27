package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

import view.GUICanvas;
import view.InputOutput;

/**
 * A Player in the game
 * @author Ronni Perez
 *
 */
public class Player {
	// public because final
	public final int ID;		//unique identifier, also the player number
	public final String character;		//character portrayed by this player
	public final String name;
	public Color color;			//token color on canvas
	
	private boolean isPlaying;		//disqualified or not?
	private Square location;		//current location in the board
	private Set<Card> yourCards;	//cards in-hand
	private Set<Card> cardsSeen;		//detective notes
	
	public Player(int ID, String name, String character, Square location) {
		this.ID = ID;
		this.isPlaying = true;
		this.name = name;
		this.character = character;
		this.location = location;
		this.yourCards = new HashSet<Card>();
		this.cardsSeen = new HashSet<Card>();
		this.color = setColor(character);
	}
	
	/**
	 * FOR TESTING PURPOSES ONLY
	 * @param ID
	 * @param character
	 * @param location
	 */
	public Player(int ID, String character, Square location) {
		this.ID = ID;
		this.isPlaying = true;
		this.name = "";
		this.character = character;
		this.location = location;
		this.yourCards = new HashSet<Card>();
		this.cardsSeen = new HashSet<Card>();
		this.color = setColor(character);
	}

	/**
	 * Add card to collection of cards in hand and to 'seen' cards
	 * @param cd
	 */
	public void addCard (Card cd) {
		yourCards.add(cd);
		cardsSeen.add(cd);
	}
	
	/**
	 * Supplementary method
	 * Checks if Player has Card with the name
	 * Used by Cluedo.checkSuggestion() 
	 * @param name -- long name of card
	 * @param isTesting is true iff in 'jUnit testing'. Set to true when testing
	 * @return card iff player has card, null otherwise
	 */
	public Card hasCard(String name, boolean isTesting) {
		for (Card c: yourCards) {
			if (c.name.equals(name)) {
				if (!isTesting) {		//if not testing, output
					InputOutput.popup("Suggestion Refuted", "Player "+ID+" has the card "+name);
				}
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Supplementary method
	 * Used by Board.movePiece()
	 * @param loc
	 */
	public void setLocation(Square loc) {
		this.location = loc;
	}
	
	/**
	 * Supplementary method
	 * Used by Board.expel()
	 * @param isPlaying
	 */
	public void setPlaying(boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	//GETTERS
	public Square getLocation() {
		return location;
	}
	
	@Override
	public String toString () {
		return "P"+ID;
	}

	public boolean isPlaying() {
		return isPlaying;
	}
	
	public Set<Card> allCards() {
		return yourCards;
	}

	//ADDED METHODS
	/**
	 * Returns Color of this Player depending on character portrayed
	 * @param character -- character portrayed by this Player
	 * @return
	 */
	private Color setColor(String character) {
		switch(character) {
			case "Miss Scarlett":
				return new Color(255, 36, 0);
			case "Colonel Mustard":
				return new Color(255, 128, 0);
			case "Mrs. White":
				return Color.WHITE;
			case "The Reverend Green":
				return new Color(0, 128, 0);
			case "Mrs. Peacock":
				return new Color(0, 0, 255);
			case "Professor Plum":
				return new Color(142, 69, 133);
			}
		//dead code
		return null;
	}
	
	/**
	 * Add the Card cd to detective notes
	 * @param cd
	 */
	public void addToSeen(Card cd) {
		cardsSeen.add(cd);
	}
	
	/**
	 * Manages JDialog popup message to show cards in hand
	 */
	public void showCards() {
		StringBuilder text = new StringBuilder();
		text.append("You have the following cards in your hand: \n");
		for (Card c: yourCards) {	//output each card on one line
			text.append(c.toString()+"\n");
		}
		InputOutput.popup(this.name+"'s Cards",  text.toString());
	}
	
	/**
	 * Manages JDialog popup message to show detective notes i.e. cards seen
	 */
	public void seenCards() {
		StringBuilder text = new StringBuilder();
		text.append("You have seen the following cards: \n");
		for (Card c: cardsSeen) {		//output each card on one line
			text.append(c.toString()+"\n");
		}
		InputOutput.popup(this.name+"'s Cards",  text.toString());
	}
	
	/**
	 * Draws this Player's token on boardCanvas
	 * @param g -- the graphics component of boardCanvas
	 * @param canvas -- the boardCanvas
	 */
	public void draw(Graphics2D g, GUICanvas canvas) {
		int x = canvas.getXFromBoardCol(location.col);
		int y = canvas.getYFromBoardRow(location.row);
		g.setColor(color);		//set to this color
		//draw Player token
		g.fillOval(x+5, y+5 , (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
		g.setColor(Color.BLACK);
		g.drawOval(x+5, y+5, (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
		//draw Player ID in the middle
		g.drawString("P"+ID, (int) (x+canvas.getSquareWidth()/2)-4, (int) (y+canvas.getSquareHeight()/2)+7);
	}
	
	/**
	 * Animate move from this.loc to newRow,newColumn
	 * @param g -- the graphics component of boardCanvas
	 * @param canvas -- the boardCanvas
	 * @param newRow -- valid row
	 * @param newCol -- valid column
	 * @throws InterruptedException
	 */
	public void animate(Graphics g, GUICanvas canvas, int newRow, int newCol) throws InterruptedException {
		//actual canvas coordinates x,y
		int oldX = canvas.getXFromBoardCol(location.col);		//get vital info directions
		int oldY = canvas.getYFromBoardRow(location.row);
		int newX = canvas.getXFromBoardCol(newCol);
		int newY = canvas.getYFromBoardRow(newRow);
		
		g.setColor(color);		//set to this color
		
		//decide on what direction to animate
		if (oldX < newX) animateRight(g,canvas,oldX,newX,oldY);
		else if (oldX > newX) animateLeft(g,canvas,oldX,newX,oldY);
		else if (oldY < newY) animateDown(g,canvas,oldY,newY,oldX);
		else if (oldY > newY) animateUp(g,canvas,oldY,newY,oldX);
	}
	
	//HELPER METHODS TO ANIMATE MOVE
	private void animateUp(Graphics g, GUICanvas canvas, int oldY, int newY,	int oldX) throws InterruptedException {
		for (int i = oldY; i!=newY; i--) {
			g.fillOval(oldX+5, i , (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
			Thread.sleep(canvas.delay);		//delay
		}
	}

	private void animateDown(Graphics g, GUICanvas canvas, int oldY, int newY,	int oldX) throws InterruptedException {
		for (int i = oldY; i!=newY; i++) {
			g.fillOval(oldX+5, i , (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
			Thread.sleep(canvas.delay);		//delay
		}
	}

	private void animateLeft(Graphics g, GUICanvas canvas, int oldX, int newX, int oldY) throws InterruptedException {
		for (int i = oldX; i!=newX; i--) {
			g.fillOval(i,  oldY+5, (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
			Thread.sleep(canvas.delay);		//delay
		}
	}

	private void animateRight(Graphics g, GUICanvas canvas, int oldX, int newX, int oldY) throws InterruptedException {
		for (int i = oldX; i!=newX; i++) {
			g.fillOval(i,  oldY+5, (int)(canvas.getSquareWidth()-4), (int)(canvas.getSquareHeight()-4));
			Thread.sleep(canvas.delay);		//delay
		}
	}
	
}
