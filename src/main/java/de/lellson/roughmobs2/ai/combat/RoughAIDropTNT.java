package de.lellson.roughmobs2.ai.combat;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityTNTPrimed;

public class RoughAIDropTNT extends EntityAIBase {

	protected EntityLiving entity;
	
	public RoughAIDropTNT(EntityLiving entity) {
		this.entity = entity;
		this.setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {
		
		List<EntityLivingBase> entities = entity.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(3, 0, 3).expand(0, -60, 0));
		
		for (Entity target : entities)
		{
			if (entity.getAttackTarget() == target)
				return true;
		}
			
		return false;
	}
	
	@Override
	public void startExecuting() {
		
		EntityTNTPrimed tnt = new EntityTNTPrimed(entity.world, entity.posX, entity.posY-1, entity.posZ, entity);
		entity.world.spawnEntity(tnt);
	}
}
