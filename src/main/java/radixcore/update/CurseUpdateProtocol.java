/*package com.radixshock.radixcore.core;

import java.net.URL;
import java.util.Scanner;

import radixcore.ModMetadataEx;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.radixshock.radixcore.constant.Font;
import com.radixshock.radixcore.util.object.Version;

*//**
 * <b>NOTICE!</b> This updater is set up differently. On your mod base,
 * {@link IEnforcedCore#getRedirectURL()} is not used at all and for
 * {@link IEnforcedCore#getUpdateURL()}, you put the slug for your project
 * instead. For example:<br />
 * http://curse.com/mc-mods/minecraft/<b><u>mb-battlegear-2</u></b><br />
 * <br />
 * Also, it's set up in a special way on your Curse project. Your files HAVE to
 * have the exact name of it's version. But this updater will only work if you
 * use <code>#.#.#</code>, <code>#.#</code> , or simply just <code>#</code>
 * version formats. If you don't use any of these, you can change it by changing
 * {@link simpleVersioning} below to say <code>true</code>. Also, just as a
 * quick note, when you use version formats, they're interpreted as this:
 * <table>
 * <tr>
 * <th>Actual version number</th>
 * <th>How the updater interprets it</th>
 * </tr>
 * <tr>
 * <td>1.6.4</td>
 * <td>1.6.4</td>
 * </tr>
 * <tr>
 * <td>1.8</td>
 * <td>1.8.0</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>1.0.0</td>
 * </tr>
 * <tr>
 * <td>1.2.3.4</td>
 * <td>1.2.3</td>
 * </tr>
 * </table>
 * 
 * @author MCGamer20000
 *//*
public class CurseUpdateProtocol implements IUpdateProtocol
{
	@Override
	public UpdateData getUpdateData(ModMetadataEx modData) 
	{
		UpdateData returnData;

		try 
		{
			URL url = new URL("http://widget.mcf.li/mc-mods/minecraft/" + modData.curseId + ".json");
			Scanner scanner = new Scanner(url.openStream());

			JsonObject file = new GsonBuilder().create().fromJson(scanner.nextLine(), JsonObject.class).get("download").getAsJsonObject();

			returnData = new UpdateData();
			returnData.modVersion = scanner.nextLine();

			returnData.modVersion = file.get("version").getAsString();
		}

		catch (Exception e) 
		{
			mod.getLogger().log("Error checking for updates.");
		}

		return null;
	}

	@Override
	public void cleanUp() 
	{

	}
}
*/