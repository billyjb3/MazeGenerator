package generator;

public class Cell
{
	private int x, y;
	private boolean isWall = false;
	private boolean visited = false;
	private boolean isStart = false;
	private boolean isFinish = false;
	private boolean isDeadEnd = false;
	
	Cell(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	Cell(int x, int y, boolean isWall)
	{
		this.x = x;
		this.y = y;
		this.isWall = isWall;
	}
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public void setIsWall(boolean isWall)
	{
		this.isWall = isWall;
	}
	public boolean isWall()
	{
		return isWall;
	}
	public void setVisited(boolean v)
	{
		this.visited = v;
	}
	public boolean isVisited()
	{
		return visited;
	}
	public void setIsStart(boolean b)
	{
		this.isStart = b;
	}
	public boolean isStart()
	{
		return isStart;
	}
	public void setIsFinish(boolean b)
	{
		this.isFinish = b;
	}
	public boolean isFinish()
	{
		return isFinish;
	}
	public void setIsDeadEnd(boolean b)
	{
		isDeadEnd = b;
	}
	public boolean isDeadEnd()
	{
		return isDeadEnd;
	}
}
