package de.lellson.roughmobs2.ai.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class RoughAIWeaponSwitch extends EntityAIBase{

	protected EntityLiving entity;
	protected double range;
	protected ItemStack defaultMainhand;
	protected ItemStack defaultOffhand;
	protected ItemStack currentMainhand;
	protected ItemStack currentOffhand;
	
	public RoughAIWeaponSwitch(EntityLiving entity, double range) {
		this.entity = entity;
		this.range = range;
		this.defaultMainhand = entity.getHeldItemMainhand();
		this.defaultOffhand = entity.getHeldItemOffhand();
		setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {
		
		if (defaultMainhand == ItemStack.EMPTY || defaultMainhand == null || defaultOffhand == ItemStack.EMPTY || defaultOffhand == null)
			return false;
		
		EntityLivingBase target = entity.getAttackTarget();
		return target != null && target.getDistanceSq(entity) < range;
	}
	
	@Override
	public void startExecuting() {
		
		this.currentMainhand = entity.getHeldItemMainhand().copy();
		this.currentOffhand = entity.getHeldItemOffhand().copy();
		
		entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, this.currentOffhand);
		entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, this.currentMainhand);
	}
	
	@Override
	public void resetTask() {
		entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, this.currentMainhand);
		entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, this.currentOffhand);
	}
}
