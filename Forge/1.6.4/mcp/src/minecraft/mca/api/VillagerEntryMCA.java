package mca.api;

import java.util.ArrayList;
import java.util.List;

public class VillagerEntryMCA 
{
	public List<String> skinsMale = new ArrayList<String>();
	public List<String> skinsFemale = new ArrayList<String>();
	protected int id;
	protected boolean isLocalized;
	protected boolean useDefaultTexture;
	protected String professionName;
	protected String texturesLocation;

	protected VillagerEntryMCA(int id, String professionName, String texturesLocation)
	{
		this.id = id;
		this.professionName = professionName;
		this.texturesLocation = texturesLocation;
		this.isLocalized = false;
		this.useDefaultTexture = this.texturesLocation.equals("/assets/mca/textures/api/skins/");
	}

	public boolean getIsLocalized()
	{
		return isLocalized;
	}

	public boolean getUseDefaultTexture()
	{
		return useDefaultTexture;
	}

	public String getLocalizedProfessionID()
	{
		if (this.id != -1)
		{
			return "profession." + professionName.toLowerCase();
		}
		
		else
		{
			return "profession.playerchild";
		}
	}

	public String getUnlocalizedProfessionName()
	{
		return professionName;
	}

	public String getTexturesLocation() 
	{
		return texturesLocation;
	}
}
