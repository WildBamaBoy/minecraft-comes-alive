package mca.test;

public class DummyStack
{
	private String name;
	private int size;
	
	public DummyStack(String name, int size)
	{
		this.name = name;
		this.size = size;
	}
	
	public int getStackSize()
	{
		return size;
	}
	
	public String getStackName()
	{
		return name;
	}
}
