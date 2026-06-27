package view;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import main.Cluedo;
import main.Loader;
import model.Board;
import model.Square;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;
import javax.swing.JPanel;

/**
 * The Game GUI
 * @author Ronni Perez
 *
 */
public class GUI implements WindowListener, MouseListener,KeyListener{	
	private Cluedo game;
	private Board board;
	
	private JLabel remaining;		//remaining move steps --- will need to be decremented each time current Player steps
	
	public Set<int[]> visited;	//visited set of Squares during one given turn in row,col
	public JLabel playerTurn; // texts in GUI
	public JLabel characterLbl;		//character played by 'current' Player
	public JLabel diceRoll;		//total dice roll
	public JLabel status;		//status bar used mainly for hovering and INVALID msgs
	
	//These btns will need to be disabled depending on game state
	public JButton suggestBtn;
	public JButton endBtn;

	private JFrame frame; // the window
	private JPanel bottomPanel;		//bottom portion of window
	private GUICanvas boardCanvas; // the canvas.draw() will need to be called at certain times
	

	public GUI(Cluedo cluedo) {
		this.game = cluedo;
		setupGUI();		//setup GUI elements
	}

	/**
	 * Assign a game board to this GUI
	 * @param board
	 */
	public void setBoard(Board board) {
		this.board = board;
		this.boardCanvas.setBoard(board);
	}

	/**
	 * Setup GUI components
	 */
	private void setupGUI() {
		frame = new JFrame("Cluedo v4 by Ron Perez");		//gui window, resizable by default
		frame.setMinimumSize(new Dimension(545, 795)); //minimum window size
		frame.getContentPane().setLayout(null); // use absolute layout

		//gui components
		setupMenu();
		setupCanvas();
		
		bottomPanel = new JPanel();	//bottom-half holder
		bottomPanel.setLayout(null);		//use absolute layout
		bottomPanel.setBounds(0, 545, 538, 197);		//set location, dynamic
		frame.getContentPane().add(bottomPanel);
		
		//add buttons and labels to bottom-half
		setupButtons();
		setupLabels();

		// don't exit on close, ask first
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
		frame.setVisible(true); // show window
		//manage resize
		frame.addComponentListener(new ComponentAdapter() {  
		        public void componentResized(ComponentEvent e) {
		        	setReady(false);		//not ready to draw
		            repositionAndResize();	//resize and reposition
		            if (game.loaded) {		//if loaded (and ready)
		            	setReady(true);	//set ready and redraw
		            }
		        }
		});
		
		frame.addWindowListener(this);		//listens to window events and key events
		frame.addKeyListener(this);
		frame.requestFocus();		//request focus
	}

	private void repositionAndResize() {
		bottomPanel.setBounds(0, frame.getHeight()-255, bottomPanel.getWidth(), bottomPanel.getHeight());
		boardCanvas.setBounds(0, 0, frame.getWidth(), bottomPanel.getY()-5);
		
		int newCanvasWidth = boardCanvas.getWidth();
		int newCanvasHeight = boardCanvas.getHeight();
		boardCanvas.setSquareWidth(newCanvasWidth/Loader.cols());
		boardCanvas.setSquareHeight(newCanvasHeight/Loader.rows());
	}

	/**
	 * Setup GUI text outputs using various JLabel components
	 */
	private void setupLabels() {
		//You rolled 'n'
		diceRoll = new JLabel();
		//font
		diceRoll.setFont(new Font("Times New Roman", diceRoll.getFont()
				.getStyle() | Font.BOLD, diceRoll.getFont().getSize() + 8));
		diceRoll.setBounds(10, 550-bottomPanel.getY(), 122, 23);
		bottomPanel.add(diceRoll);
		
		//It's playerName's turn
		playerTurn = new JLabel();
		//font
		playerTurn.setFont(playerTurn.getFont().deriveFont(
				playerTurn.getFont().getStyle() | Font.BOLD,
				playerTurn.getFont().getSize() + 5f));
		playerTurn.setBounds(135, 583-bottomPanel.getY(), 311, 23);
		bottomPanel.add(playerTurn);
		
		//Go playerCharacter
		characterLbl = new JLabel();
		//font
		characterLbl.setFont(characterLbl.getFont().deriveFont(
				characterLbl.getFont().getStyle() | Font.BOLD,
				characterLbl.getFont().getSize() + 14f));
		characterLbl.setBounds(117, 597-bottomPanel.getY(), 311, 40);
		bottomPanel.add(characterLbl);
		
		//'N' -- this decrements each move step
		remaining = new JLabel();
		//centre text
		remaining.setHorizontalAlignment(SwingConstants.CENTER);
		//font
		remaining.setFont(remaining.getFont().deriveFont(
				remaining.getFont().getStyle() | Font.BOLD,
				remaining.getFont().getSize() + 32f));
		remaining.setBounds(24, 588-bottomPanel.getY(), 50, 50);
		bottomPanel.add(remaining);
		
		//info to user
		JLabel notice = new JLabel("Click the circles OR use arrow keys to move");
		//font
		notice.setFont(notice.getFont().deriveFont(
				notice.getFont().getStyle() & ~Font.BOLD | Font.ITALIC));
		notice.setBounds(127, 634-bottomPanel.getY(), 301, 40);
		bottomPanel.add(notice);
		
		//bottom bar
		//INVALID MOVE or MOUSE HOVER
		status = new JLabel();
		//font
		status.setFont(status.getFont().deriveFont(
				status.getFont().getStyle() | Font.ITALIC,
				status.getFont().getSize() + 5f));
		status.setForeground(new Color(160, 82, 45));		//text color
		status.setBounds(0, 722-bottomPanel.getY(), 538, 20);
		bottomPanel.add(status);
	}

	/**
	 * Setup GUI buttons
	 */
	private void setupButtons() {
		//cards in hand
		JButton cardsBtn = new JButton("Your Cards");
		cardsBtn.setToolTipText("Show a list of the cards that you currently hold.");
		cardsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {	//shows cards
				board.getPlayer(game.getCurrent()).showCards();
				frame.requestFocus();		//request focus
			}
		});
		cardsBtn.setBounds(426, 563-bottomPanel.getY(), 101, 20);
		bottomPanel.add(cardsBtn);
		
		//end turn
		endBtn = new JButton("End Turn");
		endBtn.setEnabled(false);
		endBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {	//sets remaining to 0
				game.makeChoice(0); // end turn
				frame.requestFocus();		//request focus
			}
		});
		endBtn.setBounds(438, 622-bottomPanel.getY(), 89, 35);
		bottomPanel.add(endBtn);
		
		//detective notes
		JButton seenBtn = new JButton("Cards Seen");
		seenBtn.setToolTipText("Show a list of the cards that you have seen during the game.");
		seenBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {	//show seen cards
				board.getPlayer(game.getCurrent()).seenCards();
				frame.requestFocus();		//request focus
			}
		});
		seenBtn.setBounds(426, 593-bottomPanel.getY(), 101, 20);
		bottomPanel.add(seenBtn);
		
		//accuse
		JButton accuseBtn = new JButton("Accuse Someone");
		accuseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {	//accuse someone by passing arg 1
				game.makeChoice(1);
				frame.requestFocus();		//request focus
			}
		});
		accuseBtn.setBounds(117, 680-bottomPanel.getY(), 142, 23);
		bottomPanel.add(accuseBtn);
		
		//suggest
		suggestBtn = new JButton("Make a Suggestion");
		suggestBtn.setEnabled(false); // disable at start of game
		suggestBtn.addActionListener(new ActionListener() {	//suggest by passing arg 2
			public void actionPerformed(ActionEvent event) {
				game.makeChoice(2);
				frame.requestFocus();		//request focus
			}
		});
		suggestBtn.setBounds(273, 680-bottomPanel.getY(), 163, 23);
		bottomPanel.add(suggestBtn);
	}

	/**
	 * Sets up canvas to draw Board on
	 */
	private void setupCanvas() {
		//this means that each Square width x height === 30 x 30
		//this would depend on the assets/board.png file
		boardCanvas = new GUICanvas(this, 30, 30);
		
		boardCanvas.setBackground(Color.WHITE);
		boardCanvas.setBounds(0, 0, 538, 539);
		frame.getContentPane().add(boardCanvas);
		//add this as mouse listener to for board canvas
		//I put this here because it would be cleaner for code access
		//as this class serves as intermediary between boardCanvas and game state
		boardCanvas.addMouseListener(this);
	}

	/**
	 * Set up menu bar for GUI
	 */
	private void setupMenu() {
		JMenuBar menuBar = new javax.swing.JMenuBar();	//menu bar
		frame.setJMenuBar(menuBar);
		
		JMenu file = new javax.swing.JMenu();	//menu item 1
		file.setText("File");
		menuBar.add(file);
		
		//item 1.1
		JMenuItem fileNew = new JMenuItem("New Game");
		fileNew.setToolTipText("This would start a new game");
		fileNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//ask user first
				int r = InputOutput.yesOrNo("New Game","All progress will be lost. Do you want to start a new game");
				if (r == JOptionPane.YES_OPTION) {
					game.reset();
				}
			}
		});
		file.add(fileNew);
		
		//item 1.2
		JMenuItem fileExit = new JMenuItem("Quit");
		fileExit.setToolTipText("Exit application");
		fileExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				windowClosing(null);		//same event as this
			}
		});
		file.add(fileExit);
		
		JMenu play = new javax.swing.JMenu();	//menu item 2
		play.setText("Game");
		menuBar.add(play);
		
		//item 2.1
		JMenuItem gameShowCards = new JMenuItem("Show Cards");
		gameShowCards
				.setToolTipText("Show a list of the cards that you currently hold.");
		gameShowCards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				board.getPlayer(game.getCurrent()).showCards();
			}
		});
		play.add(gameShowCards);
		
		//item 2.2
		JMenuItem showSeen = new JMenuItem("Detective Notes");
		showSeen.setToolTipText("Show a list of the cards that you have seen during the game.");
		showSeen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				board.getPlayer(game.getCurrent()).seenCards();
			}
		});
		play.add(showSeen);
		
		JMenu help = new javax.swing.JMenu();	//menu item 
		help.setText("Help");
		menuBar.add(help);
		
		// item 3.1
		JMenuItem keyMapping = new JMenuItem("Keyboard Mappings");
		keyMapping.setToolTipText("Show short-cut keys.");
		keyMapping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				showKeys();
			}
		});
		help.add(keyMapping);
	}
	
	/**
	 * Show keyboard shortcuts using JOptionPane.showMessageDialog
	 */
	public void showKeys() {
		StringBuilder text = new StringBuilder();
		text.append("The following are the key mappings for this application: \n");

		text.append("\nPlayer Movement: \n");
		text.append("Arrow keys: {Up, Down, Left, Right} \n");
		
		text.append("\nFile Menu Shortcuts: \n");
		text.append("Shift+N -> New Game\nShift+Q -> Quit\n");
		
		text.append("\nIn-game Shortcuts: \n");
		text.append("A -> Accuse Someone\nS -> Make A Suggestion \n");
		text.append("E -> End Turn\nC -> Cards in Hand\n");
		text.append("D -> Cards Seen a.k.a. Detective Notes\n");
		
		text.append("\nYou can access this again by going to Help -> Keyboard Mappings");
		InputOutput.popup("ShortCut Keys",  text.toString());
	}

	/**
	 * Updates remaining step output and repaint GUI
	 * </br> Called at first move and after every move in a given turn
	 */
	public void draw() {
		Color fontColor = board.getPlayer(game.getCurrent()).color;	//player token color
		if (fontColor  != Color.WHITE) {	//if not white
			remaining.setForeground(board.getPlayer(game.getCurrent()).color);		//same color as current
		} else {		//if white, can't see, so change
			remaining.setForeground(Color.WHITE.darker().darker().darker());	//gray
		}
		remaining.setText(" " + game.getRemaining());		//update remaining move steps available
		boardCanvas.repaint();		//redraw
	}
	
	/**
	 * Set isReady to true iff all GAME and GUI components are ready to be drawn
	 * @param b
	 */
	public void setReady(boolean b) {
		boardCanvas.isReady = b;
		if(b) {
			this.draw();
		}
	}

	/**
	 * Board column given boardCanvas x on mouse click and hover 
	 * @param xcoord -- boardCanvas x (actually mouse x)
	 * @return
	 */
	public int getBoardColFromX(int xcoord) {
		double bx = xcoord;
		bx /= boardCanvas.getSquareWidth();
		return (int) bx;
	}

	/**
	 * Board row given boardCanvas y on mouse click and hover 
	 * @param ycoord -- boardCanvas y (actually mouse y)
	 * @return
	 */
	public int getBoardRowFromY(int ycoord) {
		double by = ycoord;
		by /= boardCanvas.getSquareHeight();
		return (int) by;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int row = getBoardRowFromY(e.getY());		//new move of current to row,col
		int col = getBoardColFromX(e.getX());
		
		//ANIMATE MOVE if valid move or NOT using stairwells ONLY
		animate(row,col);
		
		//update current player's location , then redraw
		game.chooseMove(row, col);	
		draw();
		frame.requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		status.setText("");		//clear status text
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode(); // key released
		switch (keyCode) { // if arrow keys
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_UP: 
		case KeyEvent.VK_KP_DOWN: 
		case KeyEvent.VK_KP_LEFT: 
		case KeyEvent.VK_KP_RIGHT: 
			moveKey(keyCode);
			return;
		default: // else, use key assignments
			String ch = ("" + e.getKeyChar()).toLowerCase();
			gameCommand(ch);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		String ch = (""+e.getKeyChar()).toLowerCase();
		if (e.isShiftDown()) {	//is the Control modifier down 
			windowCommand(ch);
		}
	}

	/**
	 * Execute method based on key released. This equivalent to file-menu commands ONLY
	 * @param ch -- lowercase String equivalent of key pressed
	 */
	private void windowCommand(String ch) {
		if (ch.equals("n")) {		//new
			//ask user first
			int r = InputOutput.yesOrNo("New Game","All progress will be lost. Do you want to start a new game");
			if (r == JOptionPane.YES_OPTION) {
				game.reset();
			}
		} else if (ch.equals("q")) {	//quit
			windowClosing(null);
		} 
	}

	/**
	 * Execute method based on key released. This is for in-game commands ONLY
	 * @param ch -- lowercase String equivalent of key pressed
	 */
	private void gameCommand(String ch) {
		if (ch.equals("a")) {		//accuse
			game.makeChoice(1);
		} else if (ch.equals("s")) {	//suggest
			if (suggestBtn.isEnabled()) {
				game.makeChoice(2);
			}
		} else if (ch.equals("e")) {	//end
			if(endBtn.isEnabled()) {
				game.makeChoice(0);
			}
		} else if (ch.equals("c")) {	//cards in hand
			board.getPlayer(game.getCurrent()).showCards();
		} else if (ch.equals("d")) {	//detective notes
			board.getPlayer(game.getCurrent()).seenCards();
		} 
	}

	/**
	 * Move the current Player via keyEvent release
	 * @param keyCode -- the integer keyCode associated with the key in the keyEvent.
	 */
	private void moveKey(int keyCode) {
		//current loc of current player
		int oldRow = board.getPlayer(game.getCurrent()).getLocation().row;
		int oldCol = board.getPlayer(game.getCurrent()).getLocation().col;
		//animate,move, redraw
		switch (keyCode) { // if arrow keys
		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
			animate(oldRow-1,oldCol);
			game.chooseMove(oldRow-1, oldCol); 
			draw();
			return;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN: 
			animate(oldRow+1,oldCol);
			game.chooseMove(oldRow+1, oldCol); 
			draw();
			return;
		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT: 
			animate(oldRow,oldCol-1);
			game.chooseMove(oldRow, oldCol-1); 
			draw(); 
			return;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT: 
			animate(oldRow,oldCol+1);
			game.chooseMove(oldRow, oldCol+1);
			draw();
		}
	}
	
	/**
	 * Animate the current Player's movement to row,col
	 * </br> this may not be valid so inspect first
	 * @param row
	 * @param col
	 */
	private void animate(int row, int col) {
		try {
			if (board.isValid(game.getCurrent(), row, col, visited)) {		//animate only if valid move	
				Square oldLoc = board.getPlayer(game.getCurrent()).getLocation();
				Square newLoc = board.getSquare(row, col);
				//animate only playarea to playarea movement, not involving room
				if (oldLoc.kind == Square.Type.PLAYAREA && newLoc.kind == Square.Type.PLAYAREA) {
					board.getPlayer(game.getCurrent()).animate(boardCanvas.getGraphics(), boardCanvas, row, col);
				}
			}
		} catch (InterruptedException err) {err.printStackTrace();}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// Ask the user to confirm they wanted to do this
		int r = InputOutput.yesOrNo("Confirm Exit","Are you sure you want to exit?");
		if (r == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	//GETTERS
	public int getCurrent () {
		return game.getCurrent();
	}
	
	public int getRemaining () {
		return game.getRemaining();
	}
	
	public Map<String, String> getWeapons() {
		return game.weapons();
	}
	
	//TODO		//USELESS METHODS HERE TO FULFILL INTERFACE

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
