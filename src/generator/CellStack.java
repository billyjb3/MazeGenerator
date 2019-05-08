package generator;

public class CellStack
{
	int[] cells;
	int top = -1;
	
	CellStack()
	{
		cells = new int[40];
	}
	
	public void push(int n)
	{
		if(top == cells.length-1)
			expand();
		cells[++top] = n;
	}
	public int pop()
	{
		if(top != -1)
			return cells[top--];
		return -1;
	}
	public int peek()
	{
		if(top != -1)
			return cells[top];
		return -1;
	}
	private void expand()
	{
		int[] temp = new int[cells.length*2];
		for(int i = 0; i < cells.length; i++)
		{
			temp[i] = cells[i];
		}
		cells = temp;
	}
	public boolean isEmpty()
	{
		if(top == -1)
			return true;
		return false;
	}
}
