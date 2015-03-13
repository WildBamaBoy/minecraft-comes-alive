package radixcore.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import radixcore.item.ItemEffect;
import radixcore.item.ItemSingle;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.registry.GameRegistry;

public final class RadixStartup 
{
	public static CreativeTabs registerCreativeTab(Class itemClass, String itemFieldName, ModMetadata metadata, String name)
	{
		try
		{
			final Field itemRefField = itemClass.getField(itemFieldName);
			String upperName = RadixString.upperFirstLetter(itemRefField.getName());

			Item item = new Item();
			item.setUnlocalizedName(upperName);
			item.setTextureName(metadata.modId + ":" + upperName);
			
			CreativeTabs returnTab = new CreativeTabs("tab" + metadata.modId + (name != null ? "." + name : ""))
			{
				@Override
				public Item getTabIconItem()
				{
					try 
					{
						return (Item)itemRefField.get(null);
					} 
					
					catch (Exception e) 
					{
						RadixExcept.logFatalCatch(e, "Registering tab icon item");
					}
					
					return null; //Fall-through from exception.
				}
			};
			
			item.setCreativeTab(returnTab);
			return returnTab;
		}

		catch (Exception e)
		{
			RadixExcept.logFatalCatch(e, "registering creative tab");
		}
		
		return null; //Fall-through from exception.
	}

	/**
	 * See {@link #initBaseItems(Class, CreativeTabs, ModMetadata)}
	 * 
	 * @param 	itemClass	The class containing the fields to automatically be instantiated and registered as Items.
	 * @param	metadata	The metadata of the mod these items will belong to.
	 */
	public static void initBaseItems(Class itemClass, ModMetadata metadata)
	{
		initBaseItems(itemClass, null, metadata);
	}

	/**
	 * Analyzes the static fields in the provided class and fully instantiates and registers items 
	 * matching the type {@link net.minecraft.Item}, {@link radixcore.item.ItemEffect}, or {@link radixcore.item.ItemSingle}
	 * <p>
	 * This method assumes you name your fields using camel case with the first letter as lower-case. The new item instance 
	 * will have its unlocalized name assigned as the field name <b>with the first letter capitalized</b> (referred to as 
	 * <code>upperName</code>) for aesthetic purposes. For example, provided the field name <code>fooBar</code>, the unlocalized 
	 * name will be <code>FooBar</code>. 
	 * <p>
	 * The item will also have its texture name assigned using <code>upperName</code>.
	 * <p>
	 * The new item is fully registered with Forge's game registry, using <code>upperName</code> as 
	 * the mod-unique name of the item.
	 * <p>
	 * If the field is not static, not of the type Item, not or of the type ItemEnchanted, it will remain null
	 * after this method completes execution.
	 * 
	 * @param 	itemClass		The class containing the fields to automatically be instantiated and registered as Items.
	 * @param	creativeTab		The creative tab to group the item under.
	 * @param	metadata		The metadata of the mod these items will belong to.
	 */
	public static void initBaseItems(Class itemClass, CreativeTabs creativeTab, ModMetadata metadata)
	{
		for (Field f : itemClass.getFields())
		{
			try
			{
				//Only handle static, non-null fields of the type Item or ItemEffect.
				if (Modifier.isStatic(f.getModifiers()) && f.get(null) == null && 
						(f.getType() == Item.class || f.getType() == ItemEffect.class || f.getType() == ItemSingle.class))
				{
					Item item = f.getType() == Item.class ? new Item() : f.getType() == ItemEffect.class ? new ItemEffect() : new ItemSingle();
					String upperName = RadixString.upperFirstLetter(f.getName());

					item.setUnlocalizedName(upperName);
					item.setTextureName(metadata.modId + ":" + upperName);
					item.setCreativeTab(creativeTab);
					
					f.set(null, item);

					GameRegistry.registerItem((Item) f.get(null), upperName);
				}
			}

			catch (Exception e)
			{
				RadixExcept.logFatalCatch(e, "registering item " + f.getName());
			}
		}
	}

	private RadixStartup()
	{

	}
}
