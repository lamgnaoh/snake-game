package main;

import java.awt.*;
import java.util.*;

// Food is a food item that the snake can eat. It is placed randomly in the pit.
public class Food {
	//current food location(x,y) in cells
	private int x,y;
	//color for display
	private Color color = Color.BLUE;
	//for randomly placing the food
	private Random rand = new Random();
	
	//default constructor
	public Food() {
		//place outside the pit, so that it will not be "displayed"
		x = -1;
		y = -1;
	}
	
	//Regenerate a food item. Randomly place inside the pit
	public void regenerate() {
		x = rand.nextInt(GameMain.COLUMNS - 4) + 2;
		y = rand.nextInt(GameMain.ROWS - 4) + 2;
		
	}
	//Return the x, y coordinate of the cell that contains this food item
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	//Draw itself
	public void draw(Graphics g) {
		g.setColor(color);
		g.fill3DRect(x * GameMain.CELL_SIZE,
				y* GameMain.CELL_SIZE, 
				 GameMain.CELL_SIZE,
				 GameMain.CELL_SIZE, 
				true);
	}
	
	
	
	
	
	
	
	
	
}
