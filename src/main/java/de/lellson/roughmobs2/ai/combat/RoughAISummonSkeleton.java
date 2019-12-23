package de.lellson.roughmobs2.ai.combat;

import java.util.List;

import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;

public class RoughAISummonSkeleton extends EntityAIBase {
	
	protected int timer;
	
	protected EntityLiving entity;
	protected int delay;
	protected boolean wither;
	
	public RoughAISummonSkeleton(EntityLiving entity, int delay, boolean wither) {
		this.entity = entity;
		this.delay = delay;
		this.timer = delay;
		this.wither = wither;
		this.setMutexBits(4);
	}

	@Override
	public boolean shouldExecute() {
		
		if (this.timer > 0)
			this.timer--;
		
		if (this.timer == 0)
		{
			List<EntityLivingBase> entities = entity.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(7, 5, 7));
			
			for (Entity target : entities)
			{
				if (entity.getAttackTarget() == target)
				{
					this.timer = this.delay;
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void startExecuting() {

		AbstractSkeleton skeleton;
		if (wither)
			skeleton = new EntityWitherSkeleton(entity.getEntityWorld());
		else
			skeleton = new EntitySkeleton(entity.getEntityWorld());
		
		
		skeleton.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
		skeleton.onInitialSpawn(entity.getEntityWorld().getDifficultyForLocation(entity.getPosition()), null);
		
		FeatureHelper.spawnParticle(skeleton, EnumParticleTypes.SMOKE_NORMAL, 5);
		
		entity.getEntityWorld().spawnEntity(skeleton);
	}
}
