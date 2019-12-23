package de.lellson.roughmobs2.features;

import java.util.ArrayList;
import java.util.List;

import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class EndermanFeatures extends EntityFeatures {

	private boolean dropEnderpearl;
	
	private boolean teleportAttacker;
	private String immunityItem;
	
	private int blindChance;
	private int blindDuration;
	
	private int stealItemChance;
	
	private Item immunityItemItem;
	
	public EndermanFeatures() {
		super("enderman", EntityEnderman.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		dropEnderpearl = RoughConfig.getBoolean(name, "DropEnderpearl", true, "Set this to false to prevent endermen from dropping an extra enderpearl on death");
		teleportAttacker = RoughConfig.getBoolean(name, "TeleportAttacker", true, "Set this to false to prevent endermen from teleporting attackers away");
		immunityItem = RoughConfig.getString(name, "ImmunityItem", "minecraft:ender_eye", "If this item is somewhere in the players inventory, the player becomes immune to teleportation and item stealing\nLeave this empty to disable this feature");
		
		blindChance = RoughConfig.getInteger(name, "BlindChance", 3, 0, MAX, "Chance (1 in X) that an %s applies the blindness effect to its target\nSet to 0 to disable this feature");
		blindDuration = RoughConfig.getInteger(name, "BlindDuration", 200, 1, MAX, "Duration in ticks of the applied blindness effect (20 ticks = 1 second)");
		
		stealItemChance = RoughConfig.getInteger(name, "StealItemChance", 3, 0, MAX, "Chance (1 in X) that an %s steals the targets held or equipped item to drop it on the ground\nSet to 0 to disable this feature");
	}
	
	@Override
	public void postInit() {
		immunityItemItem = Item.REGISTRY.getObject(new ResourceLocation(immunityItem));
	}
	
	@Override
	public void onDeath(Entity deadEntity, DamageSource source) {
		if (dropEnderpearl)
			deadEntity.dropItem(Items.ENDER_PEARL, 1);
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (teleportAttacker && attacker instanceof EntityLivingBase)
		{
			if (isImmuneToTeleport(attacker))
			{
				FeatureHelper.playSound(attacker, SoundEvents.ENTITY_ENDEREYE_DEATH);
			}
			else
				teleportRandom((EntityLivingBase)attacker, 24);
		}
	}

	@Override
	public void onAttack(Entity attacker, Entity immediateAttacker, Entity target, LivingAttackEvent event) {

		if (!(target instanceof EntityLivingBase))
			return;
		
		if (blindChance > 0 && RND.nextInt(blindChance) == 0) 
		{
			((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, blindDuration));
		}
		
		if (stealItemChance > 0 && RND.nextInt(stealItemChance) == 0) 
		{
			if (isImmuneToTeleport(attacker))
			{
				FeatureHelper.playSound(attacker, SoundEvents.ENTITY_ENDEREYE_DEATH);
			}
			else
				tryDropHeldItem((EntityLivingBase)target, attacker);
		}
	}
	
	private void tryDropHeldItem(EntityLivingBase target, Entity attacker) {
		
		List<EnumHand> filledHands = new ArrayList<EnumHand>();
		
		if (!target.getHeldItemMainhand().isEmpty())
			filledHands.add(EnumHand.MAIN_HAND);
		
		if (!target.getHeldItemOffhand().isEmpty())
			filledHands.add(EnumHand.OFF_HAND);
			
		if (filledHands.isEmpty())
			return;
		
		EnumHand hand = filledHands.get(RND.nextInt(filledHands.size()));
		ItemStack heldStack = target.getHeldItem(hand).copy();
		target.setHeldItem(hand, ItemStack.EMPTY);
		attacker.entityDropItem(heldStack, (float)(Math.random() + 0.5f));
	}

	public static boolean teleportRandom(EntityLivingBase entity) {
		return teleportRandom(entity, 64);
	}
	
	public static boolean teleportRandom(EntityLivingBase entity, double multi) {
		
		if (entity == null || entity instanceof FakePlayer || entity.isDead)
        	return false;
		
        double x = entity.posX + (RND.nextDouble() - 0.5D) * multi;
        double y = entity.posY + (RND.nextInt(64) - 32);
        double z = entity.posZ + (RND.nextDouble() - 0.5D) * multi;
        
        EnderTeleportEvent event = new EnderTeleportEvent(entity, x, y, z, 0);
        
        if (MinecraftForge.EVENT_BUS.post(event) || event == null || entity == null) 
        	return false;

        boolean flag = entity.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (flag)
        {
        	FeatureHelper.playSound(entity, SoundEvents.ENTITY_ENDERMEN_TELEPORT);
        }

        return flag;
    }
	
	private boolean isImmuneToTeleport(Entity attacker) {
		return immunityItemItem != null && attacker instanceof EntityPlayer && ((EntityPlayer)attacker).inventory.hasItemStack(new ItemStack(immunityItemItem));
	}
}
