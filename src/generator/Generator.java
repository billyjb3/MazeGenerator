package generator;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;

public class Generator extends Canvas implements Runnable
{
	private JFrame frame; 
	private BufferStrategy bs;
	private Graphics g;
	
	private Cell[][] cells; //WxH grid of maze tiles, either being wall or path
	private CellNode start;
	private CellNode cnode;
	private int cx, cy;
	private int mazeWidth = 151; //preferable if odd number, but not necessary; minimum for non-trivial mazes ~51; optimal >= 151
									//max size is 1151, beyond that cellSize is <1..Canvas collapses
	private int UPS = 60;								//need to set min cell size to 1 so that width can continue increasing. may end up bigger than screen tho
	
	private int mazeHeight; //mazeHeight ~= mazeWidth/16*9; --screen proportioned
	
	private int cellSize; //cellSize = (1200-boarderSize*2)/mazeWidth; --sets the cellsize to help maintain a constant frame size. 
							//works well enough but isn't right for all width sizes. needs to be improved
	
	private int boarderSize = 20;
	
	//canvas size = (mazeWidth*cellSize + boarderSize*2 , mazeHeight*cellSize + boarderSize*2)
	
	private boolean isRunning = false;
	private boolean isSolving = false;
	private boolean autoSolve = true;
	private boolean showGeneration = true;
	private boolean unlimitedUPS = false;
	private Thread thread;
	
	private CellStack stack = new CellStack();
	
	private Random r = new Random();
	
	//while generating, the % chances that the maze will turn in the given direction
	//plan to make trending for this to decrease crowding to make the branches longer/more spaced
	private int rightChance = 40;
	private int upChance = 10;
	private int leftChance = 40;
	private int downChance = 10; //not needed; calculated in code by subtracting previous three from 100% still here as representation/ease of testing
	private int trendR;
	private int d;
	
	public void run()
	{
		long nano = (long) 1e9;
		long updateNano = nano/UPS;
		long start = System.nanoTime();
		long current;
		long delta;
		int updates = 0;
		int frames = 0;
		
		while(isRunning)
		{
			current = System.nanoTime();
			delta = current - start;
			if(delta >= nano)
			{
				frame.setTitle("Maze Generator   |   FPS: " + frames + "   |   UPS: " + updates);
				updates = 0;
				frames = 0;
				start = System.nanoTime();
			}
			if(unlimitedUPS || delta > updates*updateNano)
			{
				update();
				updates++;
			}
			if(showGeneration == true)
			{
				render();
				frames++;
			}
		}
		stop();
	}
	private void update()
	{
		if(!isSolving)
		{
			if(cells[stack.peek()%mazeWidth][stack.peek()/mazeWidth].isFinish())
				stack.pop();
			else
			{
				boolean[] dir = new boolean[4]; //right, up, left, down
				
				int x = stack.peek() % mazeWidth;
				int y = stack.peek() / mazeWidth;

				if(y - 2 >= 0 && !cells[x][y-2].isVisited())
					dir[1] = true;
				if(x + 2 < mazeWidth && !cells[x+2][y].isVisited())
					dir[0] = true;
				if(y + 2 < mazeHeight && !cells[x][y+2].isVisited())	
					dir[3] = true;
				if(x - 2 >= 0 && !cells[x-2][y].isVisited())	
					dir[2] = true;
				
				if(dir[0] == false && dir[1] == false && dir[2] == false && dir[3] == false)
				{
					if(stack.peek() == 0)
					{
						if(autoSolve)
						{
							isSolving = true;
							start = new CellNode(0);
							cnode = start;
							for(int i = 0; i < cells.length; i++)
								for(int j = 0; j < cells[0].length; j++)
									cells[i][j].setVisited(false);
							cells[0][0].setVisited(true);
						}
						else
							isRunning = false;
					}
					else
						stack.pop();
				}
				else
				{
					boolean found = false;
					while(!found)
					{
						trendR = r.nextInt(100);
						if(trendR >= 0 && trendR < rightChance)
							d = 0;
						else if(trendR >= rightChance && trendR < upChance+rightChance)
							d = 1;
						else if(trendR >= upChance+rightChance && trendR < upChance+rightChance+leftChance)
							d = 2;
						else if(trendR >= upChance+rightChance+leftChance && trendR < 100)
							d = 3;
						if(dir[d] == true)
						{
							found = true;
							if(d == 0)
							{
								cells[x+1][y].setIsWall(false);
								cells[x+1][y].setVisited(true);
								cells[x+2][y].setVisited(true);
								stack.push(y*mazeWidth + (x+2));
							}
							else if(d == 1)
							{
								cells[x][y-1].setIsWall(false);
								cells[x][y-1].setVisited(true);
								cells[x][y-2].setVisited(true);
								stack.push((y-2)*mazeWidth + x);
							}
							else if(d == 2)
							{
								cells[x-1][y].setIsWall(false);
								cells[x-1][y].setVisited(true);
								cells[x-2][y].setVisited(true);
								stack.push(y*mazeWidth + (x-2));
							}
							else if(d == 3)
							{
								cells[x][y+1].setIsWall(false);
								cells[x][y+1].setVisited(true);
								cells[x][y+2].setVisited(true);
								stack.push((y+2)*mazeWidth + x);
							}
						}
					}
				}
			}
		}
		else //solver
		{
			cx = cnode.getID()%mazeWidth;
			cy = cnode.getID()/mazeWidth;
			
			if(cx + 1 < mazeWidth && !cells[cx+1][cy].isWall() && !cells[cx+1][cy].isVisited()) //check right
			{
				cnode.addChild(new CellNode(cy*mazeWidth + (cx+1)));
				cnode = cnode.getChildByID(cy*mazeWidth + (cx+1));
				cells[cx+1][cy].setVisited(true);
			}
			else if(cy-1 >= 0 && !cells[cx][cy-1].isWall() && !cells[cx][cy-1].isVisited()) // check up
			{
				cnode.addChild(new CellNode((cy-1)*mazeWidth + cx));
				cnode = cnode.getChildByID((cy-1)*mazeWidth + cx);
				cells[cx][cy-1].setVisited(true);
			}
			else if(cx-1 >= 0 && !cells[cx-1][cy].isWall() && !cells[cx-1][cy].isVisited()) // check left
			{
				cnode.addChild(new CellNode(cy*mazeWidth + (cx-1)));
				cnode = cnode.getChildByID(cy*mazeWidth + (cx-1));
				cells[cx-1][cy].setVisited(true);
			}
			else if(cy+1 < mazeHeight && !cells[cx][cy+1].isWall() && !cells[cx][cy+1].isVisited()) //check down
			{
				cnode.addChild(new CellNode((cy+1)*mazeWidth + cx));
				cnode = cnode.getChildByID((cy+1)*mazeWidth + cx);
				cells[cx][cy+1].setVisited(true);
			}
			else
			{
				cells[cx][cy].setIsDeadEnd(true);
				cnode = cnode.getParent();
			}
			
			if(cells[cnode.getID()%mazeWidth][cnode.getID()/mazeWidth].isFinish())
			{
				isRunning = false;
				render();
			}
		}
	}
	private void render()
	{
		bs = getBufferStrategy();
		g = bs.getDrawGraphics();
		g.setColor(Color.black);
		g.fillRect(0,0, this.getWidth(), this.getHeight());
			
		for(int i = 0; i < mazeWidth; i++)
			for(int j = 0; j < mazeHeight; j++)
			{
				if(cells[i][j].isWall())
					g.setColor(Color.black);
				else if(!isSolving && cells[i][j].isVisited()) 
					g.setColor(Color.white);
				else if(isSolving && cells[i][j].isVisited() && !cells[i][j].isDeadEnd()) 
					g.setColor(Color.blue);
				else if(isSolving && cells[i][j].isVisited() && cells[i][j].isDeadEnd()) 
					g.setColor(Color.white);
				else if(!isSolving && !cells[i][j].isVisited()) 
					g.setColor(Color.black);
				else if(isSolving && !cells[i][j].isVisited())
					g.setColor(Color.white);
				g.fillRect(cells[i][j].getX(), cells[i][j].getY(), cellSize, cellSize);
			}
		
		g.setColor(Color.blue);
		g.fillRect(cells[0][0].getX(), cells[0][0].getY(), cellSize, cellSize);
		
		g.setColor(Color.green);
		g.fillRect(cells[mazeWidth-1][mazeHeight-1].getX(), cells[mazeWidth-1][mazeHeight-1].getY(), cellSize, cellSize);
		
		bs.show();
		g.dispose();
	}
	public synchronized void start()
	{
		isRunning = true;
		thread = new Thread(this);
		thread.start();
	}
	public synchronized void stop()
	{
		isRunning = false;
		try{thread.join();}
		catch(Exception e){e.printStackTrace();}
	}
	Generator()
	{
		if(mazeWidth%2 == 0)
			mazeWidth++;
		mazeHeight = mazeWidth/16*9;
		if(mazeHeight%2 == 0)
			mazeHeight++;
		cellSize = (1200-boarderSize*2)/mazeWidth;
		this.setSize(mazeWidth*cellSize + boarderSize*2, mazeHeight*cellSize + boarderSize*2);
		cells = new Cell[mazeWidth][mazeHeight];
		for(int i = 0; i < mazeWidth; i++)
			for(int j = 0; j < mazeHeight; j++)
			{
				cells[i][j] = new Cell(i*cellSize + boarderSize, j*cellSize + boarderSize);
				
				if(i%2 != 0 || j%2 != 0)
				{
					cells[i][j].setIsWall(true);
				}
			}

		frame = new JFrame("MazeGenerator   |   FPS: -   |   UPS: -");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		cells[0][0].setIsStart(true);
		cells[0][0].setVisited(true);
		stack.push(0);
		
		cells[mazeWidth - 1][mazeHeight - 1].setIsFinish(true);
		createBufferStrategy(3);
		
		frame.addComponentListener
		(
			new ComponentListener()
			{
				public void componentMoved(ComponentEvent arg0)
				{
					if(!isRunning)
						render();
				}
				public void componentHidden(ComponentEvent arg0){}
				public void componentResized(ComponentEvent arg0){}
				public void componentShown(ComponentEvent arg0){}
			}
		);
	}	
	public Cell[][] getCells()
	{
		return cells;
	}
	public boolean isRunning()
	{
		return isRunning;
	}
	public int getCellSize()	
	{
		return cellSize;
	}
	public void setAutoSolve(boolean b)
	{
		autoSolve = b;
	}
	public void setShowGeneration(boolean b)
	{
		showGeneration = b;
	}
}
