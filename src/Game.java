import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;

public class Game
{
	@SuppressWarnings("serial")
	class Console extends JTextArea
	{	
		static final String DEFAULT_OUTPUT = "";
		
		Console(int rows, int cols, int fontSize, KeyListener input, String output)
		{
			super(rows, cols);
			
			int marginWidth = fontSize/2;
			Font font = new Font("consolas", Font.PLAIN, fontSize);
			
			setFont(font);
			setEditable(false);
			setBackground(Color.BLACK);
			setForeground(Color.LIGHT_GRAY);
			setMargin(new Insets(marginWidth, marginWidth, marginWidth, marginWidth));
			
			setText(output);
			addKeyListener(input);
			
		}// Console constructor
		
		Console(Console original)
		{
			this(original.getRows(), original.getColumns(), original.getFont().getSize(), 
					original.getKeyListeners()[0], original.getText());
			
		}// Console copy constructor (2)
			
		Console(int rows, int cols, int fontSize, String output)
		{
			this(rows, cols, fontSize, MUTE_LISTENER, output);
		
		}// Console no-input constructor
		
		Console(int rows, int cols, int fontSize, KeyListener input)
		{
			this(rows, cols, fontSize, input, DEFAULT_OUTPUT);
			
		}// Console dark constructor
		
		Console(int rows, int cols, int fontSize)
		{
			this(rows, cols, fontSize, MUTE_LISTENER, DEFAULT_OUTPUT);
			
		}// Console mute and blind constructor
		
		/**
		setInput
		Removes all listeners from current Console instance 
		and adds the one provided in the argument 
		*/
		public void setInput(KeyListener input)
		{
			for (KeyListener listener : getKeyListeners())
				removeKeyListener(listener);
			
			addKeyListener(input);
			
		}// setInput
		
		/**
		setOuput 
		Replaces the current String displayed on Console
		with the one provided in the argument
		*/
		public void setOutput(String output)
		{
			setText(output);
			
		}// setOutput
		
	}// Console

	JFrame window;
	Console console;
	
	static final KeyListener MUTE_LISTENER = new KeyListener()
	{
		public void keyTyped(KeyEvent e){}
		public void keyPressed(KeyEvent e){}
		public void keyReleased(KeyEvent e){}
	};

	static final int ROWS = 21;
	static final int COLS = 21;
	static final int DEFAULT_FONT_SIZE = 28;
	static final int SPAWN_FREQUENCY = 2;
	static final int SPAWN_QUANTITY = 1;
	final static int ANIMATION_INTERVAL = 15;
	
	static final int PLAYER_STARTING_ROW = ROWS/2;
	static final int PLAYER_STARTING_COL = COLS/2;
	
	Timer animation = new Timer(ANIMATION_INTERVAL, new ActionListener()
	{					
		public void actionPerformed(ActionEvent e)
		{	
			if (!arrow.move(arrow.moveRow, arrow.moveCol))
			{
				animation.stop();
				next();
			}
			
			updateBoard();
			updateVisible();
		
		}// actionPerformed
	});
	
	Menu visible;
	Menu previous;
	
	// Dynamic Game Variables
	int turn;
	int score;
	long startTime, endTime, elapsedTime;
	
	// Global Game Variables
	Player player;
	Arrow arrow;
	ArrayList<Spider> enemyList;
	Piece board[][];
	
	boolean reverseControls = false;
	
	Game()
	{	
		window = new JFrame();
		window.setUndecorated(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		console = new Console(ROWS, COLS, DEFAULT_FONT_SIZE);
		window.add(console);
		
		new StartGame().execute();

		ImageIcon img = new ImageIcon("./icon/finrod.gif");
		window.setIconImage(img.getImage());
		window.pack();
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		new EnterTitleScreen().execute();
		
	}// Game Constructor
	
	/**
	outOfBounds
	Checks if the given values for a row and column are
	out of the bounds of the game board
	*/
	boolean outOfBounds(int r, int c)
	{
		return (r < 0 || board.length <= r || c < 0 || board[0].length <= c);
	
	}// outOfBounds
	
	/**
	clearBoard
	Fills entire game board with the Floor tile piece
	*/
	void clearBoard()
	{
		for (int r = 0; r < board.length; r++)
			for (int c = 0; c < board[0].length; c++)
				board[r][c] = new Floor(r, c);
	
	}// clearBoard
	
	/**
	updateBoard
	Updates the position of all the dynamic board pieces
	*/
	void updateBoard()
	{			
		clearBoard();
		
		if (animation.isRunning())
			arrow.placeOnBoard();
		
		for (Piece spider : enemyList)
			spider.placeOnBoard();
		
		player.placeOnBoard();

	}// updateBoard
	
	/**
	updateVisible
	Updates the console to display the current
	menu object referenced by the 'visible' variable
	*/
	void updateVisible()
	{
		visible.update();
	
	}// updateVisible
	
	/**
	setup
	Initializes all dynamic game variables to their default values
	Imperative that it is called during any constructor of Game
	*/
	void setup()
	{
		// Tracking Variables
		turn = 0;	
		score = 0;
		startTime = 0;
		endTime = 0;
		elapsedTime = 0;
		
		// Board setup
		board = new Piece[ROWS][COLS];
		clearBoard();
			
		// Player Setup
		player = new Player(PLAYER_STARTING_ROW, PLAYER_STARTING_COL);
		player.placeOnBoard();
		
		// Arrow
		arrow = new Arrow();
		
		// Enemies Setup
		enemyList = new ArrayList<Spider>();
		
		//scoreClock.restart();
		
	}//setup	

	/**
	spawnSpider
	Spawns one spider in one of the four edges of the game map.
	This function is recursive and does NOT internally check for
	overflows. Overflow prevention should be present before function call.
	If the function is unable to spawn a spider in the map, a stack
	overflow *WILL OCCUR*
	*/
	void spawnSpider()
	{
		int r = -1, c = -1;
		boolean horizontal = (int)(Math.random()*2) == 0; // false means vertical
		boolean topOrLeft = (int)(Math.random()*2) == 0; // false means (bottom / right)
		
		if (horizontal)
		{	
			if (topOrLeft)
				r = 0;
			else
				r = board.length - 1;
	
			c = (int)(Math.random()*board[r].length);	
		}
		else
		{
			r = (int)(Math.random()*board.length);
			
			if (topOrLeft)
				c = 0;
			else
				c = board[r].length - 1;
		}
		
		if (board[r][c].ascii != Piece.FLOOR)
			spawnSpider(); // recursion point
		else
		{
			Spider s = new Spider(r, c); // break case
			s.placeOnBoard();
		}
		
	}// spawnSpider
	
	/**
	next
	Advances the game to the next turn by performing each turn's subroutines.
	1 - updates the board to current values
	2 - each spider moves following the order of spawn
	3 - if a spider spawn is due, one is performed
	4 - the turn counter is updated
	5 - the console is updated
	*/
	void next()
	{	
		updateBoard();
		
		for (Spider s : enemyList)
		{
			s.hunt();
			updateBoard();
		}
		
		for (Spider s : enemyList)
		{
			if (!s.moved)
			{
				s.hunt();
				updateBoard();
			}
		}
		
		if (turn%SPAWN_FREQUENCY == 0 && enemyList.size() < 2*(board.length - 1) + 2*(board[0].length - 1) - SPAWN_QUANTITY -1) // prevents stack overflow
		{
			for (int i = 0; i < SPAWN_QUANTITY; i++)
			{
				spawnSpider();
				updateBoard();
			}
		}
		
		turn++;
		updateVisible();
					
	}// next
	
	/**
	changeVisible
	Replaces the current Menu object 'visible' with the provided
	argument, then updates the console's I/O accordingly. The
	replaced menu is stored in previous;
	*/
	void changeVisible(Menu newVisible)
	{
		previous = visible;
		
		visible = newVisible;
		console.setInput(visible.controls);
		updateVisible();
	
	}// changeVisible
	
	void gameOver()
	{
		endTime = System.currentTimeMillis();
		changeVisible(new GameOverScreen());
		
	}// gameOver
	
	
	/** GAME TILES **/
	
	
	class Piece
	{
		final static char FLOOR = ' ';
		final static char PLAYER = '@';
		final static char SPIDER = 'm';
		
		char ascii;
		int row;
		int col;
		
		Piece(char a, int r, int c)
		{
			ascii = a;
			row = r;
			col = c;	
		}
		
		boolean move(int drow, int dcol)
		{
			int destRow = row + drow;
			int destCol = col + dcol;
			
			if (outOfBounds(destRow, destCol))
				return false;
			
			row = destRow;
			col = destCol;
			placeOnBoard();
			return true;
		}
		
		boolean moveTo(int r, int c)
		{	
			if (outOfBounds(r, c))
				return false;
			
			row = r;
			col = c;
			placeOnBoard();
			return true;
		}
		
		void placeOnBoard()
		{
			board[row][col] = this;
		}
		
		public String toString()
		{
			return Character.toString(ascii);
		}
		
	}// Piece
	
	class Floor extends Piece
	{
		Floor(int r, int c)
		{
			super(Piece.FLOOR, r, c);		
		}
		
	}// Floor
	
	class Spider extends Piece
	{	
		boolean moved;
		
		Spider(int r, int c)
		{
			super(Piece.SPIDER, r, c);
			enemyList.add(this);
			moved = false;
		}
		
		void kill()
		{
			enemyList.remove(this);
			updateBoard();
		}
		
		void hunt()
		{
			moved = false;
			
			int rDist = Math.abs(player.row - row);
			int cDist = Math.abs(player.col - col);
			
			if (rDist <= 1 && cDist <= 1 )
			{
				gameOver();
				return;
			}

			int destRow = row;
			int destCol = col;
			
			if (rDist != 0)
				destRow += (player.row - row)/rDist;
				
			if (cDist != 0)
				destCol += (player.col - col)/cDist;
			
			if (board[destRow][destCol].ascii == Piece.FLOOR)
			{
				moveTo(destRow, destCol);
				moved = true;
				return;
			}

			if (rDist < cDist)
			{
				if (cDist != 0)
					destCol -= (player.col - col)/cDist;
			}
			else
			{
				if (rDist != 0)
					destRow -= (player.row - row)/rDist;
			}
			
			if (board[destRow][destCol].ascii == Piece.FLOOR)
			{
				moveTo(destRow, destCol);
				moved = true;
				return;
			}
			
			if (rDist < cDist)
			{
				if (rDist != 0)
					destRow -= (player.row - row)/rDist;
			}
			else
			{	
				if (cDist != 0)
					destCol -= (player.col - col)/cDist;
			}
			
			if (board[destRow][destCol].ascii == Piece.FLOOR)
			{
				moveTo(destRow, destCol);
				moved = true;
				return;
			}
			
				
		}// hunt
		
	}// Spider
	
	class Player extends Piece
	{		
		int turnsToReady;
		final int RELOAD_TIME = 3;
		
		Player(int r, int c)
		{
			super(Piece.PLAYER, r, c);
			
			turnsToReady = 0;
			
		}// Player constructor	

		boolean move(int drow, int dcol)
		{
			int destRow = row + drow;
			int destCol = col + dcol;
			
			if (outOfBounds(destRow, destCol))
				return false;
			
			if (board[destRow][destCol].ascii == Piece.SPIDER)
				return false;
			
			if(animation.isRunning())
				arrow.completeAnimation();
			
			row = destRow;
			col = destCol;
			placeOnBoard();
		
			if (drow == 0 && dcol == 0)
				turnsToReady = 0;
			else
				if (turnsToReady > 0)
					turnsToReady--;
			
			next();
			
			return true;
			
		}// move
	
		void shoot(int aimRow, int aimCol)
		{	
			if (turnsToReady > 0)
				return;
			
			if(animation.isRunning())
				arrow.completeAnimation();
			
			turnsToReady = RELOAD_TIME;
			
			if (reverseControls)
			{
				aimRow *= -1;
				aimCol *= -1;
			}
			
			arrow = new Arrow(aimRow, aimCol);
			arrow.fly();
		
		}// shoot
				
	}// Player
	
	class Arrow extends Piece
	{
		boolean vertical;
		int moveRow, moveCol;
		int multiplier;

		Arrow(int dr, int dc)
		{
			super('-', player.row, player.col);
			
			vertical = (dc == 0);
			multiplier = 0;
			moveRow = dr;
			moveCol = dc;
	
		}// Arrow constructor
		
		void completeAnimation()
		{						
			animation.stop();
		
			while (arrow.move(arrow.moveRow, arrow.moveCol));
			next();
			updateBoard();
			updateVisible();
		}
		
		Arrow()
		{
			this(0, 0);
			
		}// Arrow no-arg constructor
		
		boolean move(int drow, int dcol)
		{
			int destRow = row + drow;
			int destCol = col + dcol;
			
			if (outOfBounds(destRow, destCol))
				return false;
			
			if (board[destRow][destCol].ascii == Piece.SPIDER)
			{
				((Spider)board[destRow][destCol]).kill();
				score += Math.pow(2, multiplier++);
				if (score > 999)
					score = 999;
			}
					
			row = destRow;
			col = destCol;
			placeOnBoard();
			
			return true;
			
		}// move
		
		void fly()
		{
			multiplier = 0;
			animation.start();
			
		}// fly
		
		public String toString()
		{
			if (vertical)
				return "!";
			else
				return super.toString();
		
		}// toString
		
	}// Arrow
	
	
	/** FUNCTIONS **/
	
	
	abstract class Function
	{
		static final String DEFAULT_NAME = "function_default_name";
		String name;
		
		Function(String name)
		{
			this.name = name;
		
		}// Function constructor
		
		Function()
		{
			name = DEFAULT_NAME;
			
		}// Function no-arg constructor
		
		public String toString()
		{
			return name;
		}
		
		abstract void execute();

	}// Function
	
	class ExitGame extends Function
	{	
		ExitGame(String s)
		{
			super(s);
			
		}// ExitGame name-constructor
		
		void execute()
		{
			System.exit(0);
			
		}// execute
		
	}// ExitGame
	
	class EnterTitleScreen extends Function
	{
		EnterTitleScreen()
		{
			super("ENTER TITLE SCREEN");
			
		}// EnterTitleScreen constructor
		
		EnterTitleScreen(String name)
		{
			super(name);
			
		}// EnterTitleScreen name constructor
		
		void execute()
		{
			changeVisible(new TitleScreen());
			
		}// execute
		
	}// EnterTitleScreen
	
	class EnterOptionsScreen extends Function
	{
		EnterOptionsScreen(String name)
		{
			super(name);
			
		}// EnterOptionsScreen constructor
		
		void execute()
		{
			changeVisible(new OptionsScreen());
			
		}// execute
		
	}// EnterOptionsScreen
	
	class StartGame extends Function
	{
		StartGame()
		{
			super("START GAME");
			
		}// StartGame constructor
		
		StartGame(String s)
		{
			super(s);
			
		}// StartGame name-constructor
		
		void execute()
		{
			setup();
			updateBoard();
			startTime = System.currentTimeMillis();
			changeVisible(new GameInterface());
			
		}// execute
		
	}// StartGame
		
	class ResumeGame extends Function
	{
		ResumeGame()
		{
			super("RESUME GAME");
			
		}// ResumeGame constructor
		
		ResumeGame(String s)
		{
			super(s);
			
		}// ResumeGame name-constructor
		
		void execute()
		{
			changeVisible(new GameInterface());
			
		}// execute
		
	}// ResumeGame
	
	class EnterPrevious extends Function
	{
		EnterPrevious()
		{
			super("ENTER PREVIOUS");
			
		}// EnterPrevious constructor
		
		void execute()
		{
			Menu temp = previous;
			changeVisible(temp);
			
		}// execute
		
	}// EnterPrevious
	
	class EnterInfoScreen extends Function
	{	
		EnterInfoScreen(String s)
		{
			super(s);
			
		}// EnterInfoScreen name-constructor
		
		void execute()
		{
			changeVisible(new InformationScreen());
			
		}// execute
		
	}// EnterInfoScreen
	
	class EnterPauseScreen extends Function
	{
		EnterPauseScreen()
		{
			super("ENTER PAUSE SCREEN");
			
		}// EnterPauseScreen constructor
		
		void execute()
		{
			changeVisible(new PauseScreen());
			
		}// execute
		
	}// EnterPauseScreen
	 
	
	/** INTERFACES **/
	
	
	abstract class Menu
	{
		int index;
		ArrayList<Function> options;
		KeyListener controls;
		
		abstract void update();
		
	}// Menu

	class TitleScreen extends Menu
	{
		TitleScreen()
		{
			index = 0;
			
			options = new ArrayList<Function>();
			options.add(new StartGame("Start"));
			options.add(new EnterInfoScreen("How To Play"));
			options.add(new EnterOptionsScreen("Options"));
			options.add(new ExitGame("Exit"));
			
			controls = new KeyListener()
					{
						public void keyPressed(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_UP:
								case KeyEvent.VK_W:
								case KeyEvent.VK_NUMPAD8:
									if (index > 0)
										index--;
									else
										index = options.size() - 1;
									break;
									
								case KeyEvent.VK_DOWN:
								case KeyEvent.VK_S:
								case KeyEvent.VK_NUMPAD2:
									if (index < options.size() - 1)
										index++;
									else
										index = 0;
									break;
									
								case KeyEvent.VK_ENTER:
									options.get(index).execute();
									break;
									
								case KeyEvent.VK_ESCAPE:
									index = options.size() - 1; // Exit index
									break;
							
							}// keyCode switch
							
							updateVisible();
							
						}// keyPressed

						public void keyTyped(KeyEvent e){}
						public void keyReleased(KeyEvent e){}

					};
			
		}// TitleScreen constructor

		void update()
		{
			String out = "";

			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";
			out += "/                                           \\\n";
			
			int skips = 6;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";

			out += "/          A Song of Birch and Silk         /\n";
			out += "/        ____________________________       /\n";
			
			skips = 3;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			for (int i = 0; i < options.size(); i++)
			{
				String optionString = options.get(i).toString();
				String line = "";
				
				int margin = 8;
				
				line += "/";
				for (int spc = 0; spc < margin; spc++)
					line += " ";

				if (i == index)
					line += ">  ";
				else
					line += "   ";
					
				line += optionString;
				
				int padding = 44 - line.length();
				
				for (int j = 0; j < padding; j++)
					line += " ";
				
				line += "/\n";
		
				out += line;
			}
			
			skips = 4;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			out += "\\\t\t\t         ver 1.0.0  /\n";
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			console.setText(out);
			
		}// update
		
	}// TitleScreen
	
	class GameInterface extends Menu
	{
		GameInterface()
		{
			controls = new KeyListener()
			{
				public void keyPressed(KeyEvent e)
				{
					int code = e.getKeyCode();
					
					switch(code)
					{
						// System
						case KeyEvent.VK_ESCAPE:
							new EnterPauseScreen().execute();
							break;
							
						// Movement
						case KeyEvent.VK_A:
						case KeyEvent.VK_NUMPAD4:
							player.move(0, -1);
							break;
						case KeyEvent.VK_W:
						case KeyEvent.VK_NUMPAD8:
							player.move(-1, 0);
							break;
						case KeyEvent.VK_S:
						case KeyEvent.VK_NUMPAD2:
							player.move(1, 0);
							break;
						case KeyEvent.VK_D:
						case KeyEvent.VK_NUMPAD6:
							player.move(0, 1);
							break;				
						case KeyEvent.VK_SPACE:
						case KeyEvent.VK_NUMPAD5:
							player.move(0, 0);
							break;
							
						// Shooting					
						case KeyEvent.VK_UP:
							player.shoot(-1, 0);
							break;
							
						case KeyEvent.VK_DOWN:
							player.shoot(+1, 0);
							break;
							
						case KeyEvent.VK_LEFT:
							player.shoot(0, -1);
							break;
							
						case KeyEvent.VK_RIGHT:
							player.shoot(0, +1);
							break;
						
					}// code switch
					
				}// keyPressed

				public void keyReleased(KeyEvent e){}
				public void keyTyped(KeyEvent e){}
			};
					
		}// GameInterface constructor
		
		void update()
		{
			String s = ""; 
			
			switch(player.turnsToReady)
			{
				case 0:
					s += " -=-=-=-=-=-=[ =-------------> ]=-=-=-=-=-=-\n";
					break;
					
				case 1:
					s += " -=-=-=-=-=-=[    ---------    ]=-=-=-=-=-=-\n";
					break;
					
				case 2:
					s += " -=-=-=-=-=-=[       ---       ]=-=-=-=-=-=-\n";
					break;					
				
				case 3:
					s += " -=-=-=-=-=-=[                 ]=-=-=-=-=-=-\n";
					break;
						
			}// turnsToReady switch
			
			for (int r = 0; r < board.length; r++)
			{
				if (r == board.length - 1)
					s += "\\ ";
				else
					s += "/ ";
				
				for (int c = 0; c < board[r].length; c++)
				{
					s += board[r][c] + " ";
					
					if (c == board[r].length - 1)
						if (r == 0)
							s += "\\";	
						else
							s += "/";
					
				}// for each column
			
				if (r < board.length - 1)
					s+= "\n";

			}// for each row
			
			String scoreString = new DecimalFormat("000").format(score);
			s += "\n -=-=-=-=-=-=-=-=-=[ " + scoreString + " ]=-=-=-=-=-=-=-=-=-";
			console.setOutput(s);
			
		}// update
		
	}// GameScren

	class InformationScreen extends Menu
	{
		InformationScreen()
		{
			controls = new KeyListener()
					{
						public void keyPressed(KeyEvent e)
						{
							new EnterPrevious().execute();
							
						}// keyPressed

						public void keyTyped(KeyEvent e){}
						public void keyReleased(KeyEvent e){}

					};
					
		}// InformationScreen constructor
		
		void update()
		{
			String out = "";
			
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";
			out += "/                                           \\\n";
			out += "/         m <--- this is a spider           /\n";
			out += "/         @ <--- this is you                /\n";
			out += "/                                           /\n";
			out += "/                                           /\n";
			out += "/   - Move using [WASD]                     /\n";
			out += "/                                           /\n";
			out += "/   - Wait by pressing [SPACEBAR]           /\n";
			out += "/                                           /\n";
			out += "/   - Shoot using the [ARROW KEYS]          /\n";
			out += "/                                           /\n";
			out += "/   - [ESC] accesses the in-game menu       /\n";
			out += "/                                           /\n";
			out += "/   - You must have an arrow fully          /\n";
			out += "/     drawn before you can shoot            /\n";
			out += "/                                           /\n";
			out += "/   - It takes [3 TURNS] to fully draw      /\n";
			out += "/     an arrow while moving                 /\n";
			out += "/                                           /\n";
			out += "/   - But just [1 TURN] if you wait         /\n";
			out += "\\                                           /\n";
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			console.setText(out);
			
		}// update
		
	}// InformationScreen

	class OptionsScreen extends Menu
	{
		OptionsScreen()
		{			
			controls = new KeyListener()
					{
						public void keyPressed(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
									
								case KeyEvent.VK_ESCAPE:
									new EnterPrevious().execute();
									break;
									
								default:
									reverseControls = !reverseControls;
									
							}// keyCode switch
							
							updateVisible();
							
						}// keyPressed
	
						public void keyTyped(KeyEvent e){}
						public void keyReleased(KeyEvent e){}
	
					};
				
		}// PauseScreen constructor
		
		public void update()
		{
			String out = "";

			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";
			out += "/                                           \\\n";

			int skips = 7;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			out += "/   Control scheme: ";
			
			out += ((reverseControls) ? "  Default  [Reverse]    /\n" : " [Default]  Reverse     /\n");
			
			skips = 1;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			if (reverseControls)
			{
				out += "/    Arrows are fired in the opposite       /\n";
				out += "/    direction of the pressed arrow key     /\n";
			}
			else
			{				
				out += "/    Arrows are fired in the same           /\n";
				out += "/    direction of the pressed arrow key     /\n";
			}
			
			skips = 8;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";

			out += "\\                                           /\n";
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			console.setText(out);
			
		}// update
		
	}// OptionsScreen

	class PauseScreen extends Menu
	{
		PauseScreen()
		{			
			index = 0;
		
			options = new ArrayList<Function>();

			options.add(new ResumeGame("Resume"));
			options.add(new StartGame("Restart"));
			options.add(new EnterOptionsScreen("Options"));
			options.add(new EnterInfoScreen("How to Play"));
			options.add(new EnterTitleScreen("Exit to Main Menu"));
			
			controls = new KeyListener()
					{
						public void keyPressed(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_UP:
								case KeyEvent.VK_W:
								case KeyEvent.VK_NUMPAD8:
									if (index > 0)
										index--;
									else
										index = options.size() - 1;
									break;
									
								case KeyEvent.VK_DOWN:
								case KeyEvent.VK_S:
								case KeyEvent.VK_NUMPAD2:
									if (index < options.size() - 1)
										index++;
									else
										index = 0;
									break;
									
								case KeyEvent.VK_ENTER:
									options.get(index).execute();
									break;
									
								case KeyEvent.VK_ESCAPE:
									new ResumeGame().execute();
									break;
									
							}// keyCode switch
							
							updateVisible();
							
						}// keyPressed
	
						public void keyTyped(KeyEvent e){}
						public void keyReleased(KeyEvent e){}
	
					};
				
		}// PauseScreen constructor
		
		void update()
		{
			String out = "";

			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";
			out += "/                                           \\\n";

			int skips = 7;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			for (int i = 0; i < options.size(); i++)
			{
				String optionString = options.get(i).toString();
				String line = "";
				
				int margin = 2;
				
				line += "/";
				for (int spc = 0; spc < margin; spc++)
					line += " ";

				if (i == index)
					line += ">  ";
				else
					line += "   ";
					
				line += optionString;
				
				int padding = 44 - line.length();
				
				for (int j = 0; j < padding; j++)
					line += " ";
				
				line += "/\n";
		
				out += line;
				
			}
			
			skips = 7;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";


			out += "\\                                           /\n";
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			console.setText(out);
			
		}// update
		
	}// PauseScreen
	
	class GameOverScreen extends Menu
	{
		GameOverScreen()
		{
			controls = new KeyListener()
			{
				public void keyPressed(KeyEvent e)
				{
					int code = e.getKeyCode();
					
					switch(code)
					{
						// System
						case KeyEvent.VK_ESCAPE:
							changeVisible(new ContinueScreen());
							break;
		
					}// code switch
					
				}// keyPressed

				public void keyReleased(KeyEvent e){}
				public void keyTyped(KeyEvent e){}
			};
			
		}// GameOverScreen constructor
		
		void update()
		{
			String s = ""; 
			s += " -=-=-=-=-=-=-=-[ GAME OVER ]-=-=-=-=-=-=-=-\n";
			
			for (int r = 0; r < board.length; r++)
			{
				if (r == board.length - 1)
					s += "\\ ";
				else
					s += "/ ";
				
				for (int c = 0; c < board[r].length; c++)
				{
					s += board[r][c] + " ";
					
					if (c == board[r].length - 1)
						if (r == 0)
							s += "\\";	
						else
							s += "/";
					
				}// for each column
			
				if (r < board.length - 1)
					s+= "\n";

			}// for each row
			

			s += "\n -=-=-=-=-[ Press ESC to continue ]-=-=-=-=-";
			
			console.setOutput(s);
			
		}// update
		
	}// GameOverScreen

	class ContinueScreen extends Menu
	{
		ContinueScreen()
		{
			index = 0;
			
			options = new ArrayList<Function>();

			options.add(new StartGame("New Game"));
			options.add(new EnterTitleScreen("Main Menu"));
			
			controls = new KeyListener()
					{
						public void keyPressed(KeyEvent e)
						{
							switch(e.getKeyCode())
							{
								case KeyEvent.VK_UP:
								case KeyEvent.VK_W:
								case KeyEvent.VK_NUMPAD8:
									if (index > 0)
										index--;
									else
										index = options.size() - 1;
									break;
									
								case KeyEvent.VK_DOWN:
								case KeyEvent.VK_S:
								case KeyEvent.VK_NUMPAD2:
									if (index < options.size() - 1)
										index++;
									else
										index = 0;
									break;
									
								case KeyEvent.VK_ENTER:
									options.get(index).execute();
									break;
									
							}// keyCode switch
							
							updateVisible();
							
						}// keyPressed
	
						public void keyTyped(KeyEvent e){}
						public void keyReleased(KeyEvent e){}
	
					};
			
		}// ContinueScreen constructor

		void update()
		{
			String out = "";

			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";
			out += "/                                           \\\n";

			int skips = 4;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
	
			String scoreString = new DecimalFormat("000").format(score);
			out += "/                  - " + scoreString + " -                  /\n";

			skips = 1;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			elapsedTime = endTime - startTime;
			String ms = new DecimalFormat("000").format(elapsedTime%1000);
			String s = new DecimalFormat("00").format((elapsedTime/1000)%60);
			String min = new DecimalFormat("00").format((elapsedTime/(1000*60)%60));
			out += "/                 " + min + ":" + s + ":" + ms + "                 /\n";
			
			skips = 3;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";
			
			
			for (int i = 0; i < options.size(); i++)
			{
				String optionString = options.get(i).toString();
				String line = "";
				
				int margin = 14;
				
				line += "/";
				for (int spc = 0; spc < margin; spc++)
					line += " ";

				if (i == index)
					line += ">  ";
				else
					line += "   ";
					
				line += optionString;
				
				int padding = 44 - line.length();
				
				for (int j = 0; j < padding; j++)
					line += " ";
				
				line += "/\n";
		
				out += line;
			}
			
			skips = 7;
			for (int i = 0; i < skips; i++)
				out += "/                                           /\n";

			out += "\\                                           /\n";
			out += " -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			console.setText(out);
			
		}// update
		
	}// ContinueScreen
	
}// Game
