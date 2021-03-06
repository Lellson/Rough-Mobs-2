package de.lellson.roughmobs2.ai.combat;

import java.util.List;

import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;

public class RoughAIAggressiveTouch extends EntityAIBase {
	
	protected EntityLiving entity;
	
	private List<EntityPlayer> players;
	
	public RoughAIAggressiveTouch(EntityLiving entity) {
		this.entity = entity;
		this.setMutexBits(4);
	}

	@Override
	public boolean shouldExecute() {
		return !(players = entity.world.getEntitiesWithinAABB(EntityPlayer.class, entity.getEntityBoundingBox().expand(0.1, 0.1, 0.1))).isEmpty();
	}
	
	@Override
	public void updateTask() {
		
		for (EntityPlayer player : players) 
		{
			entity.setRevengeTarget(player);
			entity.setAttackTarget(player);
			FeatureHelper.playSound(entity, SoundEvents.ENTITY_ZOMBIE_PIG_ANGRY);
			break;
		}
	}
}
