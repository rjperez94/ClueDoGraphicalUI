package tests;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import main.Cluedo;
import main.Loader;
import model.Board;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Test Game Play
 * @author Ronni Perez
 *
 */
public class GamePlayTests {
	@Test	public void gameStart() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Cluedo game = new Cluedo(3);	//3 players
		Field f = null;
		
		f = Cluedo.class.getDeclaredField("isWon");		//not won
        f.setAccessible(true);
        assertEquals(f.get(game).toString(), "false");
        
        f = Cluedo.class.getDeclaredField("players");		//only 3 players indeed
        f.setAccessible(true);
        assertEquals(f.get(game).toString(), "3");
        
        f = Cluedo.class.getDeclaredField("current");		//P1 always starts
        f.setAccessible(true);
        assertEquals(f.get(game).toString(), "1");
	}
	
	@Test	public void diceRoll() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Cluedo game = new Cluedo(1);		//1 player
		Method m = null;
		for (int counter = 1; counter < 1000; counter++) {		//roll die 1000 times
			m=Cluedo.class.getDeclaredMethod("roll");
	        m.setAccessible(true);
	        String str = m.invoke(game).toString();
	        int i = Integer.parseInt(str);
	        assert(i >= 1 && i <= 6);		//  1 =< remaining =< 6
		}
	}
	
	@Test	public void nextTurn() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException, SecurityException {
		Cluedo game = new Cluedo(4);		//4 players
		Field f = null;
		Method m = null;
		
		f = Cluedo.class.getDeclaredField("current"); // get current
		f.setAccessible(true);
		m = Cluedo.class.getDeclaredMethod("nextMove",boolean.class);	//get nextMove
		m.setAccessible(true);
		
		//current starts with 1 as in P1
		m.invoke(game, true);		//is testing, ..current should == 2
		assertEquals(f.get(game).toString(), "2");
		m.invoke(game, true);		//is testing, ..current should == 3
		assertEquals(f.get(game).toString(), "3");
		m.invoke(game, true);		//is testing, ..current should == 4
		assertEquals(f.get(game).toString(), "4");
		m.invoke(game, true);		//is testing, ..current should == 1
		assertEquals(f.get(game).toString(), "1");
		m.invoke(game, true);		//is testing, ..current should == 2
		assertEquals(f.get(game).toString(), "2");
		m.invoke(game, true);		//is testing, ..current should == 3
		assertEquals(f.get(game).toString(), "3");
	}
	
	@Test	public void gameOver() throws NoSuchFieldException, SecurityException, 
			IllegalArgumentException, IllegalAccessException, 
			NoSuchMethodException, InvocationTargetException {
		Cluedo game = new Cluedo(5);		//5 players
		Field f = null;
		Method m = null;
		
		f = Cluedo.class.getDeclaredField("board");		//get board
        f.setAccessible(true);
        Board board = (Board) f.get(game);
        for(int i = 1; i <= 5; i++) {		//expel all
        	board.expel(i);
        }
        m=Cluedo.class.getDeclaredMethod("gameOver");
        m.setAccessible(true);
        assertEquals(m.invoke(game).toString(), "true");		//gameOver() should be true
	}
	
	@Test	public void validMoves() {
		Board board = new Board(new Loader(18, 18).getCoords(), 3);
		/* row, col
		 * {17,5},		//starting positions for player 1
			{12,0},		//p 2
			{0,5},		//p 3
		 */
		//move P1 3 steps up
		assertTrue(board.movePiece(1, 16, 5));
		assertTrue(board.movePiece(1, 15, 5));
		assertTrue(board.movePiece(1, 14, 5));
		//move P2,  1 right, 1 left, 1 up, 1 down
		assertTrue(board.movePiece(2, 12, 1));
		assertTrue(board.movePiece(2, 12, 0));
		assertTrue(board.movePiece(2, 11, 0));
		assertTrue(board.movePiece(2, 12, 0));
		//move P3,  1 left, 2 down, then right into BR, then out to right BR doorway
		assertTrue(board.movePiece(3, 0, 5));	//1 left
		assertTrue(board.movePiece(3, 1, 5));	//1 down
		assertTrue(board.movePiece(3, 2, 5));	//1 down
		assertTrue(board.movePiece(3, 2, 6));	//right to BR
		assertTrue(board.movePiece(3, 2, 12));	//out to right BR doorway
	}
	
	@Test	public void invalidMoves() {
		Board board = new Board(new Loader(18, 18).getCoords(), 1);
		/* row, col
		 * {17,5},		//starting position for player 1
		 */
		//try to move P1
		assertFalse(board.movePiece(1, 100, 100));		//out of bounds should not work, same position
		assertFalse(board.movePiece(1, -100, -100));
		
		assertFalse(board.movePiece(1, 17, 3));		//move to room LN w/o using doorway should not work, same position
		assertFalse(board.movePiece(1, 15, 5));		//move 2 steps should not work, same position
	}
}
