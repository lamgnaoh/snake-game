package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//main class for the game
public class GameMain extends JPanel{

	private static final long serialVersionUID = 1L;
	//Define constants for the game
	static final String TITLE = "Java 2D - Snake hunting";
	//number of rows (in cells)
	static final int ROWS = 40;
	//number of columns (in cells)
	static final int COLUMNS = 40;
	//Size of a cell (in pixels)
	static final int CELL_SIZE = 15;
	//width and height of the game screen
	static final int CANVAS_WIDTH = COLUMNS * CELL_SIZE;
	static final int CANVAS_HEIGHT = ROWS * CELL_SIZE;
	//number of game update per second = 3;
	static final int UPDATE_PER_SEC = 3;
	//per nanoseconds
	static final long UPDATE_PERIOD_NSEC = 1000000000L / UPDATE_PER_SEC;
	
	
	//Enumeration for the statas of the game
	enum GameState {
		INITIALIZED, PLAYING, PAUSED, GAMEOVER, DESTROYED
	}
	
	//Declare menubar
	static JMenuBar menuBar;
	
	// current state of the game
	static GameState state;
	
	//Define instance variables for the game objects
	private Food food;
	private Snake snake;
	
	// Handle for the custom drawing panel and UI components
	private GameCanvas pit;
	private ControlPanel control;
	private JLabel lblScore;
	int score = 0;
	
	// Constructor to init the UI components and game object
	public GameMain() {
		//init the game objects
		gameInit();
		
		// create UI components
		setLayout(new BorderLayout());
		//drawing panel
		pit = new GameCanvas();
		pit.setPreferredSize(new Dimension(CANVAS_WIDTH,CANVAS_HEIGHT));
		add(pit, BorderLayout.CENTER);
		
		//control panel to the bottom
		control = new ControlPanel();
		add(control, BorderLayout.SOUTH);
		
		//add the menu bar
		setupMenuBar();
		
		// start the game
		gameStart();
	}
	
	// ----------- All the game related codes here -------------
	// init all the game objects, run only once in the constructor of the main class
	public void gameInit() {
		//Allocate a new snake and a food item, do not regenerate
		snake = new Snake();
		food = new Food();
		state = GameState.INITIALIZED;
	}
	
	//shutdown the game, clean up code that runs only once
	public void gameShutdown() {
		
	}
	
	// to start and restart the game
	public void gameStart() {
		// Create a new thread
		Thread gameThread = new Thread() {
			//Override run() to provide the running behavior of this thread
			public void run() {
				gameLoop();
			}
		};
		//Start the thread.start() calls run, which in turn calls gameLoop()
		gameThread.start();
	}
	
	// run the game loop here
	private void gameLoop() {
		//Regenerate and reset the game objects for a new game
		if(state == GameState.INITIALIZED || state == GameState.GAMEOVER) {
			//Generate a new snake and a food item
			snake.regenerate();
			
			//regenerate if food placed under the snake
			int x, y;
			do {
				food.regenerate();
				x = food.getX();
				y = food.getY();
			}while(snake.contains(x,y));
			
			state = GameState.PLAYING;
			
		}
		//Game loop
		long beginTime, timeTaken, timeLeft; //in msec
		while(state != GameState.GAMEOVER) {
			beginTime = System.nanoTime();
			if(state == GameState.PLAYING) {
				//update the state and position of all the game objects
				//detect collisions and provide responses
				gameUpdate();
			}
			// Refresh the display
			repaint();
			//Delay timer to provide the necessary delay to meet the target rate
			timeTaken = System.nanoTime() - beginTime;
			// in milliseconds
			timeLeft = (UPDATE_PERIOD_NSEC - timeTaken)/ 1000000;
			if(timeLeft < 10) timeLeft = 10; //set a minium
			try {
					//Provides the necessary delay and also yields control
					//so that other thread can do work
				Thread.sleep(timeLeft);
				}catch(InterruptedException ex) {}
			
		}
	}
	
	//update the state and position of all the game objects
	// detect collisions and provide responses
	public void gameUpdate() {
		snake.update();
		processCollision();
	}
	
	//Collision detection and response
	public void processCollision() {
	// check if this snake eats the food item
		int headX = snake.getHeadX();
		int headY = snake.getHeadY();
		
		if(headX == food.getX() && headY == food.getY()) {
			// to play a specific sound
			SoundEffect.EAT.play();
			score = score + 1;
			lblScore.setText("Score: "+score);
			
			//food eaten, regenerate one
			int x, y;
			do {
				food.regenerate();
				x = food.getX();
				y = food.getY();
			}while(snake.contains(x, y));
		}else {
			//not eaten, shrink the tail
			snake.shrink();
		}
		
		// Check if the snake moves out of bounds
		if(!pit.contains(headX, headY)) {
			state = GameState.GAMEOVER;
			// to play a specific sound
			SoundEffect.DIE.play();
			score = 0;
			lblScore.setText("Score: "+score);
			return;
		}
		
		// Check if the snake eats itself
		if(snake.eatItself()) {
			state = GameState.GAMEOVER;
			// to play a specific sound
			SoundEffect.DIE.play();
			score = 0;
			lblScore.setText("Score: "+score);
			return;
		}
	}
	
	// Refresh the display. Called back via repaint(), which invoke the paintComponent()
	private void gameDraw(Graphics g) {
		//draw game objects
		 snake.draw(g);
		 food.draw(g);
		
		g.setFont(new Font("Dialog", Font.PLAIN, 14));
		g.setColor(Color.BLACK);
		g.drawString("Snake: ("+snake.getHeadX() + "," + snake.getHeadY() + ")",5, 25);
		
		if(state == GameState.GAMEOVER) {
			g.setFont(new Font("Verdana", Font.BOLD, 30));
			g.setColor(Color.RED);
			g.drawString("GAME OVER!", 200, CANVAS_HEIGHT / 2);
		}
		
	}
	
	//Process a key-pressed event. Update the current state
	public void gameKeyPressed(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_UP:
			snake.setDirection(Snake.Direction.UP);
			break;
		case KeyEvent.VK_DOWN:
			snake.setDirection(Snake.Direction.DOWN);
			break;
		case KeyEvent.VK_LEFT:
			snake.setDirection(Snake.Direction.LEFT);
			break;
		case KeyEvent.VK_RIGHT:
			snake.setDirection(Snake.Direction.RIGHT);
			break;
		}
	}
	
	
	//Game Control Panel with Start, Stop, Pause and Mute buttons, designed as an inner class
	class ControlPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private JButton btnStartPause;
		private JButton btnStop;
		private JButton btnMute;
		//import icons for buttons
		private ImageIcon iconStart = new ImageIcon(getClass().getResource("/images/start.png"), "START");
		private ImageIcon iconPause = new ImageIcon(getClass().getResource("/images/pause.png"), "PAUSE");
		private ImageIcon iconStop = new ImageIcon(getClass().getResource("/images/stop.png"), "STOP");
		private ImageIcon iconSound = new ImageIcon(getClass().getResource("/images/sound.png"), "SOUND ON");
		private ImageIcon iconMuted = new ImageIcon(getClass().getResource("/images/muted.png"), "MUTED");
		
		
		public ControlPanel () {
			this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
			
			btnStartPause = new JButton(iconPause);
			btnStartPause.setToolTipText("Pause");
			btnStartPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnStartPause.setEnabled(true);
			add(btnStartPause);
			
			btnStop = new JButton(iconStop);
			btnStop.setToolTipText("Stop");
			btnStop.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnStop.setEnabled(true);
			add(btnStop);
			
			btnMute = new JButton(iconMuted);
			btnMute.setToolTipText("Mute");
			btnMute.setCursor(new Cursor(Cursor.HAND_CURSOR));
			btnMute.setEnabled(true);
			add(btnMute);
			
			lblScore = new JLabel("Score: 0");
			add(lblScore);
			
			//handle click events on buttons
			btnStartPause.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switch(state) {
					case INITIALIZED:
					case GAMEOVER:
						btnStartPause.setIcon(iconPause);
						btnStartPause.setToolTipText("Pause");
						gameStart();
						//To play a specific sound
						SoundEffect.CLICK.play();
						score = 0;
						lblScore.setText("Score: "+score);
						break;
					case PLAYING:
						state = GameState.PAUSED;
						btnStartPause.setIcon(iconStart);
						btnStartPause.setToolTipText("Start");
						//To play a specific sound
						SoundEffect.CLICK.play();
						break;
					case PAUSED:
						state = GameState.PLAYING;
						btnStartPause.setIcon(iconPause);
						btnStartPause.setToolTipText("Pause");
						//To play a specific sound
						SoundEffect.CLICK.play();
						break;
					}
					btnStop.setEnabled(true);
					pit.requestFocus();
					
				}
			});
			
			btnStop.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					state = GameState.GAMEOVER;
					btnStartPause.setIcon(iconStart);
					btnStartPause.setEnabled(true);
					btnStop.setEnabled(false);
					//To play a specific sound
					SoundEffect.CLICK.play();
					
				}
			});
			
			btnMute.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(SoundEffect.volume == SoundEffect.Volume.MUTE) {
						SoundEffect.volume = SoundEffect.Volume.LOW;
						btnMute.setIcon(iconSound);
						//To play a specific sound
						SoundEffect.CLICK.play();
						pit.requestFocus();
					}else {
						SoundEffect.volume = SoundEffect.Volume.MUTE;
						btnMute.setIcon(iconMuted);
						//To play a specific sound
						SoundEffect.CLICK.play();
						pit.requestFocus();
					}
					
				}
			});
			
		}
		
		// Reset control for a new game
					public void reset() {
						btnStartPause.setIcon(iconStart);
						btnStartPause.setEnabled(true);
						btnStop.setEnabled(false);
					}
	}
	
	// Custom drawing panel, written as an inner class
	class GameCanvas extends JPanel implements KeyListener {

		private static final long serialVersionUID = 1L;
		//constructor
		public GameCanvas() {
			setFocusable(true); //so that can receive key-events
			requestFocus();
			addKeyListener(this);
		}
		
		//overwrite paintComponent to do custom drawing
		//called back by repaint()
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			//paint background, may use an image for background
			//set background color
			setBackground(Color.decode("0x3F919E"));
			
			//draw the game objects
			gameDraw(g);
		}
		
		//KeyEvent handlers
		@Override
		public void keyPressed(KeyEvent e) {
			gameKeyPressed(e.getKeyCode());
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		// Check if this pit contains the given(x,y) for collision detection
		public boolean contains (int x, int y) {
			if((x<0)|| (x>=ROWS)) return false;
			if((y<0)|| (y>=COLUMNS)) return false;
			return true;
		}
		
	}
	// Helper function to setup the menubar
	private void setupMenuBar() {
		JMenu menu; //a menu in the menu bar
		JMenuItem menuItem; // a regular menu-item in a menu
		
		menuBar = new JMenuBar();
		
		// First Menu - "Game"
		menu = new JMenu("Game");
		menu.setMnemonic(KeyEvent.VK_G);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("New", KeyEvent.VK_N);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// stop the current game if needed
				if(state == GameState.PLAYING || state == GameState.PAUSED) {
					state = GameState.GAMEOVER;
				}
				gameStart();
				control.reset();
			}
			
		});
		
		//Help Menu
		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);
		
		menuItem = new JMenuItem("Help Contents", KeyEvent.VK_H);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String message = "Arrow keys to change direction\n"
						+"P to pause/resume \n"
						+"S to toggle sound on/off \n";
				JOptionPane.showMessageDialog(GameMain.this, message,
						"Instructions", JOptionPane.PLAIN_MESSAGE);
					
				
			}
			
		});
		
		menuItem = new JMenuItem("About", KeyEvent.VK_A);
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(GameMain.this, 
						"The snake hunting for teaching students game programming",
						"About", JOptionPane.PLAIN_MESSAGE);
				
			}
			
		});
	}
	
	// main function
	public static void main(String[] args) {
		//use the event dispatch thread to build the UI for thread-safety
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame(TITLE);
				// main JPanel as content pane
				frame.setContentPane(new GameMain());
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.pack();
				//center the app window
				frame.setLocationRelativeTo(null);
				//show the frame
				frame.setJMenuBar(menuBar);
				frame.setVisible(true);
			
			}
		});
	}
	
	
	
	
	
	
	
	
	
	
	
}
