package mca.tile;

import net.minecraft.tileentity.TileEntitySign;

public class TileTombstone extends TileEntitySign
{
	public TileTombstone()
	{
	}
	
	@Override
    public boolean getIsEditable()
    {
        return true;
    }
}
