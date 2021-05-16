package mca.entity;

import cobalt.minecraft.inventory.CInventory;
import cobalt.minecraft.nbt.CNBT;
import cobalt.minecraft.network.datasync.*;
import cobalt.minecraft.world.CWorld;
import mca.api.API;
import mca.api.types.APIButton;
import mca.api.types.Hair;
import mca.client.gui.GuiInteract;
import mca.core.Constants;
import mca.core.MCA;
import mca.core.minecraft.ProfessionsMCA;
import mca.entity.data.*;
import mca.enums.*;
import mca.items.ItemSpecialCaseGift;
import mca.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

public class EntityVillagerMCA extends VillagerEntity implements INamedContainerProvider {
    public final CDataManager data = new CDataManager(this);

    public CStringParameter villagerName = data.newString("villagerName");
    public CStringParameter clothes = data.newString("clothes");
    public CStringParameter hair = data.newString("hair");
    public CStringParameter hairOverlay = data.newString("hairOverlay");
    public CIntegerParameter gender = data.newInteger("gender");
    public CTagParameter memories = data.newTag("memories");
    public CIntegerParameter moveState = data.newInteger("moveState");
    public CIntegerParameter ageState = data.newInteger("ageState");
    public CStringParameter spouseName = data.newString("spouseName");
    public CUUIDParameter spouseUUID = data.newUUID("spouseUUID");
    public CUUIDParameter playerToFollowUUID = data.newUUID("spouseUUID");
    public CIntegerParameter marriageState = data.newInteger("marriageState");
    public CBooleanParameter isProcreating = data.newBoolean("isProcreating");
    public CTagParameter parents = data.newTag("parents");
    public CBooleanParameter isInfected = data.newBoolean("isInfected");
    public CIntegerParameter activeChore = data.newInteger("activeChore");
    public CBooleanParameter isSwinging = data.newBoolean("isSwinging");
    public CBooleanParameter hasBaby = data.newBoolean("hasBaby");
    public CBooleanParameter isBabyMale = data.newBoolean("isBabyMale");
    public CIntegerParameter babyAge = data.newInteger("babyAge");
    public CUUIDParameter choreAssigningPlayer = data.newUUID("choreAssigningPlayer");
    public BlockPosParameter bedPos = data.newPos("bedPos");
    public BlockPosParameter workplacePos = data.newPos("workplacePos");
    public BlockPosParameter hangoutPos = data.newPos("hangoutPos");
    public CBooleanParameter isSleeping = data.newBoolean("isSleeping");

    // genes
    // TODO move into own class
    public CFloatParameter GENE_SIZE = data.newFloat("gene_size");
    public CFloatParameter GENE_WIDTH = data.newFloat("gene_width");
    public CFloatParameter GENE_BREAST = data.newFloat("gene_breast");
    public CFloatParameter GENE_MELANIN = data.newFloat("gene_melanin");
    public CFloatParameter GENE_HEMOGLOBIN = data.newFloat("gene_hemoglobin");
    public CFloatParameter GENE_EUMELANIN = data.newFloat("gene_eumelanin");
    public CFloatParameter GENE_PHEOMELANIN = data.newFloat("gene_pheomelanin");
    public CFloatParameter GENE_SKIN = data.newFloat("gene_skin");
    public CFloatParameter GENE_FACE = data.newFloat("gene_face");

    //personality and mood
    public CIntegerParameter PERSONALITY = data.newInteger("personality");
    public CIntegerParameter MOOD = data.newInteger("mood");

    // genes list
    public CFloatParameter[] GENES = new CFloatParameter[]{
            GENE_SIZE, GENE_WIDTH, GENE_BREAST, GENE_MELANIN, GENE_HEMOGLOBIN, GENE_EUMELANIN, GENE_PHEOMELANIN, GENE_SKIN, GENE_FACE};
    public static final String[] GENES_NAMES = new String[]{
            "gene_size", "gene_width", "gene_breast", "gene_melanin", "gene_hemoglobin", "gene_eumelanin", "gene_pheomelanin", "gene_skin", "gene_face"};

    public final CInventory inventory;
    public final CWorld world;

    private float swingProgressTicks;

    public EntityVillagerMCA(EntityType<? extends EntityVillagerMCA> type, World w) {
        super(type, w);
        inventory = new CInventory(this, 27);

        world = CWorld.fromMC(w);

        //register has to be here, not in initialize, since the super call is called before the field init
        // and the data manager required those fields
        data.register();

        this.setSilent(true);

        if (!world.isClientSide) {
            EnumGender eGender = EnumGender.getRandom();
            gender.set(eGender.getId());

            villagerName.set(API.getRandomName(eGender));
        }
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public ILivingEntityData finalizeSpawn(IServerWorld p_213386_1_, DifficultyInstance p_213386_2_, SpawnReason p_213386_3_, @Nullable ILivingEntityData p_213386_4_, @Nullable CompoundNBT p_213386_5_) {
        ILivingEntityData iLivingEntityData = super.finalizeSpawn(p_213386_1_, p_213386_2_, p_213386_3_, p_213386_4_, p_213386_5_);

        initializeGenes();
        initializeSkin();
        initializePersonality();

        //TODO big problem here, the profession changing AI...
        setProfession(ProfessionsMCA.randomProfession());

        return iLivingEntityData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.5D).add(Attributes.FOLLOW_RANGE, 48.0D);
    }


    public final VillagerProfession getProfession() {
        return this.getVillagerData().getProfession();
    }

    public final void setProfession(VillagerProfession profession) {
        this.setVillagerData(this.getVillagerData().setProfession(profession));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        //villager is peaceful and wont hurt as long as not necessary
        if (getPersonality() == EnumPersonality.PEACEFUL && getHealth() == getMaxHealth()) {
            return false;
        }

        //we don't use attributes
        float damage = getProfession() == MCA.PROFESSION_GUARD.get() ? 9.0F : 3.0F;
        float knockback = 3.0F;

        //personality bonus
        if (getPersonality() == EnumPersonality.WEAK) damage *= 0.75;
        if (getPersonality() == EnumPersonality.CONFIDENT) damage *= 1.25;
        if (getPersonality() == EnumPersonality.STRONG) damage *= 1.5;

        //enchantment
        if (target instanceof LivingEntity) {
            damage += EnchantmentHelper.getDamageBonus(this.getMainHandItem(), ((LivingEntity) target).getMobType());
            knockback += (float) EnchantmentHelper.getKnockbackBonus(this);
        }

        //fire aspect
        int i = EnchantmentHelper.getFireAspect(this);
        if (i > 0) {
            target.setSecondsOnFire(i * 4);
        }

        boolean damageDealt = target.hurt(DamageSource.mobAttack(this), damage);

        //knockback and post damage stuff
        if (damageDealt) {
            if (knockback > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity) target).knockback(knockback * 0.5F, (double) MathHelper.sin(this.yRot * ((float) Math.PI / 180F)), -MathHelper.cos(this.yRot * ((float) Math.PI / 180F)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
            }

            this.doEnchantDamageEffects(this, target);
            this.setLastHurtMob(target);
        }

        return damageDealt;
    }

    @Override
    public final ActionResultType interactAt(PlayerEntity player, Vector3d pos, @Nonnull Hand hand) {
        if (world.isClientSide) {
            Minecraft.getInstance().setScreen(new GuiInteract(this, player));
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.PASS;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);

        data.load(CNBT.fromMC(nbt));

        //verify clothes and hair
        clothes.set(API.getNextClothing(this, nbt.getString("clothes"), 0));
        Hair h = API.getNextHair(this, new Hair(
                hair.get(),
                hairOverlay.get()
        ), 0);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());

        //set speed
        float speed = 1.0f;

        //personality bonuses
        if (getPersonality() == EnumPersonality.ATHLETIC) speed *= 1.15;
        if (getPersonality() == EnumPersonality.SLEEPY) speed *= 0.8;

        //width and size impact
        speed /= GENE_WIDTH.get();
        speed *= GENE_SKIN.get();

        setSpeed(speed);
    }

    @Override
    public final void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        data.save(CNBT.fromMC(nbt));
    }

    private void initializeSkin() {
        clothes.set(API.getRandomClothing(this));

        Hair h = API.getRandomHair(this);
        hair.set(h.getTexture());
        hairOverlay.set(h.getOverlay());
    }

    private void initializePersonality() {
        PERSONALITY.set(EnumPersonality.getRandom().getId());
        MOOD.set(random.nextInt((EnumMood.maxLevel - EnumMood.minLevel) * EnumMood.levelsPerMood + 1) + EnumMood.minLevel * EnumMood.levelsPerMood);
    }

    //returns a float between 0 and 1, weighted at 0.5
    private float centeredRandom() {
        return (float) Math.min(1.0, Math.max(0.0, (random.nextFloat() - 0.5f) * (random.nextFloat() - 0.5f) + 0.5f));
    }

    //initializes the genes with random numbers
    private void initializeGenes() {
        for (CFloatParameter dp : GENES) {
            dp.set(random.nextFloat());
        }

        // size is more centered
        GENE_SIZE.set(centeredRandom());
        GENE_WIDTH.set(centeredRandom());

        //temperature
        float temp = world.getBiome(getOnPos()).getBaseTemperature();

        // melanin
        GENE_MELANIN.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));
        GENE_HEMOGLOBIN.set(Util.clamp((random.nextFloat() - 0.5f) * 0.5f + temp * 0.5f));

        // TODO hair tend to have similar values than hair, but the used LUT is a little bit random
        GENE_EUMELANIN.set(random.nextFloat());
        GENE_PHEOMELANIN.set(random.nextFloat());
    }

    //interpolates and mutates the genes from two parent villager
    public void inheritGenes(EntityVillagerMCA mother, EntityVillagerMCA father) {
        for (int i = 0; i < GENES.length; i++) {
            float m = mother.GENES[i].get();
            float f = father.GENES[i].get();
            float interpolation = random.nextFloat();
            float mutation = (random.nextFloat() - 0.5f) * 0.2f;
            float g = m * interpolation + f * (1.0f - interpolation) + mutation;
            GENES[i].set((float) Math.min(1.0, Math.max(0.0, g)));
        }
    }

    @Override
    public final boolean hurt(DamageSource source, float damageAmount) {
        // Guards take 50% less damage
        if (getProfession() == MCA.PROFESSION_GUARD.get()) {
            damageAmount *= 0.5;
        }

        //personality bonus
        if (getPersonality() == EnumPersonality.TOUGH) damageAmount *= 0.5;
        if (getPersonality() == EnumPersonality.FRAGILE) damageAmount *= 1.25;

        if (!world.isClientSide) {
            if (source.getEntity() instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) source.getEntity();
                sendMessageTo(MCA.localize("villager.hurt"), p);
            }

            if (source.getDirectEntity() instanceof ZombieEntity && getProfession() != MCA.PROFESSION_GUARD.get() && MCA.getConfig().enableInfection && random.nextFloat() < MCA.getConfig().infectionChance / 100.0) {
                isInfected.set(true);
            }
        }

        return super.hurt(source, damageAmount);
    }

    @Override
    public void tick() {
        super.tick();

        updateSwinging();

        if (tickCount % 100 == 0 && !world.isClientSide) {
            reportBuildings();
        }

        if (world.isClientSide) {
            onEachClientUpdate();
        } else {
            onEachServerUpdate();
        }
    }

    //report potential buildings within this villagers reach
    private void reportBuildings() {
        VillageManagerData manager = VillageManagerData.get(world);

        Stream<BlockPos> stream = ((ServerWorld) level).getPoiManager().findAll(
                PointOfInterestType.HOME.getPredicate(),
                (p) -> !manager.cache.contains(p),
                getOnPos(),
                48,
                PointOfInterestManager.Status.ANY);

        stream.forEach((pos) -> manager.reportBuilding(level, pos));
    }

    public void sendMessageTo(String message, Entity receiver) {
        receiver.sendMessage(new StringTextComponent(message), receiver.getUUID());
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);

        if (!world.isClientSide) {
            //The death of a villager negatively modifies the mood of nearby villagers
            for (EntityVillagerMCA villager : world.getCloseEntities(this, 32.0D, EntityVillagerMCA.class)) {
                villager.modifyMoodLevel(-10);
            }

            //TODO: player memory gets lost on revive
            //TODO: childp becomes to child on revive (needs verification)

            inventory.dropAllItems();

            if (isMarried()) {
                UUID spouse = spouseUUID.get().orElse(Constants.ZERO_UUID);
                Entity sp = world.getEntityByUUID(spouse);
                PlayerSaveData playerSaveData = PlayerSaveData.get(world, spouse);

                // Notify spouse of the death
                if (sp instanceof EntityVillagerMCA) {
                    ((EntityVillagerMCA) sp).endMarriage();
                } else if (playerSaveData != null) {
                    playerSaveData.endMarriage();
                    PlayerEntity player = world.getPlayerEntityByUUID(spouse);
                    if (player != null) {
                        //TODO store message in case player was offline
                    }
                }
            }

            // Notify all parents of the death
            ParentPair parents = getParents();
            Arrays.stream(parents.getBothParentEntities(world))
                    .filter(e -> e instanceof PlayerEntity)
                    .forEach(e -> {
                        //TODO store message in case player was offline
                    });

            SavedVillagers.get(world).saveVillager(this);
        }
    }

    @Override
    public final SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    protected final SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected final SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    public final ITextComponent getDisplayName() {
        return new StringTextComponent(villagerName.get());
    }

    @Override
    public final ITextComponent getCustomName() {
        return new StringTextComponent(villagerName.get());
    }

    private void updateSwinging() {
        if (isSwinging.get()) {
            swingProgressTicks++;

            if (swingProgressTicks >= 8) {
                swingProgressTicks = 0;
                isSwinging.set(false);
            }
        } else {
            swingProgressTicks = 0;
        }
    }

    public Memories getMemoriesForPlayer(PlayerEntity player) {
        CNBT cnbt = memories.get();
        CNBT compoundTag = cnbt.getCompoundTag(player.getUUID().toString());
        Memories returnMemories = Memories.fromCNBT(this, compoundTag);
        if (returnMemories == null) {
            returnMemories = Memories.getNew(this, player.getUUID());
            memories.set(memories.get().setTag(player.getUUID().toString(), returnMemories.toCNBT()));
        }
        return returnMemories;
    }

    public EnumPersonality getPersonality() {
        return EnumPersonality.getById(PERSONALITY.get());
    }

    public EnumMood getMood() {
        return getPersonality().getMoodGroup().getMood(MOOD.get());
    }

    public void modifyMoodLevel(int mood) {
        MOOD.set(MOOD.get() + mood);
    }

    public int getMoodLevel() {
        return MOOD.get();
    }

    private void goHome(PlayerEntity player) {
        if (getHome().equals(BlockPos.ZERO)) {
            say(player, "interaction.gohome.fail");
        } else {
            BlockPos home = getHome();
            if (!this.getNavigation().moveTo(home.getX(), home.getY(), home.getZ(), getSpeed())) {
                teleportTo(home.getX(), home.getY(), home.getZ());
            }
            say(player, "interaction.gohome.success");
        }
    }

    public BlockPos getWorkplace() {
        return workplacePos.get();
    }

    public BlockPos getHangout() {
        return hangoutPos.get();
    }

    public BlockPos getHome() {
        return bedPos.get();
    }

    private void setHome(PlayerEntity player) {
        say(player, "interaction.sethome.success");
        bedPos.set(player.blockPosition());
    }

    public void setWorkplace(PlayerEntity player) {
        say(player, "interaction.setworkplace.success");
        workplacePos.set(player.blockPosition());
    }

    public void setHangout(PlayerEntity player) {
        say(player, "interaction.sethangout.success");
        hangoutPos.set(player.blockPosition());
    }

    public void say(PlayerEntity target, String phraseId, String... params) {
        ArrayList<String> paramList = new ArrayList<>();
        Collections.addAll(paramList, params);

        // Player is always first in params passed to localizer for say().
        paramList.add(0, target.getName().getString());

        String chatPrefix = MCA.getConfig().villagerChatPrefix + getDisplayName().getString() + ": ";
        if (isInfected.get()) { // Infected villagers do not speak
            sendMessageTo(chatPrefix + "???", target);
            playSound(SoundEvents.ZOMBIE_AMBIENT, this.getSoundVolume(), this.getVoicePitch());
        } else {
            EnumDialogueType dialogueType = getMemoriesForPlayer(target).getDialogueType();
            sendMessageTo(chatPrefix + MCA.localize(dialogueType.getName() + "." + phraseId, paramList.toArray(new String[0])), target);
        }
    }

    public boolean isMarried() {
        return !spouseUUID.get().orElse(Constants.ZERO_UUID).equals(Constants.ZERO_UUID);
    }

    public boolean isMarriedTo(UUID uuid) {
        return spouseUUID.get().orElse(Constants.ZERO_UUID).equals(uuid);
    }

    public void marry(PlayerEntity player) {
        spouseUUID.set(player.getUUID());
        spouseName.set(player.getName().getContents());
        marriageState.set(EnumMarriageState.MARRIED.getId());
    }

    public void marry(EntityVillagerMCA spouse) {
        spouseUUID.set(spouse.getUUID());
        spouseName.set(spouse.villagerName.get());
        marriageState.set(EnumMarriageState.MARRIED.getId());
    }

    private void endMarriage() {
        spouseUUID.set(Constants.ZERO_UUID);
        spouseName.set("");
        marriageState.set(EnumMarriageState.NOT_MARRIED.getId());
    }

    private void handleInteraction(PlayerEntity player, Memories memory, APIButton button) {
        //interaction
        String interactionName = button.getIdentifier().replace("gui.button.", "");
        EnumInteraction interaction = EnumInteraction.fromName(interactionName);

        //success chance and hearts
        float successChance = 0.85F;
        int heartsBoost = 5;
        if (interaction != null) {
            heartsBoost = interaction.getHearts(this, memory);
            successChance = interaction.getSuccessChance(this, memory) / 100.0f;
        }

        boolean succeeded = random.nextFloat() < successChance;

        //sensitive people doubles the loss
        if (!succeeded && getPersonality() == EnumPersonality.SENSITIVE) {
            heartsBoost *= 2;
        }

        memory.modInteractionFatigue(1);
        memory.modHearts(succeeded ? heartsBoost : (heartsBoost * -1));
        modifyMoodLevel(succeeded ? heartsBoost : (heartsBoost * -1));

        String responseId = String.format("%s.%s", interactionName, succeeded ? "success" : "fail");
        say(player, responseId);
    }

    public void handleInteraction(PlayerEntity player, String guiKey, String buttonId) {
        Memories memory = getMemoriesForPlayer(player);
        java.util.Optional<APIButton> button = API.getButtonById(guiKey, buttonId);
        if (!button.isPresent()) {
            MCA.log("Button not found for key and ID: " + guiKey + ", " + buttonId);
        } else if (button.get().isInteraction()) handleInteraction(player, memory, button.get());

        Hair h;
        switch (buttonId) {
            case "gui.button.move":
                moveState.set(EnumMoveState.MOVE.getId());
                this.playerToFollowUUID.set(Constants.ZERO_UUID);
                break;
            case "gui.button.stay":
                moveState.set(EnumMoveState.STAY.getId());
                break;
            case "gui.button.follow":
                moveState.set(EnumMoveState.FOLLOW.getId());
                this.playerToFollowUUID.set(player.getUUID());
                stopChore();
                break;
            case "gui.button.ridehorse":
//                toggleMount(player);
                break;
            case "gui.button.sethome":
                setHome(player);
                break;
            case "gui.button.gohome":
                goHome(player);
                break;
            case "gui.button.setworkplace":
                setWorkplace(player);
                break;
            case "gui.button.sethangout":
                setHangout(player);
                break;
            case "gui.button.trade":
                super.mobInteract(player, Hand.MAIN_HAND);
                break;
            case "gui.button.inventory":
                player.openMenu(this);
                break;
            case "gui.button.gift":
                ItemStack stack = player.getMainHandItem();
                if (!stack.isEmpty()) {
                    int giftValue = API.getGiftValueFromStack(stack);
                    if (!handleSpecialCaseGift(player, stack)) {
                        if (stack.getItem() == Items.GOLDEN_APPLE) isInfected.set(false);
                        else {
                            modifyMoodLevel(giftValue / 4 + 2);
                            memory.modHearts(giftValue);
                            say(player, API.getResponseForGift(stack));
                        }
                    }
                    if (giftValue > 0) {
                        player.getMainHandItem().shrink(1);
                    }
                }
                break;
            case "gui.button.procreate":
                if (PlayerSaveData.get(world, player.getUUID()).isBabyPresent())
                    say(player, "interaction.procreate.fail.hasbaby");
                else if (memory.getHearts() < 100) say(player, "interaction.procreate.fail.lowhearts");
                else {
                    //TODO
//                    EntityAITasks.EntityAITaskEntry task = tasks.taskEntries.stream().filter((ai) -> ai.action instanceof EntityAIProcreate).findFirst().orElse(null);
//                    if (task != null) {
//                        ((EntityAIProcreate) task.action).procreateTimer = 20 * 3; // 3 seconds
//                        isProcreating.set(true);
//                    }
                }
                break;
            case "gui.button.infected":
                isInfected.set(!isInfected.get());
                break;
            case "gui.button.clothing.randClothing":
                clothes.set(API.getRandomClothing(this));
                break;
            case "gui.button.clothing.prevClothing":
                clothes.set(API.getNextClothing(this, clothes.get(), -1));
                break;
            case "gui.button.clothing.nextClothing":
                clothes.set(API.getNextClothing(this, clothes.get()));
                break;
            case "gui.button.clothing.randHair":
                h = API.getRandomHair(this);
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.clothing.prevHair":
                h = API.getNextHair(this, new Hair(hair.get(), hairOverlay.get()), -1);
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.clothing.nextHair":
                h = API.getNextHair(this, new Hair(hair.get(), hairOverlay.get()));
                hair.set(h.getTexture());
                hairOverlay.set(h.getOverlay());
                break;
            case "gui.button.profession":
                setProfession(ProfessionsMCA.randomProfession());
//                applySpecialAI();
                break;
            case "gui.button.prospecting":
                startChore(EnumChore.PROSPECT, player);
                break;
            case "gui.button.hunting":
                startChore(EnumChore.HUNT, player);
                break;
            case "gui.button.fishing":
                startChore(EnumChore.FISH, player);
                break;
            case "gui.button.chopping":
                startChore(EnumChore.CHOP, player);
                break;
            case "gui.button.harvesting":
                startChore(EnumChore.HARVEST, player);
                break;
            case "gui.button.stopworking":
                stopChore();
                break;
            case "gui.button.village":
                //Village village = VillageHelper.findClosestVillage(world, this.getPos());
                //TODO somebody decided to remove villages....
//                if (village != null) {
//                    String phrase = MCA.localize("events.village",
//                            String.valueOf(village.getVillageRadius()),
//                            String.valueOf(village.getNumVillagers()),
//                            String.valueOf(village.getNumVillageDoors())
//                    );
//                    player.sendMessage(phrase);
//                } else {
                sendMessageTo("I wasn't able to find a village.", player);
//                }
        }
    }

    private boolean handleSpecialCaseGift(PlayerEntity player, ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ItemSpecialCaseGift) {
            if (((ItemSpecialCaseGift) item).handle(player, this)) {
                player.getMainHandItem().shrink(1);
            }
            return true;
        } else if (item == Items.CAKE) {
            if (isMarried() && !isBaby()) {
                Entity spouse = world.getEntityByUUID(spouseUUID.get().orElse(Constants.ZERO_UUID));
                if (spouse instanceof EntityVillagerMCA) {
                    EntityVillagerMCA progressor = gender.get() == EnumGender.FEMALE.getId() ? this : (EntityVillagerMCA) spouse;
                    progressor.hasBaby.set(true);
                    progressor.isBabyMale.set(random.nextBoolean());
                    addParticlesAroundSelf(ParticleTypes.HEART);
                    say(player, "gift.cake.success");
                } else {
                    say(player, "gift.cake.fail");
                }
                return true;
            }
        } else if (item == Items.GOLDEN_APPLE && this.isBaby()) {
            // increase age by 5 minutes
            this.ageUp(1200 * 5);
            return true;
        }

        return false;
    }

    public void addParticlesAroundSelfPublic(IParticleData p) {
        addParticlesAroundSelf(p);
    }

    private void onEachClientUpdate() {
        if (isProcreating.get()) {
            this.yHeadRot += 50.0F;
        }

        if (this.tickCount % 20 == 0) {
            onEachClientSecond();
        }
    }

    private void onEachClientSecond() {

    }

    private void onEachServerUpdate() {
        if (this.tickCount % 20 == 0) { // Every second
            onEachServerSecond();
        }

        if (this.tickCount % 200 == 0 && this.getHealth() > 0.0F) { // Every 10 seconds and when we're not already dead
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getHealth() + 1.0F); // heal
            }
        }

        //check if another state has been reached
        EnumAgeState last = EnumAgeState.byId(ageState.get());
        EnumAgeState next = EnumAgeState.byCurrentAge(getAge());
        if (last != next) {
            ageState.set(next.getId());

            if (next == EnumAgeState.ADULT) {
                // Notify player parents of the age up and set correct dialogue type.
                Entity[] parents = getParents().getBothParentEntities(world);
                Arrays.stream(parents).filter(e -> e instanceof PlayerEntity).map(e -> (PlayerEntity) e).forEach(p -> {
                    getMemoriesForPlayer(p).setDialogueType(EnumDialogueType.ADULT);
                    sendMessageTo(MCA.localize("notify.child.grownup", villagerName.get()), p);
                });

                // Change profession away from child for villager children.
                if (getProfession() == MCA.PROFESSION_CHILD.get()) {
                    setProfession(API.randomProfession().getMcProfession());
                }
            }
        }
    }

    private void onEachServerSecond() {
        // villager has a baby
        if (hasBaby.get()) {
            babyAge.set(babyAge.get() + 1);

            // grow up time is in minutes and we measure age in seconds
            if (babyAge.get() >= MCA.getConfig().babyGrowUpTime * 60) {
                EnumGender gender = isBabyMale.get() ? EnumGender.MALE : EnumGender.FEMALE;
                EntityVillagerMCA child = new EntityVillagerMCA(MCA.ENTITYTYPE_VILLAGER.get(), level);
                child.gender.set(gender.getId());
                child.setPos(this.getX(), this.getY(), this.getZ());
                //noinspection OptionalGetWithoutIsPresent
                child.parents.set(ParentPair.create(this.getUUID(), this.spouseUUID.get().get(), villagerName.get(), spouseName.get()).toNBT());
                world.spawnEntity(child);

                hasBaby.set(false);
                babyAge.set(0);
            }
        }
    }

//    @Override
//    protected void initializeAI() {
//        super.initEntityAI();
//
//        this.tasks.addTask(0, new EntityAIProspecting(this));
//        this.tasks.addTask(0, new EntityAIHunting(this));
//        this.tasks.addTask(0, new EntityAIChopping(this));
//        this.tasks.addTask(0, new EntityAIHarvesting(this));
//        this.tasks.addTask(0, new EntityAIFishing(this));
//        this.tasks.addTask(0, new EntityAIMoveState(this));
//        this.tasks.addTask(0, new EntityAIAgeBaby(this));
//        this.tasks.addTask(0, new EntityAIProcreate(this));
//        this.tasks.addTask(5, new EntityAIGoWorkplace(this));
//        this.tasks.addTask(6, new EntityAIWork(this));
//        this.tasks.addTask(5, new EntityAIGoHangout(this));
//        this.tasks.addTask(1, new EntityAISleeping(this));
//        this.tasks.addTask(10, new EntityAIWatchClosest(this, PlayerEntity.class, 8.0F));
//        this.tasks.addTask(10, new EntityAILookIdle(this));
//    }

//    private void applySpecialAI() {
//        if (getProfession() == ProfessionsMCA.bandit) {
//            this.tasks.taskEntries.clear();
//            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
//            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));
//
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, BANDIT_TARGET_SELECTOR));
//            this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, PlayerEntity.class, true));
//        } else if (getProfession() == ProfessionsMCA.guard) {
//            removeCertainTasks(EntityAIAvoidEntity.class);
//
//            this.tasks.addTask(1, new EntityAIAttackMelee(this, 0.8D, false));
//            this.tasks.addTask(2, new EntityAIMoveThroughVillage(this, 0.6D, false));
//
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVillagerMCA.class, 100, false, false, GUARD_TARGET_SELECTOR));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVex.class, 100, false, false, null));
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityVindicator.class, 100, false, false, null));
//        } else {
//            //every other villager is allowed to defend itself from zombies while fleeing
//            this.tasks.addTask(0, new EntityAIDefendFromTarget(this));
//
//            this.targetTasks.taskEntries.clear();
//            this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 100, false, false, null));
//        }
//    }

    //guards should not run away from zombies
    //TODO: should only avoid zombies when low on health
//    private void removeCertainTasks(Class typ) {
//        Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.tasks.taskEntries.iterator();
//
//        while (iterator.hasNext()) {
//            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = iterator.next();
//            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;
//
//            if (entityaibase.getClass().equals(typ)) {
//                iterator.remove();
//            }
//        }
//    }

    public void stopChore() {
        activeChore.set(EnumChore.NONE.getId());
        choreAssigningPlayer.set(Constants.ZERO_UUID);
    }

    public void startChore(EnumChore chore, PlayerEntity player) {
        activeChore.set(chore.getId());
        choreAssigningPlayer.set(player.getUUID());
    }

    public boolean playerIsParent(PlayerEntity player) {
        ParentPair data = ParentPair.fromNBT(parents.get());
        return data.getParent1UUID().equals(player.getUUID()) || data.getParent2UUID().equals(player.getUUID());
    }

    public String getCurrentActivity() {
        EnumMoveState ms = EnumMoveState.byId(moveState.get());
        if (ms != EnumMoveState.MOVE) {
            return ms.getFriendlyName();
        }

        EnumChore chore = EnumChore.byId(activeChore.get());
        if (chore != EnumChore.NONE) {
            return chore.getFriendlyName();
        }

        return null;
    }

    public ParentPair getParents() {
        return ParentPair.fromNBT(parents.get());
    }

    public void updateMemories(Memories memories) {
        CNBT nbt = this.memories.get().copy();
        nbt.setTag(memories.getPlayerUUID().toString(), memories.toCNBT());
        this.memories.set(nbt);
    }

    @Nullable
    @Override
    @ParametersAreNonnullByDefault
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return ChestContainer.threeRows(i, playerInventory, inventory);
    }

    public EnumAgeState getAgeState() {
        return EnumAgeState.byId(ageState.get());
    }
}