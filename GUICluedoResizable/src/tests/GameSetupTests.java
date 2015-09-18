package tests;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.Loader;
import model.Board;
import model.Card;
import model.Card.Type;
import model.CentreRoom;
import model.Player;
import model.Square;

/**
 * Test Game Setup specifically Board, Square, Player ad their Cards
 * @author Ronni Perez
 *
 */
public class GameSetupTests {
	@Test	public void validPlayareaNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square playarea = board.getSquare(4, 5);	//get square
		assertTrue(playarea.hasNeigbour(3, 5));	//above
		assertTrue(playarea.hasNeigbour(5, 5));	//below
		assertTrue(playarea.hasNeigbour(4, 4));	//left
		assertTrue(playarea.hasNeigbour(4, 6));	//right
	}
	
	@Test	public void validPlayareaNeigbour2() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square playarea = board.getSquare(0, 5);	//get square @ label "th"
		/*
		 * 03 04 05 06 07
00 ..... KC KC  th BR BR
01 ..... KC KC __ BR BR
		 */
		assertTrue(playarea.hasNeigbour(1, 5));	//below and right ONLY
		assertTrue(playarea.hasNeigbour(0, 6));

		assertFalse(playarea.hasNeigbour(0, 4));	//not left	i.e. not a doorway to KC
		assertFalse(playarea.hasNeigbour(1, 4));	//not south west diagonal i.e. not a  doorway to KC
		assertFalse(playarea.hasNeigbour(1, 6));	//not south east diagonal  i.e. not a  doorway to BR
	}
	
	@Test	public void invalidPlayareaNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square playarea = board.getSquare(12, 2);	//get square
		assertFalse(playarea.hasNeigbour(11, 1));	//not itself
		assertFalse(playarea.hasNeigbour(11, 1));	//not north west diagonal
		assertFalse(playarea.hasNeigbour(13, 1));	//not south west diagonal
		assertFalse(playarea.hasNeigbour(11, 3));	//not north east diagonal
		assertFalse(playarea.hasNeigbour(13, 3));	//not south east diagonal
	}
	
	@Test	public void invalidCentreRoomNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square centre = board.getSquare(8, 9);	//get 'CR' square
		//should have no neighbors
		assertFalse(centre.hasNeigbour(7, 9));	//not above
		assertFalse(centre.hasNeigbour(9, 9));	//not below
		assertFalse(centre.hasNeigbour(8, 8));	//not left
		assertFalse(centre.hasNeigbour(8, 10));	//not right
	}
	
	@Test	public void validDoorwayNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square dw = board.getSquare(3, 3);	//get doorway 'dw' to Kitchen (KC)
		/*
		 * 02 03 04
		 	.............
02 .....  KC KC KC 
03 .....  __ dw __ 
04 .....  __ __ __ 
		 */
		assertTrue(dw.hasNeigbour(2, 3));	//above (to KC)
		assertTrue(dw.hasNeigbour(4, 3));	//below
		assertTrue(dw.hasNeigbour(3, 2));	//left
		assertTrue(dw.hasNeigbour(3, 4));	//right
	}
	
	@Test	public void invalidDoorwayNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square dw = board.getSquare(3, 3);	//get doorway 'dw' to Kitchen (KC)
		/*
		 * 02 03 04
		 	.............
02 .....  KC KC KC 
03 .....  __ dw __ 
04 .....  __ __ __ 
		 */
		assertFalse(dw.hasNeigbour(4, 2));	//not south west diagonal
		assertFalse(dw.hasNeigbour(4, 4));	//not south east diagonal
	}
	
	@Test	public void validRoomNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square rm = board.getSquare(13, 7);	//get room Hall (HL)
		/*
		 * 07 08 09 10 11 12 13 
	 		..................................
12 ...... __ __ dw __ __ __ __
13 ...... HL HL HL HL HL __ __
14 ...... HL HL HL HL HL dw __
15 ...... HL HL HL HL HL __ __
		 */
		//all HL's have these 'dw' as neighbors
		//i.e. can only get out of room through its 'dw'
		assertTrue(rm.hasNeigbour(12, 9));	//front HL dw
		assertTrue(rm.hasNeigbour(14, 12));	//right HL dw
	}
	
	@Test	public void invalidRoomNeigbour() {
		Board board = new Board(new Loader(18, 18).getCoords(), 0);
		Square rm = board.getSquare(13, 7);	//get room Hall (HL)
		/*
		 *	06 07 08 09 10 11 12 13 
	 		......................................
12 ...... __ __ __ dw __ __ __ __
13 ...... __ HL HL HL HL HL __ __
14 ...... __ HL HL HL HL HL dw __
15 ...... __ HL HL HL HL HL __ __
		 */
		//all HL's have these 'dw' as neighbors
		//i.e. can only get out of room through its 'dw'
		assertFalse(rm.hasNeigbour(13, 7));	//not itself
		assertFalse(rm.hasNeigbour(13, 6));	//not left
		assertFalse(rm.hasNeigbour(12, 7));	//not above
		assertFalse(rm.hasNeigbour(12, 6));	//not north west diagonal
	}
	
	@Test	public void validPlayerLoc() {
		Board board = new Board(new Loader(18, 18).getCoords(), 6);		//6 players
		//P1 and so on
		assertTrue(board.getPlayer(1).getLocation().equals(board.getSquare(17, 5)) );
		assertTrue(board.getPlayer(2).getLocation().equals(board.getSquare(12, 0)) );
		assertTrue(board.getPlayer(3).getLocation().equals(board.getSquare(0, 6)) );
		assertTrue(board.getPlayer(4).getLocation().equals(board.getSquare(9, 17)) );
		assertTrue(board.getPlayer(5).getLocation().equals(board.getSquare(0, 11)) );
		assertTrue(board.getPlayer(6).getLocation().equals(board.getSquare(13, 17)) );
	}
	
	@Test	public void invalidPlayerLoc() {
		Board board = new Board(new Loader(18, 18).getCoords(), 6);		//6 players
		//P1 and so on
		assertFalse(board.getPlayer(1).getLocation().equals(board.getSquare(11, 5)) );
		assertFalse(board.getPlayer(2).getLocation().equals(board.getSquare(15, 14)) );
		assertFalse(board.getPlayer(3).getLocation().equals(board.getSquare(5, 15)) );
		assertFalse(board.getPlayer(4).getLocation().equals(board.getSquare(9, 12)) );
		assertFalse(board.getPlayer(5).getLocation().equals(board.getSquare(10, 17)) );
		assertFalse(board.getPlayer(6).getLocation().equals(board.getSquare(1, 17)) );
	}
	
	@Test	public void invalidPlayerSpawn() {
		Board board = new Board(new Loader(18, 18).getCoords(), 4);		//4 players
		//P5 and P6 should NOT have any mappings
		assertEquals(board.getPlayer(5), null);
		assertEquals(board.getPlayer(6), null);
	}
	
	@Test	public void invalidPlayerSpawn2() {
		Board board = new Board(new Loader(18, 18).getCoords(), 3);		//4 players
		//P4 and P5 and P6 should NOT have any mappings
		assertEquals(board.getPlayer(4), null);
		assertEquals(board.getPlayer(5), null);
		assertEquals(board.getPlayer(6), null);
	}
	
	@Test	public void validSolution() {
		CentreRoom solution = new CentreRoom();
		
		Card c1 = new Card(Type.ROOM, "Lounge");
		Card c2 = new Card(Type.CHARACTER, "Miss Scarrlet");
		Card c3 = new Card(Type.WEAPON, "Dagger");
		
		ArrayList<Card>list = new ArrayList<Card>();
		list.add(c1);
		list.add(c2);
		list.add(c3);
		solution.putInEnvelope(list);
		
		assertEquals(solution.getWeapon(), "Dagger");
		assertEquals(solution.getPlace(), "Lounge");
		assertEquals(solution.getSuspect(), "Miss Scarrlet");
	}
	
	//turn off / comment out delay in Player.hasCard(name) when testing this
	@Test	public void validCards() {
		Board board = new Board(new Loader(18, 18).getCoords(), 1);	//1 player only
		Player p = board.getPlayer(1);
		List<Card> cards = new Loader(18, 18).populateCards();
		List<String> p1Cards = new ArrayList<String>();
		Collections.shuffle(cards);
		
		for (int i=0; i < cards.size(); i+=2) {
			Card c = cards.get(i);
			p.addCard(c);
			p1Cards.add(c.name);
		}
		
		for (String name: p1Cards) {		//given the name, player should have card
			assertNotEquals(p.hasCard(name, true), null);		//turn off / comment out delay in Player.hasCard(name) when testing this
		}
	}
	
	@Test	public void invalidCards() {
		Board board = new Board(new Loader(18, 18).getCoords(), 1);	//1 player only
		Player p = board.getPlayer(1);
		List<Card> cards = new Loader(18, 18).populateCards();
		
		for (int i=0; i < cards.size(); i+=2) {
			Card c = cards.remove(i);		//remove from cards collection
			p.addCard(c);		//give to player
		}
		//now all Cards in collection are the cards that are left
		for (Card c: cards) {		//given the name, player should NOT have card
			assertEquals(p.hasCard(c.name, true), null);
		}
	}
	
}
