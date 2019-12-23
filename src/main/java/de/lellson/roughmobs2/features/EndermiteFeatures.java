package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.ai.misc.RoughAIAddEffect;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class EndermiteFeatures extends EntityFeatures {

	private boolean teleportAttacker;
	private String immunityItem;
	
	private boolean witherMobs;
	
	private Item immunityItemItem;
	
	public EndermiteFeatures() {
		super("endermite", EntityEndermite.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		teleportAttacker = RoughConfig.getBoolean(name, "TeleportAttacker", true, "Set this to false to prevent %ss from teleporting attackers away");
		immunityItem = RoughConfig.getString(name, "ImmunityItem", "minecraft:ender_eye", "If this item is somewhere in the players inventory, the player becomes immune to teleportation\nLeave this empty to disable this feature");
	
		witherMobs = RoughConfig.getBoolean(name, "WitherMobs", true, "Set this to false to prevent %ss from applying the wither effect to near entities");
	}
	
	@Override
	public void postInit() {
		immunityItemItem = Item.REGISTRY.getObject(new ResourceLocation(immunityItem));
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (witherMobs && entity instanceof EntityLiving)
			tasks.addTask(1, new RoughAIAddEffect((EntityLiving)entity, MobEffects.WITHER, 6));
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (teleportAttacker && attacker instanceof EntityLivingBase)
		{
			if (isImmuneToTeleport(attacker))
			{
				attacker.world.playSound((EntityPlayer)null, attacker.prevPosX, attacker.prevPosY, attacker.prevPosZ, SoundEvents.ENTITY_ENDEREYE_DEATH, attacker.getSoundCategory(), 1.0F, 1.0F);
				attacker.playSound(SoundEvents.ENTITY_ENDEREYE_DEATH, 1.0F, 1.0F);
			}
			else
				EndermanFeatures.teleportRandom((EntityLivingBase)attacker, 16);
		}
	}
	
	private boolean isImmuneToTeleport(Entity attacker) {
		return immunityItemItem != null && attacker instanceof EntityPlayer && ((EntityPlayer)attacker).inventory.hasItemStack(new ItemStack(immunityItemItem));
	}
}
