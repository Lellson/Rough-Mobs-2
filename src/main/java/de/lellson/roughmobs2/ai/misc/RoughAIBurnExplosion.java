package de.lellson.roughmobs2.ai.misc;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;

public class RoughAIBurnExplosion extends EntityAIBase {

	private EntityCreeper creeper;
	
	public RoughAIBurnExplosion(EntityCreeper creeper) {
		this.creeper = creeper;
		this.setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {
		return creeper.isEntityAlive() && creeper.isBurning();
	}
	
	@Override
	public void updateTask() {
		creeper.setCreeperState(1);
	}
}
