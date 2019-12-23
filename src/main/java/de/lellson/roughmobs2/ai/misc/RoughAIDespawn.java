package de.lellson.roughmobs2.ai.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RoughAIDespawn extends EntityAIBase {

	protected EntityLivingBase entity;
	protected Entity player = null;
	
	public RoughAIDespawn(EntityLivingBase entity) {
		this.entity = entity;
	}
	
	@Override
	public boolean shouldExecute() {
		
		EntityPlayer playerNew = entity.world.getClosestPlayerToEntity(entity, -1.0D);
		if (player == null || playerNew != null)
			player = playerNew;
		
		return player != null;
	}
	
	@Override
	public void updateTask() {
		
	    if (player != null)
	    {
	        double d0 = player.posX - entity.posX;
	        double d1 = player.posY - entity.posY;
	        double d2 = player.posZ - entity.posZ;
	        double d3 = d0 * d0 + d1 * d1 + d2 * d2;

	        if (d3 > 16384.0D)
	        {
	        	entity.setDead();
	        }

	        if (entity.getIdleTime() > 600 && entity.world.rand.nextInt(800) == 0 && d3 > 1024.0D)
	        {
	        	entity.setDead();
	        }
	        else if (d3 < 1024.0D)
	        {
	        	ReflectionHelper.setPrivateValue(EntityLivingBase.class, entity, 0, "idleTime", "field_70708_bq");
	        }
	    }
	}
}
