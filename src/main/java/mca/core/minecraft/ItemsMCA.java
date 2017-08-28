package mca.core.minecraft;

import java.util.ArrayList;

import mca.core.MCA;
import mca.enums.EnumMemorialType;
import mca.items.ItemBaby;
import mca.items.ItemCrystalBall;
import mca.items.ItemGuideBook;
import mca.items.ItemMemorial;
import mca.items.ItemNewOutfit;
import mca.items.ItemSpawnEgg;
import mca.items.ItemSpawnGrimReaper;
import mca.items.ItemStaffOfLife;
import mca.items.ItemTombstone;
import mca.items.ItemTooltipAppender;
import mca.items.ItemVillagerEditor;
import mca.items.ItemWhistle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import radixcore.constant.Font.Format;

public final class ItemsMCA
{
	private static final ArrayList<Item> ITEMS = new ArrayList<Item>();
	
	public static final Item WEDDING_RING = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setMaxStackSize(1);
	public static final Item WEDDING_RING_RG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to marry them.").setMaxStackSize(1);
	public static final Item ENGAGEMENT_RING = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setMaxStackSize(1);
	public static final Item ENGAGEMENT_RING_RG = new ItemTooltipAppender().setTooltip("Gift to a villager with 5 gold hearts to become engaged.").setMaxStackSize(1);
	public static final Item MATCHMAKERS_RING = new ItemTooltipAppender().setTooltip("Gift two to villagers standing close to each other to arrange a marriage between them.").setMaxStackSize(2);
	public static final Item DIVORCE_PAPERS = new ItemTooltipAppender().setTooltip("Gift to your spouse to end your marriage.").setMaxStackSize(1);
	public static final ItemBaby BABY_BOY = new ItemBaby(true);
	public static final ItemBaby BABY_GIRL = new ItemBaby(false);
	public static final ItemSpawnEgg EGG_MALE = new ItemSpawnEgg(true);
	public static final ItemSpawnEgg EGG_FEMALE = new ItemSpawnEgg(false);
	public static final ItemSpawnGrimReaper EGG_GRIM_REAPER = new ItemSpawnGrimReaper();
	public static final ItemCrystalBall CRYSTAL_BALL = new ItemCrystalBall();
	public static final Item ROSE_GOLD_INGOT = new Item();
	public static final Item ROSE_GOLD_DUST = new ItemTooltipAppender().setTooltip("Dust from a crushed rose gold ingot.");
	public static final Item GOLD_DUST = new Item();
	public static final ItemNewOutfit NEW_OUTFIT = new ItemNewOutfit();
	public static final Item NEEDLE_AND_STRING = new ItemTooltipAppender().setTooltip("Use with some wool to create cloth.").setMaxDamage(16).setMaxStackSize(1);
	public static final Item CLOTH = new ItemTooltipAppender().setTooltip("This can be used to craft new clothes for your villagers.");
	public static final ItemGuideBook BOOK_DEATH = new ItemGuideBook();
	public static final ItemGuideBook BOOK_ROMANCE = new ItemGuideBook();
	public static final ItemGuideBook BOOK_FAMILY = new ItemGuideBook();
	public static final ItemGuideBook BOOK_ROSE_GOLD = new ItemGuideBook();
	public static final ItemGuideBook BOOK_INFECTION = new ItemGuideBook();
	public static final ItemTombstone TOMBSTONE = new ItemTombstone();
	public static final ItemWhistle WHISTLE = new ItemWhistle();
	public static final ItemVillagerEditor VILLAGER_EDITOR = new ItemVillagerEditor();
	
	public static final ItemMemorial BROKEN_RING = new ItemMemorial(EnumMemorialType.BROKEN_RING);
	public static final ItemMemorial CHILDS_DOLL = new ItemMemorial(EnumMemorialType.DOLL);
	public static final ItemMemorial TOY_TRAIN = new ItemMemorial(EnumMemorialType.TRAIN);
	public static final ItemStaffOfLife STAFF_OF_LIFE = new ItemStaffOfLife();

	public static void register(RegistryEvent.Register<Item> event)
	{
		final Item[] items = {
				WEDDING_RING,
				WEDDING_RING_RG,
				ENGAGEMENT_RING,
				ENGAGEMENT_RING_RG,
				MATCHMAKERS_RING,
				DIVORCE_PAPERS,
				BABY_BOY,
				BABY_GIRL,
				EGG_MALE,
				EGG_FEMALE,
				EGG_GRIM_REAPER,
				CRYSTAL_BALL,
				ROSE_GOLD_INGOT,
				ROSE_GOLD_DUST,
				GOLD_DUST,
				NEW_OUTFIT,
				NEEDLE_AND_STRING,
				CLOTH,
				TOMBSTONE,
				WHISTLE,
				VILLAGER_EDITOR,
				BROKEN_RING,
				CHILDS_DOLL,
				TOY_TRAIN,
				STAFF_OF_LIFE,
				BOOK_DEATH,
				BOOK_ROMANCE,
				BOOK_FAMILY,
				BOOK_ROSE_GOLD,
				BOOK_INFECTION
		};
		
		setItemName(WEDDING_RING, "wedding_ring");
		setItemName(WEDDING_RING_RG, "wedding_ring_rg");
		setItemName(ENGAGEMENT_RING, "engagement_ring");
		setItemName(ENGAGEMENT_RING_RG, "engagement_ring_rg");
		setItemName(MATCHMAKERS_RING, "matchmakers_ring");
		setItemName(DIVORCE_PAPERS, "divorce_papers");
		setItemName(BABY_BOY, "baby_boy");
		setItemName(BABY_GIRL, "baby_girl");
		setItemName(EGG_MALE, "egg_male");
		setItemName(EGG_FEMALE, "egg_female");
		setItemName(EGG_GRIM_REAPER, "egg_grim_reaper");
		setItemName(CRYSTAL_BALL, "crystal_ball");
		setItemName(ROSE_GOLD_INGOT, "rose_gold_ingot");
		setItemName(ROSE_GOLD_DUST, "rose_gold_dust");
		setItemName(GOLD_DUST, "gold_dust");
		setItemName(NEW_OUTFIT, "new_outfit");
		setItemName(NEEDLE_AND_STRING, "needle_and_string");
		setItemName(CLOTH, "cloth");
		setItemName(TOMBSTONE, "tombstone");
		setItemName(WHISTLE, "whistle");
		setItemName(VILLAGER_EDITOR, "villager_editor");
		setItemName(BROKEN_RING, "broken_ring");
		setItemName(CHILDS_DOLL, "childs_doll");
		setItemName(TOY_TRAIN, "toy_train");
		setItemName(STAFF_OF_LIFE, "staff_of_life");
		setItemName(BOOK_DEATH, "book_death");
		setItemName(BOOK_ROMANCE, "book_romance");
		setItemName(BOOK_FAMILY, "book_family");
		setItemName(BOOK_ROSE_GOLD, "book_rose_gold");
		setItemName(BOOK_INFECTION, "book_infection");
		
		for (Item item : items)
		{
			item.setCreativeTab(MCA.getCreativeTab());
			event.getRegistry().register(item);
			ITEMS.add(item);
		}
	}

	public static void setBookNBT(ItemStack stack)
	{
		Item book = stack.getItem();
		NBTTagCompound nbt = new NBTTagCompound();
		
		if (book == BOOK_DEATH)
		{
			nbt.setString("title", "Death, and How to Cure It!");
			nbt.setString("author", "Ozzie the Warrior");
			nbt.setBoolean("resolved", true);
			
			NBTTagList pages = new NBTTagList();
			pages.appendTag(new NBTTagString(""
					+ "I couldn't count how many times my family has been blown to pieces by creepers.\n\nHow are they still around, you may ask?"
					+ "\n\nEasy! I, dear reader, have discovered a CURE for death itself! And through this book, I can share it with you."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Memorials\n\n" + Format.RESET
					+ "When a family member dies, they will drop a chest - and inside will be an " + Format.BOLD + "item" + Format.RESET + " that was important to them.\n\n"
					+ "This is the key to reviving someone, don't lose it! Only your spouse and children will drop these items."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Staff Of Life\n\n" + Format.RESET
					+ "The Staff is a powerful item that can revive up to 5 people. Place a memorial item on the ground and wave the staff over it. Within moments, your loved one will be fully revived!\n\n"));

			pages.appendTag(new NBTTagString(Format.BOLD + "Summoning Grim\n\n" + Format.RESET
					+ "Unfortunately, you must obtain the Staff from the Grim Reaper himself.\n\n"
					+ "To summon him, you must build an altar consisting of 3 obsidian columns that are at least 2 blocks high. They may be higher if you like."));

			pages.appendTag(new NBTTagString(Format.BOLD + "Summoning Grim pt. 2\n\n" + Format.RESET
					+ "     # # # X # # #\n"
					+ "     # # # # # # #\n"
					+ "     # # # # # # #\n"
					+ "     X # # E # # X\n\n"
					+ "X = Column\n"
					+ "E = Emerald\n"
					+ "# = Empty"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Summoning Grim pt. 3\n\n" + Format.RESET
					+ "After building the altar, wait until night and light all 3 columns.\n\n"
					+ "When you're ready to fight, light the emerald block and run!"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Fighting Grim\n\n" + Format.RESET
					+ "Grim is tough. Use full diamond armor, lots of potions, and lots of enchantments.\n\n"
					+ "He can:\n"
					+ "- Fly\n"
					+ "- Block attacks\n"
					+ "- Blind you\n"
					+ "- Move your items\n"
					+ "- Teleport\n"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Fighting Grim pt. 2\n\n" + Format.RESET
					+ "If you hit Grim while he's blocking, he will teleport behind you and strike.\n\n"
					+ "Do not try to use arrows or poison, he is immune!\n\n"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Fighting Grim pt. 3\n\n" + Format.RESET
					+ "When Grim is at " + Format.BOLD + "half health" + Format.RESET + " he will teleport into the air and begin healing.\n\n"
					+ "While healing, he will summon his minions from the underworld to fight you."));

			pages.appendTag(new NBTTagString(Format.BOLD + "Fighting Grim pt. 4\n\n" + Format.RESET
					+ "When Grim is healed, he will continue attacking you, but he won't be able to heal again for 3 minutes and 30 seconds.\n\n"
					+ "Each time Grim heals, he will not be able to restore as much health has he did previously."));
			
			nbt.setTag("pages", pages);
		}
		
		else if (book == BOOK_ROMANCE)
		{
			nbt.setString("title", "The Gentleman's Guide to Relationships");
			nbt.setString("author", "Gerry the Librarian");
			nbt.setBoolean("resolved", true);
			
			NBTTagList pages = new NBTTagList();
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Introduction\n\n" + Format.RESET
					+ "Interaction is key to building relationships and finding the love of your life.\n\n"
					+ "I've happily written this book in order to share my knowledge of interaction, love, and, unfortunately, divorce, to anyone who may need a little push in the right direction."));

			pages.appendTag(new NBTTagString(Format.BOLD + "Personalities\n\n" + Format.RESET
					+ "When speaking to any villager, you'll notice they have a personality.\n\n"
					+ "Pay close attention to this. I have outlined each personality's quirks here. Each personality has a particular category they fall into (1-3)."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Personalities pt. 2\n" + Format.RESET
					+ Format.BOLD + "Athletic (2): " + Format.RESET + "Runs faster\n"
					+ Format.BOLD + "Confident (3): " + Format.RESET + "Hits harder\n"
					+ Format.BOLD + "Strong (3): " + Format.RESET + "Doubled attack damage\n"
					+ Format.BOLD + "Friendly (1): " + Format.RESET + "Gains hearts faster\n"
					+ Format.BOLD + "Curious (2): " + Format.RESET + "Finds more when working\n"
					+ Format.BOLD + "Peaceful (1): " + Format.RESET + "Will not fight\n"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Personalities pt. 3\n" + Format.RESET
					+ Format.BOLD + "Flirty (2): " + Format.RESET + "Bonus to all interactions\n"
					+ Format.BOLD + "Witty (2): " + Format.RESET + "Appreciates jokes\n"
					+ Format.BOLD + "Sensitive (1): " + Format.RESET + "Easily offended\n"
					+ Format.BOLD + "Greedy (3): " + Format.RESET + "Finds less when working\n"
					+ Format.BOLD + "Stubborn (3): " + Format.RESET + "Harder to gain hearts\n"
					+ Format.BOLD + "Odd (2): " + Format.RESET + "N/A\n"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Personalities pt. 4\n" + Format.RESET
					+ "The category shows what a person likes more than others:\n"
					+ Format.BOLD + "1:" + Format.RESET + "Chatting, Stories\n"
					+ Format.BOLD + "2:" + Format.RESET + "Joking, Romance\n"
					+ Format.BOLD + "3:" + Format.RESET + "Chatting, Shake Hand, Stories\n\n"
					+ "In the case of personality type 3, it is not recommended to attempt jokes or romantic interactions."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Moods\n" + Format.RESET
					+ "Every person has a mood, which is always apparent when speaking to them.\n\n"
					+ "Moods can change throughout the day, and determine how likely a villager is to like your interaction, and how many hearts you'll gain."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Moods pt. 2\n" + Format.RESET
					+ "Villagers in a good mood may have a certain 'glow' about them, and will be easier to interact with.\n\n"
					+ "Villagers in a bad mood may cry, or be visibly angry and be more difficult to interact with."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Moods pt. 3\n" + Format.RESET
					+ "The death of a villager seems to put those nearby in bad moods.\n\n"
					+ "Taxing also gradually decreases the moods of everyone nearby.\n\n"
					+ "Gifts and successful interactions are known to boost moods."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Interacting\n" + Format.RESET
					+ "Choose wisely when interacting with a villager, based on their mood and personality!\n\n"
					+ "If choosing a romantic interaction, be sure that the villager you are talking to likes you a lot."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Interacting pt. 2\n" + Format.RESET
					+ "Don't be annoying! Talking to someone for too long will bore them, and your interactions may stop succeeding.\n\n"
					+ "If this happens, simply wait a few minutes before trying to talk to them again."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Marriage\n" + Format.RESET
					+ "To get married, simply gift a villager a wedding ring once you feel you have reached the highest relationship level.\n\n"
					+ "If you have a lot of friends in the village, you may want to get engaged first!"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Engagement\n" + Format.RESET
					+ "Gift an engagement ring before gifting a wedding ring.\n\n"
					+ "All nearby villagers will give you gifts when you get married, but only if you're engaged first!\n\n"
					+ "Villagers that like you more will give you better gifts."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Divorce\n" + Format.RESET
					+ "Unfortunately, sometimes it is best to split from your spouse and move on.\n\n"
					+ "To do this, you may craft divorce papers with paper, a feather, and ink.\n\n"
					+ "Gift them to your spouse and the marriage will end. They will not be happy!"));
			
			nbt.setTag("pages", pages);
		}

		else if (book == BOOK_FAMILY)
		{
			nbt.setString("title", "Managing Your Family Vol. XI");
			nbt.setString("author", "Leanne the Cleric");
			nbt.setBoolean("resolved", true);
			
			NBTTagList pages = new NBTTagList();
			pages.appendTag(new NBTTagString(""
					+ "Children are our future! Make sure to have as many as you possibly can.\n\nNot only do you get to experience the joy of"
					+ "raising a child, but once they are past the baby stage, put them to work!"));

			pages.appendTag(new NBTTagString(Format.BOLD + "Babies\n\n" + Format.RESET
					+ "When you are married, simply approach your spouse and offer to 'Procreate'\n\n"
					+ "After a short dance, you'll be the proud owner of a new baby boy or girl!"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Growth\n\n" + Format.RESET
					+ "Babies take time to grow, make sure to hold them until they are ready, or give them to your spouse to take care of.\n\n"
					+ "Once a baby is ready to grow, you may place it on the ground and it will grow into a child!"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Adolescence\n\n" + Format.RESET
					+ "Children will grow slowly from age 4 to 18.\n\nHowever, the magical properties of Golden Apples are said to accelerate "
					+ "any child's growth. I have yet to try this myself."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Chores\n\n" + Format.RESET
					+ "Any child can farm, cut wood, mine, hunt, and fish. You'll need to provide them with the tools they need to do so.\n\n"
					+ "If a tool breaks and the child doesn't have another, they will have no choice but to stop working."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Greed\n\n" + Format.RESET
					+ "If you notice your child has a Greedy personality, be careful!\n\n"
					+ "Greedy children are known to steal away items that they may pick up while doing chores."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Mining\n\n" + Format.RESET
					+ "A special note on Mining here, most children have a peculiar ability to search for and locate ores underground.\n\n"
					+ "This activity damages any pickaxe they may have in their inventory.\n\n"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Adulthood\n\n" + Format.RESET
					+ "As sad as it may be, children will eventually grow into adults. Once they are adults, they will no longer work for you.\n\n"
					+ "Adults can be married off by using Matchmaker's Rings, or they will eventually get married on their own."));
			
			nbt.setTag("pages", pages);
		}
		
		else if (book == BOOK_ROSE_GOLD)
		{
			nbt.setString("title", "On Rose Gold");
			nbt.setString("author", "William the Miner");
			nbt.setBoolean("resolved", true);
			
			NBTTagList pages = new NBTTagList();
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Warning!\nTOP SECRET\n\n" + Format.RESET
					+ "This manual is the property of William Mining Co.\n\nIf you are not a William Mining Co. employee, please refrain from "
					+ "reading this manual and return promptly to William the Miner."));
			
			pages.appendTag(new NBTTagString(""
					+ "Ah, rose gold - a lovely combination of silver, copper, and gold that smelts into a pinkish orange metal.\n\n"
					+ "Most use it as an alternative to gold for crafting rings as it is much less expensive.\n\n"
					+ "However, it has some interesting qualities that are easy to miss."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Creating Dust\n\n" + Format.RESET
					+ "Rose gold, once smelted, can be crushed into a fine dust.\n\n"
					+ "Look closely at rose gold dust in bright light, and you'll see shiny flecks of pure gold!\n\n"));

			pages.appendTag(new NBTTagString(Format.BOLD + "Washing Dust\n\n" + Format.RESET
					+ "With a little work, we can actually extract the gold from the dust and create pure gold ingots. Simply mix dust with a bucket of water.\n\n"
					+ "The lighter silver and copper components will wash away, leaving you with about 6 smaller piles of gold dust."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Extracting Gold\n\n" + Format.RESET
					+ "Arrange 9 piles of dust on your crafting table, and if you're lucky, you'll find a gold nugget in one of them!\n\n"
					+ "And of course, once you have 9 gold nuggets, you'll be able to craft them into a solid gold ingot."));
			
			nbt.setTag("pages", pages);
		}
		
		else if (book == BOOK_INFECTION)
		{
			nbt.setString("title", "Beware the Infection!");
			nbt.setString("author", "Richard the Zombie");
			nbt.setBoolean("resolved", true);
			
			NBTTagList pages = new NBTTagList();
			
			pages.appendTag(new NBTTagString(""
					+ "Good day, readers! I've written this book so that you may not end up suffering the same fate as I.\n\n"
					+ "Although I caught the infection, I was luckily able to keep all of my mental faculties."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "What is the Infection?\n\n" + Format.RESET
					+ "I discovered long ago that the zombies that appear at night are actually villagers in the late stages of infection!\n\n"
					+ "Newly infected villagers turn green, are unable to speak, and occasionally try to bite!"));

			pages.appendTag(new NBTTagString(Format.BOLD + "Curing\n\n" + Format.RESET
					+ "Contrary to popular belief, any infected villager can be cured.\n\n"
					+ "You must first weaken the villager or zombie with a potion.\n\n"
					+ "Then, immediately feed them a golden apple."));

			pages.appendTag(new NBTTagString(Format.BOLD + "Curing pt. 2\n\n" + Format.RESET
					+ "Zombies that can be cured often have enlarged heads and noses.\n\n"
					+ "Any other zombies you see unfortunately are too far gone, and cannot be cured."));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Curing pt. 3\n\n" + Format.RESET
					+ "Zombies will take a couple of minutes to be fully cured.\n\n"
					+ "Villagers who were recently infected, though, and haven't had time to degrade into a full zombie, will be cured instantly!"));
			
			pages.appendTag(new NBTTagString(Format.BOLD + "Warnings\n\n" + Format.RESET
					+ "Villagers are highly susceptible to infection, and children even more so!\n\n"
					+ "If you carry a baby with you while fighting zombies, there is a chance it can become infected."));
			
			nbt.setTag("pages", pages);
		}
		
		stack.setTagCompound(nbt);
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerModelMeshers()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		for (Item item : ITEMS)
		{
			mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
	}
	
	private static void setItemName(Item item, String itemName)
	{
		item.setUnlocalizedName(itemName);
		item.setRegistryName(new ResourceLocation(MCA.ID + ":" + itemName));
	}
}
