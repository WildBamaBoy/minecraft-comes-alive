package mca.client.model;

import org.lwjgl.opengl.GL11;

import mca.core.MCA;
import mca.entity.EntityHuman;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;

public class ModelHuman extends ModelBiped
{
	private ModelRenderer breasts;

	public ModelHuman()
	{
		super(0.5F);

		breasts = new ModelRenderer(this, 18, 21);
		breasts.addBox(-3F, 0F, -1F, 6, 3, 3);
		breasts.setRotationPoint(0F, 3.5F, -3F);
		breasts.setTextureSize(64, 64);
		breasts.mirror = true;

		setRotation(breasts, 1.07818F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f1, float f2, float f3, float f4, float f5, float f6) 
	{
		final EntityHuman human = (EntityHuman)entity;
		final double scale = 0.9375D;
		
		this.setRotationAngles(f1, f2, f3, f4, f5, f6, entity);

		//Default head texture is going to be the one we can get straight from the human object.
		String headTexture = human.getHeadTexture();
		String clothesTexture = human.getClothesTexture();

		//But if the human is infected, we need to show the zombified skin instead. Check for this here.
		if (human.getIsInfected())
		{
			String textureBase = "mca:textures/skins/";

			headTexture = human.getIsMale() ? textureBase + "ZombieVillagerMale.png" : textureBase + "ZombieVillagerFemale.png";
			clothesTexture = headTexture;
		}

		//Bind the head texture to the head and headwear.
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(headTexture));
		
		GL11.glPushMatrix();
		{
			GL11.glTranslated(0.0D, -0.05D, 0.0D);
			GL11.glScaled(scale, scale, scale);
			this.bipedHead.render(f6);
			this.bipedHeadwear.render(f6);
		}
		GL11.glPopMatrix();
		
		//Bind the clothes texture to the rest of the body.
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(clothesTexture));

		GL11.glPushMatrix();
		{
			GL11.glScaled(scale, scale, scale);
			this.bipedBody.render(f6);
			this.bipedRightLeg.render(f6);
			this.bipedLeftLeg.render(f6);
		}
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glScaled(scale, scale, scale);
			GL11.glTranslated(0.0D, 0.0D, 0.0D);
			this.bipedRightArm.render(f6);
			this.bipedLeftArm.render(f6);
		}
		GL11.glPopMatrix();
		
		if (!human.getIsMale() && !human.getIsChild() && MCA.getConfig().modifyFemaleBody && human.getItemStackFromSlot(EntityEquipmentSlot.CHEST) == null)
		{
			GL11.glPushMatrix();
			{
				//Correct scaling and location.
				GL11.glTranslated(0.0D, 0.0D, 0.005D);
				GL11.glScaled(1.15D, 1.0D, 1.0D);
				breasts.render(f6);
			}
			GL11.glPopMatrix();
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
