package generator;

public class CellNode
{
	int id;
	int ctop = 0;
	CellNode parent;
	CellNode[] children = new CellNode[3];
	
	CellNode(int id)
	{
		this.id = id;
	}
	public void setParent(CellNode parent)
	{
		this.parent = parent;
	}
	public void addChild(CellNode child)
	{
		if(ctop <= 2)
		{
			children[ctop] = child;
			children[ctop].setParent(this);
			ctop++;
		}
		else
		{
			System.out.println("CellNode is Full!");
		}
	}
	public int getID()
	{
		return id;
	}
	public CellNode getChildByID(int id)
	{
		for(int i = 0; i < 3; i++)
		{
			if(children[i] != null && children[i].getID() == id)
				return children[i];
		}
		return null;
	}
	public CellNode getParent()
	{
		return parent;
	}
}
