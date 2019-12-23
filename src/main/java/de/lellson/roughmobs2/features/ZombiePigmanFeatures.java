package de.lellson.roughmobs2.features;

import java.util.List;

import de.lellson.roughmobs2.ai.combat.RoughAIAggressiveTouch;
import de.lellson.roughmobs2.ai.combat.RoughAIMobBuff;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class ZombiePigmanFeatures extends EntityFeatures {
	
	private boolean aggressiveTouch;
	
	private float aggressiveBlockRange;
	private int aggressiveBlockChance;
	
	public ZombiePigmanFeatures() {
		super("zombie pigman", EntityPigZombie.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		aggressiveTouch = RoughConfig.getBoolean(name, "AggressiveTouch", true, "Set to false to prevent zombie pigman from getting aggressive if the player touches its hitbox");
		
		aggressiveBlockRange = RoughConfig.getFloat(name, "AggressiveBlockRange", 20, 1, MAX, "Block radius in which zombie pigman get aggressive if the player breaks blocks");
		aggressiveBlockChance = RoughConfig.getInteger(name, "AggressiveBlockChance", 10, 0, MAX, "Chance (1 in X) that a zombie pigman gets aggressive if the player breaks nearby blocks");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (entity instanceof EntityLiving && aggressiveTouch)
			tasks.addTask(1, new RoughAIAggressiveTouch((EntityLiving) entity));
	}
	
	@Override
	public void onBlockBreak(EntityPlayer player, BreakEvent event) {
		
		for (Class<? extends Entity> clazz : entityClasses)
		{
			List<Entity> entities = player.world.getEntitiesWithinAABB(clazz, player.getEntityBoundingBox().expand(aggressiveBlockRange, aggressiveBlockRange, aggressiveBlockRange));
			
			for (Entity entity : entities)
			{
				if (aggressiveBlockChance > 0 && entity instanceof EntityLiving && player.world.rand.nextInt(aggressiveBlockChance) == 0)
				{
					EntityLiving living = (EntityLiving) entity;
					living.setAttackTarget(player);
					living.setRevengeTarget(player);
					FeatureHelper.playSound(living, SoundEvents.ENTITY_ZOMBIE_PIG_ANGRY);
				}
			}
		}
	}
}
