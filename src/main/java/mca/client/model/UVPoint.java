package mca.client.model;

public final class UVPoint 
{
	private final int u;
	private final int v;
	private final int width;
	private final int height;
	
	public UVPoint (int u, int v, int width, int height)
	{
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
	}
	
	public int getU()
	{
		return u;
	}
	
	public int getV()
	{
		return v;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
