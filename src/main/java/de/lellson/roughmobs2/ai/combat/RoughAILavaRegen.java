package de.lellson.roughmobs2.ai.combat;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class RoughAILavaRegen extends EntityAIBase {

	protected EntityLiving entity;
	
	public RoughAILavaRegen(EntityLiving entity) {
		this.entity = entity;
	}
	
	@Override
	public boolean shouldExecute() {
		return entity.isInLava();
	}
	
	@Override
	public void updateTask() {
		entity.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 60, 1, false, false));
	}
}
