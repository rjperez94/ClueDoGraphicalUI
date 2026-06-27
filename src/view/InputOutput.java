package view;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import main.Loader;

/**
 * Handles all user input in the game and any output that is not directly associated with the main GUI
 * </br> This is a stand-alone class
 * @author Ronni Perez
 *
 */
public class InputOutput {
	
	/**
	 * Ask user for number of players in game
	 * @return -- integer denoting the number of players
	 */
	public static int numPlayers() {
		Object[] options = {3, 4, 5, 6};		//choices
		//result
		Object num = JOptionPane.showInputDialog(null, "How many players?", "Number of players", JOptionPane.QUESTION_MESSAGE, null, options, null);
		if(num == null) {		//if close/cancel pressed
			int r = yesOrNo("Confirm Exit","Are you sure you want to exit?");	//ask user
			if (r == JOptionPane.YES_OPTION) {	//user cancelled input, so exit game
				popup("App Closing", "The game will now close because you cancelled player number input");
				System.exit(0); 		
			} else {		//user decided to play on
				return numPlayers();
			}
		}
		return (int)(num);
	}
	
	/**
	 * Fill up names with Player names
	 * @param players -- number of players in game
	 * @return String [] with Player1's name at arr[1], Player2's name at arr[2]
	 */
	public static String[] playerNames(int players) {
		String [] arr = new String[players+1];		//if N spaces needed, create N+1
		//NOTE: index 0 will always be null
        
		for (int ID = 1; ID <= players; ID++) {
			String name = null;		//hold name value for Player i
			while (name == null || !isValidName(name)) {		
				//1st condition in while: user cannot cancel or close dialog
				//2nd condition in while: is name valid
				do {		//ask name for this player with ID until we are given one or system quits
					name =  JOptionPane.showInputDialog(null, "Enter Player "+ID+"'s name/alias:", "Player Names", JOptionPane.QUESTION_MESSAGE);
					if (name == null) {		//if cancelled or clicked closed
						int r = yesOrNo("Confirm Exit","Are you sure you want to exit?");	//ask user
						if (r == JOptionPane.YES_OPTION) {	//user cancelled input, so exit game
							popup("App Closing", "The game will now close because you cancelled player "+ID+" name input");
							System.exit(0);
						}
					}
				} while (name == null);	
				
				//ensure  name uniqueness by adding Player ID at start of name i.e. 1Ronni
				//will be used by namesToCharacter map later so this needs to be unique
				arr[ID] = ID+name;
			}
		}
		return arr;
	}
	
	/**
	 * Checks if user's name is valid i.e.
	 * </br>at LEAST 1 letter AND ONLY letters and spaces allowed
	 * @param name -- String that a player enters as their name
	 * @return true iff name meets requirements
	 */
	private static boolean isValidName(String name) {
		int i = 0;
		while (i < name.length()) {		//for each letter
			if ((""+name.charAt(i)).equals(" ")) {		//check for space
				 i++;	
			} else {		//has letter, so inspect
				return name.matches("^[\\p{L} .'-]+$");	//checks for numbers
			}
		}
		//if it gets here, then it ONLY has spaces, no letter
		return false;
	}
	
	/**
	 * Setup JRadio buttons for character selection
	 * @param playerNames -- String [] from playerNames(N)
	 * @return a Map Map<String, String> of Player name to character
	 */
	public static Map<String, String> playerChars(String[] playerNames) {
		JPanel panel = new JPanel(new GridLayout(0, 1));	//window
		panel.setVisible(true);
		
		ButtonGroup group = new ButtonGroup();		//holds radio to one group so no multiple selections
		
		List<JRadioButton> options = new ArrayList<JRadioButton>();		//radio holder
		for (String s: Loader.getPeople()){		//populate options
			JRadioButton radio = new JRadioButton(s);	
			radio.setActionCommand(s);		//set action cmd to eliminate the need for a loop later as to which is chosen
			options.add(radio);	//add to radio holder
			group.add(radio);	//add to group
			panel.add(radio);		//add to window
		}
		
		//ensures that at least one radio is selected by default
		options.get(new Random().nextInt(options.size())).setSelected(true);
		
		//do the assignment
		return assignChars(playerNames, options, panel, group);	
	}
	
	/**
	 * Assigns Player name to character mappings for each player
	 * @param playerNames -- String [] from playerNames(N)
	 * @param options -- List<JRadioButton> the radio buttons
	 * @param panel -- this JPanel / window
	 * @param group -- the button group that options param belong to
	 * @return a Map<String, String> of Player name to character
	 */
	private static Map<String, String> assignChars(String[] playerNames, List<JRadioButton> options, 
			JPanel panel, ButtonGroup group) {
		Map<String, String> nameToChar =  new HashMap<String, String>();		//mapping holder
		for (int ID = 1; ID < playerNames.length; ID++) {		//do this for all players
			//inform user, add this to window
			JLabel info = new JLabel("Player '"+playerNames[ID].substring(1)+"', please choose you character from above");
			panel.add(info);
			
			//brings up window for this player with ID to choose character
			askForCharacter(panel, ID);
			
			//get selected character
			String character = group.getSelection().getActionCommand();
			nameToChar.put(playerNames[ID], character);		//put to name to character in Map
			disableRadio(options, character);		//disable this radio for next player
			
			//remove info message from window
			panel.remove(info);
		}
		//return Map
		return nameToChar;
	}
	
	/**
	 * Brings up window for this player with ID to choose character
	 * @param panel -- the window with radio button etc
	 * @param ID -- the Player ID
	 */
	private static void askForCharacter(JPanel panel, int ID) {
		int choice = JOptionPane.CLOSED_OPTION;		//default response value so it goes into while loop
		while (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.CANCEL_OPTION) {	
			//repeat until this Player ID has chosen  a character or system quits
			choice = JOptionPane.showOptionDialog(null, panel,			//output the window as dialog
			    "Choose Your Character", JOptionPane.OK_CANCEL_OPTION,
			    JOptionPane.QUESTION_MESSAGE, null, null, null);
			if (choice == JOptionPane.CLOSED_OPTION || choice == JOptionPane.CANCEL_OPTION) {		
				//if cancelled or clicked closed
				int r = yesOrNo("Confirm Exit","Are you sure you want to exit?");	//ask user
				if (r == JOptionPane.YES_OPTION) {	//user cancelled input, so exit game
					popup("App Closing", "The game will now close because you cancelled player "+ID+" character input");
					System.exit(0);
				}
			}
		}
	}

	/**
	 * Disables selected radio button from assignChars() loop
	 * @param options --  List<JRadioButton> the radio buttons
	 * @param name -- the name of the radio selected
	 */
	private static void disableRadio(List<JRadioButton> options, String radioName) {
		for (JRadioButton radio: options){		//inspect all radio buttons
			//CONDITION1: disable selected radio
			//CONDITION2: STILL disable previously disabled radios from previous calls
			if (radio.getText().equals(radioName) || !radio.isEnabled()) {
				radio.setSelected(false);
				radio.setEnabled(false);
			} else {		//ensures that at least one radio is selected by default. Select one which is enabled
				radio.setSelected(true);
			}
		}
	}
	
	//HELPER METHODS
	/**
	 * Brings up a dialog where the choices are YES or NO
	 * @param title -- the title string for the dialog
	 * @param msg -- the String to display
	 * @return an int indicating the option selected by the user; YES_OPTION or NO_OPTION
	 */
	public static int yesOrNo(String title, String msg) {
		return JOptionPane.showConfirmDialog(null, msg, title, JOptionPane.YES_NO_OPTION);
	}
	
	/**
	 * Prompts the user for input in a blocking dialog where the possible selections are put in either
	 * </br> a single-select scheme. It is up to the Java UI to decide how best to represent the selectionValues, 
	 * </br>but usually a JComboBox OR a JList will be used
	 * @param title -- the title string for the dialog
	 * @param msg -- the String to display
	 * @param options -- an array of Strings that gives the possible selections
	 * @return user's String input, or null meaning the user canceled the input
	 */
	public static String getString(String title, String msg, String [] options) {
		Object name =  JOptionPane.showInputDialog(null, msg, title, JOptionPane.QUESTION_MESSAGE, null, options, null);
		if (name == null) { return null;	}
		
		return (String)(name);
	}
	
	//OUTPUT -- the rest are all inputs
	/**
	 * Brings up a dialog that displays a message using an information message icon
	 * @param title -- the title string for the dialog
	 * @param msg -- the String to display
	 */
	public static void popup(String title, String msg) {
		JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
	}
}
