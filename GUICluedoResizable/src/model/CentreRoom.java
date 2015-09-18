package model;

import java.util.List;

import model.Card.Type;

/**
 * A CentreRoom is a ""Room"" which contains the solution in the game
 * @author  Ronni Perez
 *
 */
public class CentreRoom {
	//these are long names of the person, weapon and room
	private String suspect;	//self explanatory
	private String weapon;
	private String place;

	/**
	 * Supplementary method used by Loader.pickSolution() to put appropriate solution in
	 * @param cards -- ALWAYS have 3 elements
	 */
	public void putInEnvelope(List<Card> cards) {
		for (Card c: cards) {
			if (c.kind == Type.CHARACTER) {
				this.suspect = c.name;
			} else if (c.kind == Type.WEAPON) {
				this.weapon = c.name;
			} else if (c.kind == Type.ROOM) {
				this.place = c.name;
			}
		}
	}

	//GETTERS
	public String getSuspect() {
		return suspect;
	}

	public String getWeapon() {
		return weapon;
	}

	public String getPlace() {
		return place;
	}
}