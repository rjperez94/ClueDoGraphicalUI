package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import model.Card;
import model.CentreRoom;
import model.Card.Type;

/**
 * Loads necessary files and characters, weapons etc ready for parsing by different classes
 * @author Ronni Perez
 *
 */
public class Loader {
	private String[][] coords;			//string representation of the board
	private static String[] people;		//characters in the game
	private static String[] weapons;		//weapons in the game
	private static String[] rooms;		//rooms in the game
	private static int rows;	
	private static int cols;
	
	public Loader (int rows, int cols) {
		Loader.rows = rows;
		Loader.cols = cols;
		coords = new String[rows][cols];
		loadBoard();		//read strings from assets/board.txt
		loadCards();		//cards
	}
	
	/**
	 * Read game Board from text file
	 */
	private void loadBoard() {
		Scanner sc = new Scanner(Loader.class.getResourceAsStream("assets/board.txt"));
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				coords[i][j] = sc.next(); // put it into coords String[][]
			}
		}
		sc.close();
	}

	/**
	 * Cards from hard coded String[] 
	 * @return
	 */
	private void loadCards() {
		people = new String []{
				"Miss Scarlett","Colonel Mustard","Mrs. White","The Reverend Green","Mrs. Peacock","Professor Plum"
				};
		weapons = new String []{
				"Candlestick","Dagger","Lead Pipe","Revolver","Rope","Spanner"
				};
		rooms = new String []{
				"Kitchen",
				"Ball Room",
				"Conservatory",
				 "Billiard Room",
				 "Library",
				 "Study",
				 "Hall",
				 "Lounge",
				 "Dining Room"
				};
	}

	/**
	 * Create and return list of Card Objects
	 * Used by loadCards()
	 * @return
	 */
	public List<Card> populateCards() {
		List <Card> cards = new ArrayList<Card>();
		for (String s: people) {
			cards.add (new Card(Type.CHARACTER, s ));
		}
		for (String s: weapons) {
			cards.add (new Card(Type.WEAPON, s));
		}
		for (String s: rooms) {
			cards.add (new Card(Type.ROOM, s));
		}
		return cards;
	}
	
	public void pickSolution(CentreRoom solution, List<Card> cards) {
		Random rand = new Random();
		String person = people[rand.nextInt(people.length)];
		String tool = weapons[rand.nextInt(weapons.length)];
		String place = rooms[rand.nextInt(rooms.length)];
		
		List<Card> sol = new ArrayList<Card>();
		for (int i = 0; i<cards.size(); i++) {
			Card c = cards.get(i);
			String name = c.name;
			
			//inspect in order -- character, weapon, room
			if (name.equals(person) || name.equals(tool) || name.equals(place)) {
				sol.add(c);		//add to solution and remove card from 'pool'
				cards.remove(i);
			}
		}
		solution.putInEnvelope(sol);
	}

	/**
	 * Returns map of roomNames to weaponNames
	 * Put weapons in random rooms (1 weapon per room at most) ????
	 * -- see Section 2.4 of instructions
	 * @return
	 */
	public Map<String, String> putInRoom() {
		Map <String, String> inRoom = new HashMap<String, String>(); 
		//add loaded weapons and rooms from hardcoded names to a collection
		ArrayList<String> tools = new ArrayList<String>(Arrays.asList(weapons));
		ArrayList<String> places = new ArrayList<String>(Arrays.asList(rooms));
		//shuffle both collections
		Collections.shuffle(places);
		Collections.shuffle(tools);
		while (!tools.isEmpty()) {		//for each weapon
			String key = places.remove(0);	//remove first element in each collection
			String val = tools.remove(0);
			
			inRoom.put(key, val);		//put in map
		}
		
		while (!places.isEmpty()) {		//for the remaining rooms
			inRoom.put(places.remove(0), null);
		}
		return inRoom;
	}
	
	/*HELPER METHOD*/
	//Used by Board class to construct board game
	public String[][] getCoords() {
		return coords;
	}

	/*GETTERS*/
	public static int rows() {
		return rows;
	}
	
	public static int cols() {
		return cols;
	}
	
	public static String [] getPeople() {
		return people;
	}
	
	public static String[] getWeapons() {
		return weapons;
	}

	public static String[] getRooms() {
		return rooms;
	}
}
