package de.lellson.roughmobs2.ai.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class RoughAISunlightBurn extends EntityAIBase {

	protected EntityLiving entity;
	protected boolean helmetMode;
	
	public RoughAISunlightBurn(EntityLiving entity, boolean helmetMode) {
		this.entity = entity;
		this.helmetMode = helmetMode;
		this.setMutexBits(4);
	}
	
	@Override
	public boolean shouldExecute() {

        float f = entity.getBrightness();
        boolean flag = entity.world.isDaytime() && f > 0.5F && entity.world.canSeeSky(new BlockPos(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ));
        
        if (!flag)
        	return false;
		
        ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (itemstack != null && !itemstack.isEmpty()) 
        {
        	if (helmetMode)
        		return !entity.isBurning();
        	
            if (itemstack.isItemStackDamageable()) 
            {
                itemstack.setItemDamage(itemstack.getItemDamage() + entity.world.rand.nextInt(2));

                if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) 
                {
                	entity.renderBrokenItemStack(itemstack);
                	entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
            return false;
        }
        
		return !helmetMode && !entity.isBurning();
	}
	
	@Override
	public void updateTask() {
		entity.setFire(8);
	}
}
