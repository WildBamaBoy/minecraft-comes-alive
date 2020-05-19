package mca.core.minecraft;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import mca.core.Constants;
import mca.core.MCA;
import mca.items.ItemBaby;
import mca.items.ItemEngagementRing;
import mca.items.ItemGuideBook;
import mca.items.ItemMatchmakersRing;
import mca.items.ItemSpawnEgg;
import mca.items.ItemStaffOfLife;
import mca.items.ItemVillagerEditor;
import mca.items.ItemWeddingRing;
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

public final class ItemsMCA {
    public static final ItemSpawnEgg EGG_MALE = new ItemSpawnEgg(true);
    public static final ItemSpawnEgg EGG_FEMALE = new ItemSpawnEgg(false);
    public static final Item WEDDING_RING = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item WEDDING_RING_RG = new ItemWeddingRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item ENGAGEMENT_RING_RG = new ItemEngagementRing().setMaxStackSize(1);
    public static final Item MATCHMAKERS_RING = new ItemMatchmakersRing().setMaxStackSize(2);
    public static final Item BABY_BOY = new ItemBaby(true);
    public static final Item BABY_GIRL = new ItemBaby(false);
    public static final Item ROSE_GOLD_INGOT = new Item().setUnlocalizedName("rose_gold_ingot");
    public static final Item ROSE_GOLD_DUST = new Item().setUnlocalizedName("rose_gold_dust");
    public static final Item GOLD_DUST = new Item().setUnlocalizedName("gold_dust");
    public static final Item VILLAGER_EDITOR = new ItemVillagerEditor();
    public static final Item STAFF_OF_LIFE = new ItemStaffOfLife();
    public static final Item WHISTLE = new ItemWhistle();
    public static final ItemGuideBook BOOK_DEATH = new ItemGuideBook();
    public static final ItemGuideBook BOOK_ROMANCE = new ItemGuideBook();
    public static final ItemGuideBook BOOK_FAMILY = new ItemGuideBook();
    public static final ItemGuideBook BOOK_ROSE_GOLD = new ItemGuideBook();
    public static final ItemGuideBook BOOK_INFECTION = new ItemGuideBook();

    private static final List<Item> ITEMS = new ArrayList<>();

    public static void register(RegistryEvent.Register<Item> event) {
        for (Field f : ItemsMCA.class.getFields()) {
            try {
                Object instance = f.get(null);
                if (instance instanceof Item) {
                    Item item = (Item) instance;
                    setItemName(item, f.getName().toLowerCase());
                    event.getRegistry().register(item);
                    ITEMS.add(item);
                }
            } catch (Exception e) {
                MCA.getLog().error("Error while registering items: ", e);
            }
        }
    }

    public static void assignCreativeTabs() {
        ITEMS.stream().forEach(i -> i.setCreativeTab(MCA.creativeTab));
    }

    public static void setBookNBT(ItemStack stack) {
        Item book = stack.getItem();
        NBTTagCompound nbt = new NBTTagCompound();

        if (book == BOOK_DEATH) {
            nbt.setString("title", "Death, and How to Cure It!");
            nbt.setString("author", "Ozzie the Warrior");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();
            pages.appendTag(new NBTTagString(""
                    + "I couldn't count how many times my family has been blown to pieces by creepers.\n\nHow are they still around, you may ask?"
                    + "\n\nEasy! I, dear reader, have discovered a CURE for death itself! And through this book, I can share it with you."));

            /*
            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Memorials\n\n" + Constants.Format.RESET
                    + "When a family member dies, they will drop a chest - and inside will be an " + Constants.Format.BOLD + "item" + Constants.Format.RESET + " that was important to them.\n\n"
                    + "This is the key to reviving someone, don't lose it! Only your spouse and children will drop these items."));
            */
            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Staff Of Life\n\n" + Constants.Format.RESET
                    + "The Staff is a powerful item that can revive up to 5 people. Wave the staff around and speak the name of the villager you wish to revive. Within moments, your loved one will be fully revived!\n\n"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Summoning Grim\n\n" + Constants.Format.RESET
                    + "Unfortunately, you must obtain the Staff from the Grim Reaper himself.\n\n"
                    + "To summon him, you must build an altar consisting of 3 obsidian columns that are at least 2 blocks high. They may be higher if you like."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Summoning Grim pt. 2\n\n" + Constants.Format.RESET
                    + "     # # # X # # #\n"
                    + "     # # # # # # #\n"
                    + "     # # # # # # #\n"
                    + "     X # # E # # X\n\n"
                    + "X = Obsidian Column\n"
                    + "E = Emerald\n"
                    + "# = Empty"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Summoning Grim pt. 3\n\n" + Constants.Format.RESET
                    + "After building the altar, wait until night and light all 3 columns.\n\n"
                    + "When you're ready to fight, light the emerald block and run!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Fighting Grim\n\n" + Constants.Format.RESET
                    + "Grim is tough. Use full diamond armor, lots of potions, and lots of enchantments.\n\n"
                    + "He can:\n"
                    + "- Fly\n"
                    + "- Block attacks\n"
                    + "- Blind you\n"
                    + "- Move your items\n"
                    + "- Teleport\n"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Fighting Grim pt. 2\n\n" + Constants.Format.RESET
                    + "If you hit Grim while he's blocking, he will teleport behind you and strike.\n\n"
                    + "Do not try to use arrows or poison, he is immune!\n\n"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Fighting Grim pt. 3\n\n" + Constants.Format.RESET
                    + "When Grim is at " + Constants.Format.BOLD + "half health" + Constants.Format.RESET + " he will teleport into the air and begin healing.\n\n"
                    + "While healing, he will summon his minions from the underworld to fight you."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Fighting Grim pt. 4\n\n" + Constants.Format.RESET
                    + "When Grim is healed, he will continue attacking you, but he won't be able to heal again for 3 minutes and 30 seconds.\n\n"
                    + "Each time Grim heals, he will not be able to restore as much health has he did previously."));

            nbt.setTag("pages", pages);
        }

        else if (book == BOOK_ROMANCE)
        {
            nbt.setString("title", "Relationships and You");
            nbt.setString("author", "Gerry the Librarian");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Introduction\n\n" + Constants.Format.RESET
                    + "Interaction is key to building relationships and finding the love of your life.\n\n"
                    + "I've happily written this book in order to share my knowledge of interaction, love, and, unfortunately, divorce, to anyone who may need a little push in the right direction."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Interacting\n" + Constants.Format.RESET
                    + "Choose wisely when interacting with a villager!\n\n"
                    + "If choosing a romantic interaction, be sure that the villager you are talking to likes you a lot."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Interacting pt. 2\n" + Constants.Format.RESET
                    + "Don't be annoying! Talking to someone for too long will bore them, and your interactions may stop succeeding.\n\n"
                    + "If this happens, simply wait a few minutes before trying to talk to them again."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Marriage\n" + Constants.Format.RESET
                    + "To get married, simply gift a villager a wedding ring once you feel you have reached the highest relationship level.\n\n"
                    + "Once you're married, you can then procreate and have children of your own!"));

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
                    + " raising a child, but once they are past the baby stage, put them to work!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Babies\n\n" + Constants.Format.RESET
                    + "When you are married, simply approach your spouse and offer to 'Procreate'.\n\n"
                    + "After a short dance, you'll be the proud owner of a new baby boy or girl (or maybe even both)!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Growth\n\n" + Constants.Format.RESET
                    + "Babies take time to grow, make sure to hold them until they are ready, or give them to your spouse to take care of.\n\n"
                    + "Once a baby is ready to grow, you may place it on the ground and it will grow into a child!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Adolescence\n\n" + Constants.Format.RESET
                    + "Children will grow slowly from a baby to a teen.\n\nHowever, the magical properties of Golden Apples are said to accelerate "
                    + "any child's growth. I have yet to try this myself."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Chores\n\n" + Constants.Format.RESET
                    + "Any child can harvest, chop wood, prospect, hunt, and fish. You'll need to provide them with the tools they need to do so.\n\n"
                    + "If a tool breaks and the child doesn't have another, they will have no choice but to stop working."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Adulthood\n\n" + Constants.Format.RESET
                    + "As sad as it may be, children will eventually grow into adults.\n\n"
                    + "Adults can be married off by using Matchmaker's Rings, or they will eventually get married on their own."));

            nbt.setTag("pages", pages);
        }

        else if (book == BOOK_ROSE_GOLD)
        {
            nbt.setString("title", "On Rose Gold");
            nbt.setString("author", "William the Miner");
            nbt.setBoolean("resolved", true);

            NBTTagList pages = new NBTTagList();

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Warning!\nTOP SECRET\n\n" + Constants.Format.RESET
                    + "This manual is the property of William Mining Co.\n\nIf you are not a William Mining Co. employee, please refrain from "
                    + "reading this manual and return promptly to William the Miner."));

            pages.appendTag(new NBTTagString(""
                    + "Ah, rose gold - a lovely combination of silver, copper, and gold that smelts into a pinkish orange metal.\n\n"
                    + "Most use it as an alternative to gold for crafting rings as it is much less expensive.\n\n"
                    + "However, it has some interesting qualities that are easy to miss."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Creating Dust\n\n" + Constants.Format.RESET
                    + "Rose gold, once smelted, can be crushed into a fine dust.\n\n"
                    + "Look closely at rose gold dust in bright light, and you'll see shiny flecks of pure gold!\n\n"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Washing Dust\n\n" + Constants.Format.RESET
                    + "With a little work, we can actually extract the gold from the dust and create pure gold ingots. Simply mix dust with a bucket of water.\n\n"
                    + "The lighter silver and copper components will wash away, leaving you with about 6 smaller piles of gold dust."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Extracting Gold\n\n" + Constants.Format.RESET
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

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "What is the Infection?\n\n" + Constants.Format.RESET
                    + "I discovered long ago that the zombies that appear at night are actually villagers in the late stages of infection!\n\n"
                    + "Newly infected villagers turn green, are unable to speak, and occasionally try to bite!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Curing\n\n" + Constants.Format.RESET
                    + "Contrary to popular belief, the infection can be cured if caught in time.\n\n"
                    + "You must first weaken the villager or zombie with a potion.\n\n"
                    + "Then, immediately feed them a golden apple."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Curing pt. 2\n\n" + Constants.Format.RESET
                    + "Zombies that can be cured often have enlarged heads and noses.\n\n"
                    + "Any other zombies you see unfortunately are too far gone, and cannot be cured."));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Curing pt. 3\n\n" + Constants.Format.RESET
                    + "Zombies will take a couple of minutes to be fully cured.\n\n"
                    + "Villagers who were recently infected, though, and haven't had time to degrade into a full zombie, will be cured instantly!"));

            pages.appendTag(new NBTTagString(Constants.Format.BOLD + "Warnings\n\n" + Constants.Format.RESET
                    + "Villagers are highly susceptible to infection, and children even more so!\n\n"
                    + "If you carry a baby with you while fighting zombies, there is a chance it can become infected."));

            nbt.setTag("pages", pages);
        }

        stack.setTagCompound(nbt);
    }

    @SideOnly(Side.CLIENT)
    public static void registerModelMeshers() {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

        for (Item item : ITEMS) mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void setItemName(Item item, String itemName) {
        item.setUnlocalizedName(itemName);
        item.setRegistryName(new ResourceLocation(MCA.MODID + ":" + itemName));
    }
}