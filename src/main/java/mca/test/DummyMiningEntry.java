package mca.test;

public class DummyMiningEntry
{
	private String name;
	private float percentileWeight;
	
	public DummyMiningEntry(String name, float percentileWeight)
	{
		this.name = name;
		this.percentileWeight = percentileWeight;
	}
	
	public String getName()
	{
		return name;
	}
	
	public float percentileWeight()
	{
		return percentileWeight;
	}
}
