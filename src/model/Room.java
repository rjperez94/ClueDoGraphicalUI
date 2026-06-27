package model;

/**
 * A room is a Square that has a name
 * @author  Ronni Perez
 */
public class Room extends Square {
	public Room(int row, int col, Type kind, String name, String code) {
		super(row, col, kind, code);
		this.name = name;
	}
}
