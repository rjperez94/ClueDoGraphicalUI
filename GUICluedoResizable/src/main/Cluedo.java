package main;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import view.GUI;
import view.InputOutput;
import model.Board;
import model.Card;
import model.CentreRoom;
import model.Player;
import model.Square;
import model.Square.Type;

/**
 * Has main logic of ClueDo game
 * <br> -- This places N player tokens depending on how many are playing
 * <br> -- P1 always starts up to P N
 * <br> -- Can move horizontal or vertical
 * <br> -- Moving from one room to another room still REQUIRES 1 MOVE step
 * <br> -- Entering room ends turn
 * <br> -- Can make suggestions as long as you're in a room
 * <br> -- Automatic refute of suggestion
 * <br> -- Cannot forfeit turn
 * <br> -- Disqualified players stay in the game only to prove other’s suggestions wrong 
 * 	with the cards they hold in their hands
 * @author Ronni Perez
 *
 */
public class Cluedo {
	//private InputParser parser;	//parse text in system.in
	private Loader loader;		//load board file and game "environment"
	private Board board;
	private final int rows = 18;		//specs says 25x25 but done 18x18 instead, tedious hardcoding
	private final int cols = 18;
	private Map<String, String> weaponsInRoom;	//room name to weapon -- see Section 2.4 of instructions
	private int players;		//number of players at the beginning, this may decrease over time if player is eliminated
	private int current;		//player ID of the one in current turn
	private int remaining;	//remaining MOVES for the current player
	private CentreRoom solution;	//where solution is kept
	private boolean isWon;		//if game over
	private GUI ui;		//user interface
	
	public boolean loaded;		//are components loaded
	
	/**
	 * Main constructor
	 */
	public Cluedo() {
		InputOutput.popup("Game Start", "Welcome to Cluedo game");
		
		loaded = false;
		loader = new Loader (rows, cols);		//parse txt file from assets/board.txt
		ui = new GUI(this);		//render image from assets/board.png
		
		reset();		//setup game
		turnInfo();		//start play
	}
	
	/**
	 * Constructor used for Testing ONLY
	 * This does not call reset() and turn();
	 * @param players -- number of players
	 */
	public Cluedo(int players) {
		this.players = players;
		loader = new Loader (rows, cols);		//parse txt file from assets/board.txt
		isWon = false;
		List <Card> cards = loader.populateCards();		//game cards
		board = new Board(loader.getCoords(), this.players);		//get board "string" representation
		//pick 'solution' cards and put them in centre room
		solution = new CentreRoom();
		loader.pickSolution (solution,cards);
		
		weaponsInRoom = loader.putInRoom();		//put each weapon 'token'??? in a room-- see Section 2.4 of instructions
		deal(cards);		//give cards to players

		current = 1;		//P1 always goes first
		remaining = 0;
	}

	/**
	 * Setup game of Cluedo
	 * This can be recalled later when the game finishes
	 * if user want to play again
	 */
	public void reset() {
		loaded = false;
		isWon = false;
		List <Card> cards = loader.populateCards();		//game cards
		players = InputOutput.numPlayers();		//game players
		String [] playerNames = InputOutput.playerNames(players);	//player names
		//player to characters played
		Map<String,String>nameToChar = InputOutput.playerChars(playerNames);
		board = new Board(loader.getCoords(), players, playerNames, nameToChar);		//get board "string" representation
		ui.setBoard(board);
		//pick 'solution' cards and put them in centre room
		solution = new CentreRoom();
		loader.pickSolution (solution,cards);
		
		weaponsInRoom = loader.putInRoom();		//put each weapon in a room
		deal(cards);		//give cards to players
		
		current = 1;		//P1 always goes first
		remaining = 0;
		ui.showKeys(); //show key mappings
		turnInfo();		//start play
		loaded = true;
		ui.setReady(true);
	}

	/**
	 * Distributes remaining cards to players
	 * @param cards -- unshuffled cards which are left after solution was picked
	 */
	private void deal(List<Card> cards) {
		Collections.shuffle(cards);		//shuffle cards
		int dealTo = 1;
		while (!cards.isEmpty()) {		//deal each card
			Card c = cards.remove(0);
			Player p = board.getPlayer(dealTo);
			p.addCard(c);			//add to player's card
			
			if (dealTo >= players) 	dealTo = 1;
			else 	dealTo++;
		}
	}
	
	

	/**
	 * Redraws labels in GUI
	 */
	private void turnInfo() {
		remaining = roll();		//roll the die
		Player p = board.getPlayer(current);		//get token of current player
		Square loc = p.getLocation();
		ui.visited = new HashSet<int[]>();		//maintain visited set of Squares during turn
		ui.visited.add(new int [] {loc.row, loc.col});	//add current player position
		//inform of whos's turn it is
		ui.playerTurn.setText("It's "+p.name+" (P"+current+")'s turn");
		ui.characterLbl.setText(" Go "+p.character);
		ui.diceRoll.setText("You rolled:"+remaining);
		ui.status.setText("");
		ui.draw();
		ui.endBtn.setEnabled(false); 		//must move at least once or accuse or suggest
	}

	/**
	 * Generates random number from 1....6
	 * 1 Die ONLY, uncomment code for dice
	 * @return
	 */
	private int roll() {
		return ((int)(Math.random()*6) + 1) ;
				//+ ((int)(Math.random()*6) + 1);
	}
	
	/**
	 * Present ONLY valid options to current player
	 * @return number of current player's choosing
	 * 
	 * 0 End turn  (available only if player has moved at least one move step)
	 * 1 Make an ACCUSATION
	 * 2 Announce SUGGESTION (available only if player is in a room)
	 */
	public void makeChoice(int option) {
		if (!isWon) {		//if game not won
			//execute accordingly
			//option 2 only available when current player is in a room
			switch (option) {
			case 0: remaining = 0; break;
			case 1: chooseAccuse(); break;
			case 2: chooseSuggest(board.getPlayer(current).getLocation().getName()); break;
			}
		}
		if (remaining <=0 || isWon) {
			evaluate();
		}
	}
	
	/**
	 * Chooses the next situation based on the game state
	 * i.e. next player moves, game over (no winner), game over (has winner)
	 * and asks user to play again in the latter cases
	 */
	private void evaluate() {
		if (!isWon && !gameOver()) {			//check if any players left or game not won
			nextMove(false);		//call turn
			return;
		} else if (!isWon && gameOver()) {		//game stops, no winner
			InputOutput.popup("Game Over", "The case has gone cold. No active players left");
		}
		//ask user to play again
		int r = InputOutput.yesOrNo("Play Again", "Do you want to play again?");
		if (r == JOptionPane.YES_OPTION) {
			ui.setReady(false);
			reset();
		}
	}
	
	/**
	 * Increment 'current' player with the ID of next player player
	 * i.e. 
	 * if 4 players playing, then current == from 1 to 4 until game finishes;
	 * if 6 players playing, then current == from 1 to 6 until game finishes;
	 * 
	 * @param isTesting is true iff in 'jUnit testing'. Set to true when testing
	 */
	private void nextMove(boolean isTesting) {
		do {
			if (current >= players)		//inspect limit
				current = 1;
			else
				current++;
		} while (!board.isPlaying(current));	//inspect if player has been disqualified i.e. made false accusation
		
		if (!isTesting) {		//if playing game, i.e. not testing
			turnInfo();		//re-call turn for next player
			String room = board.inRoom(current);	//get room location of current player	
			if (room != null) {		//player is in a room (any room)
				ui.suggestBtn.setEnabled(true);
			} else {
				ui.suggestBtn.setEnabled(false);
			}
		}
	}

	/**
	 * Game stop or continue
	 * 
	 * @return true iff there are no players remaining i.e. everyone has been
	 *         disqualified/expelled()
	 */
	private boolean gameOver() {
		for (int i = 1; i <= players; i++) {
			if (board.getPlayer(i).isPlaying()) {		//look for active players
				return false;
			}
		}
		//there are no active players
		return true;
	}
	
	/**
	 * Announce a Suggestion using the room that the current player is in
	 * You can only suggest using the room you are in
	 * @param room -- room that the current player is in
	 * Assume room is not null
	 */
	private void chooseSuggest (String room) {
		String person = InputOutput.getString("Choose Character", "SUGGEST a suspect: ", Loader.getPeople());		//pick character
		if (person == null) return;	
		String weapon = InputOutput.getString("Choose Weapon", "SUGGEST a murder weapon: ", Loader.getWeapons());		//pick weapon		
		if (weapon == null) return;
		
		//inform user of his/her suggestion
		InputOutput.popup("Your Suggestion", "You suggested: "+ person+", "+room+", "+weapon);
		
		//move player and weapon in room -- via power of suggestion
		moveCharAndWeapon(person, weapon, room);
		
		//refute this suggestion
		checkSuggestion(person, weapon, room);
	}

	/**
	 * Put player that plays the 'person' and the specified weapon in this room
	 * @param person -- name of character
	 * @param weapon -- name of weapon
	 * @param room -- name of room
	 */
	private void moveCharAndWeapon(String person, String weapon, String room) {
		Player p = board.getPlayer(person);
		//update or move player token when possible
		if (p != null) {		//if player that represents character is playing
			outerloop:
			for (int row = 0; row < this.rows; row++) {
				for (int col = 0; col < this.cols; col++) {
					Square sq = board.getSquare(row, col);
					if (sq.getName().equals(room) && sq.getOccupied() == null) {	//if vacant space found
						board.movePlayerViaSuggestion(p.ID, row, col);		//move player token to row,col
						InputOutput.popup(null, p.character+"(P"+p.ID+") will be moved to {"+row+", "+col+"} "+sq.getName());
						ui.draw();
						break outerloop;		//end loop
					}
				}
			}
		}
		
		//update weapon mappings e.g.
		//kitchen has dagger
		//lounge  has candlestick
		//suggestion is dagger and lounge so:
		//kitchen to lounge               dagger
		
		String wpn = weaponsInRoom.get(room);	//old weapon, if any		//candlestick
		String rm = getRoom(weapon);	//old room			//kitchen
		
		//update i.e. swap
		weaponsInRoom.put(rm, wpn);
		weaponsInRoom.put(room, weapon);
	}

	/**
	 * Check for room that a given weapon is in
	 * @param weapon -- name of weapon to search for
	 * @return name of room that a weapon is in (will never be null)
	 */
	private String getRoom(String weapon) {
		for (Map.Entry<String, String> entry: weaponsInRoom.entrySet()) {
			if ((weapon).equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		//dead code
		return null;
	}

	/**
	 * Try to refute suggestion of current player  by other players
	 * The card to be shown (if any) is chosen by the computer at random
	 * @param person -- suspect chosen by the current player
	 * @param weapon -- weapon chosen by the current player
	 * @param room -- room that the current player is in
	 */
	private void checkSuggestion(String person, String weapon, String room) {
		int nextPlayer = current +1;		//next player to show if it has one of those cards
		while (nextPlayer != current) {		//go through every player
			//even if player is disqualified, allow show card
			if (nextPlayer > players) 	nextPlayer = 1;
			else 	nextPlayer++;
			
			Player p = board.getPlayer(nextPlayer);		//get next player that is not disqualified
			if (p != null) {
				//inspect each card of next player if he has either of the three elements in the suggestion
				//output is handled by the Player class
				if (refuteSuggestion(p, person, weapon, room)) {
					return;
				}
				//this next player cannot refute the current player's suggestion, go to next eligible player
				InputOutput.popup("Info", "P"+p.ID+" cannot refute the suggestion");
			}
		}
		
		//if it gets here, then no one can refute the suggestion
		InputOutput.popup("Unrefutable", "No one can refute the suggestion");
	}

	/**
	 * ADDED METHOD
	 * Checks if Player p can refute suggestion of current player
	 * @param p -- the Player refuting the suggestion
	 * @param person -- name of person in suggestion
	 * @param weapon -- name of weapon in suggestion
	 * @param room -- name of room in suggestion
	 * @return true if p can refute suggestion of current Player
	 */
	private boolean refuteSuggestion(Player p, String person, String weapon, String room) {
		Card personCard = p.hasCard(person, false);
		if (personCard != null) {
			board.getPlayer(current).addToSeen(personCard);	//add this card to seen card of current player
			return true;		//card found
		}
		Card weaponCard = p.hasCard(person, false);
		if (weaponCard != null) {
			board.getPlayer(current).addToSeen(weaponCard);	//add this card to seen card of current player
			return true;		//card found
		}
		Card roomCard = p.hasCard(person, false);
		if (roomCard != null) {
			board.getPlayer(current).addToSeen(roomCard);	//add this card to seen card of current player
			return true;		//card found
		}
		return false;		//Player p cannot refute suggestion
	}

	/**
	 * Make an Accusation
	 * At the end of this method, 'current' player can either win or be disqualified
	 */
	private void chooseAccuse() {
		String person = InputOutput.getString("Choose Character", "Choose the suspect:", Loader.getPeople());		//pick character
		if (person == null) return;
		String weapon = InputOutput.getString("Choose Weapon", "Choose the murder weapon: ", Loader.getWeapons());		//pick weapon		
		if (weapon == null) return;
		String room = InputOutput.getString("Choose Room", "Choose the scene of the crime:", Loader.getRooms());		//pick room (any room)
		if (room == null) return;
		
		//inform user of his/her accusation
		InputOutput.popup("Your Suggestion", "You chose: "+ person+", "+room+", "+weapon);
		
		if (person.equals(solution.getSuspect()) 		//if accusation matches solution
				&& weapon.equals(solution.getWeapon()) 
				&& room.equals(solution.getPlace()) ) {
			InputOutput.popup(null, "You WON!!!");
			InputOutput.popup(null, board.getPlayer(current).character+" won the game");
			InputOutput.popup(null, "The suspect is "+person+". He/She used the "+weapon+" to kill Dr Black in the "+room);
			isWon = true;
			makeChoice(0);			// stop turn immediately
		} else {		//else, disqualify/expel 'current' player
			InputOutput.popup("You're out", "P"+current+"'s accusations are baseless. P"+current+" is expelled from the game BUT can still rubute suggestions");
			board.expel(current);
			makeChoice(0);			// stop turn immediately
		}
	}
	
	/**
	 * Manage moving token one step in the board
	 * @param newRow -- row to move to
	 * @param newCol -- column to move to
	 */
	public void chooseMove(int newRow, int newCol) {
		if (remaining > 0) {
			int oldRow = board.getPlayer(current).getLocation().row;
			int oldCol = board.getPlayer(current).getLocation().col;
			if (board.movePiece(current,newRow, newCol, ui.visited)) {		//if move is valid
				//update visited set
				updateVisited(oldRow, oldCol);
				
				ui.endBtn.setEnabled(true);		//can now end turn if you wish
				
				ui.status.setText("");	//clear status bar text
				//if  NOT entered room
				Square newSquare = board.getSquare(newRow, newCol);
				if (newSquare.kind != Type.ROOM) {
					remaining--;			//decrement remaining allowed move steps
					ui.suggestBtn.setEnabled(false);
				} else {		
					//entered a room...
					ui.suggestBtn.setEnabled(true);
					remaining = 0;		//set to 0 so player turn ends
					InputOutput.popup("Make a Choice", "You may now make a suggestion OR choose to end your turn");
				}
				
				if (!ui.suggestBtn.isEnabled()) {	//if can suggest, wait for 'end turn' OR suggest; else continue here
					if (remaining <=0) {
						evaluate();
					}
				} else {
					ui.status.setText("You now have the option of making a suggestion");
				}
			} else {
				ui.status.setText("INVALID: You cannot move at: "+newRow+" , "+newCol);
			}
		}
	}
	
	/**
	 * Updates visited set during current turn 
	 * </br>Assume that newRow,newCol is always valid
	 * @param oldRow -- row to move to
	 * @param oldCol -- column to move to
	 */
	private void updateVisited(int oldRow, int oldCol) {
		Square sqr = board.getSquare(oldRow, oldCol);
		if (sqr.kind != Type.ROOM) {		//if square is not room
			ui.visited.add(new int []{oldRow, oldCol});	//add this Square's coordinate
		} else {		//if it's a room
			String roomName = sqr.getName();
			for (int row = 0; row < rows; row++) {	//for all squares
				for (int col = 0; col < cols; col++) {
					//add all Squares in the same room i.e. you can't go back to the room you just left, in the same turn
					if (board.getSquare(row, col).getName().equals(roomName)) {
						ui.visited.add(new int []{row, col});
					}
				}
			}
			
		}
		
	}

	//ADDED GETTERS FOR GUI
	//current Player ID
	public int getCurrent() {	
		return current;
	}
	
	//remaining move steps for current player
	public int getRemaining() {	
		return remaining;
	}
	
	//room to weapon mappings
	//some rooms may not have a weapon in them
	public Map<String, String> weapons() {
		return weaponsInRoom;
	}
	
	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		new Cluedo();
	}

}