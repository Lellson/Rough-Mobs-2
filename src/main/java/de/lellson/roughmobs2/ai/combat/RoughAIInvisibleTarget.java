package de.lellson.roughmobs2.ai.combat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RoughAIInvisibleTarget extends EntityAIBase {

	protected EntityLiving entity;
	protected double range;
	
	public RoughAIInvisibleTarget(EntityLiving entity, double range) {
		this.entity = entity;
		this.range = range;
		this.setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {
		return entity.getAttackTarget() != null && entity.getDistance(entity.getAttackTarget()) < range && entity.getActivePotionEffect(MobEffects.INVISIBILITY) == null;
	}
	
	@Override
	public void startExecuting() {
		entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 100, 0, false, false));
	}
}
