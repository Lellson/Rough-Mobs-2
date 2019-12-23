package de.lellson.roughmobs2.ai.misc;

import java.util.List;

import de.lellson.roughmobs2.misc.Constants;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

public class RoughAISearchForRider extends EntityAIBase {

	public static final String MOUNT_SEARCHER = Constants.unique("mountSearcher");
	public static final int IS_SEARCHER = 2;
	public static final int NO_SEARCHER = 1;
	
	protected EntityLiving entity;
	protected List<Class<? extends Entity>> possibleRiders;
	protected int range;
	protected int chance;
	
	protected EntityLiving mountSearcher;
	
	public RoughAISearchForRider(EntityLiving entity, List<Class<? extends Entity>> possibleRiders, int range, int chance) {
		this.entity = entity;
		this.possibleRiders = possibleRiders;
		this.range = range;
		this.chance = chance;
	}

	@Override
	public boolean shouldExecute() {
		
		if (entity.isDead || entity.isBeingRidden())
		{
			mountSearcher = null;
			return false;
		}

		List<EntityLiving> entities = entity.world.getEntitiesWithinAABB(EntityLiving.class, entity.getEntityBoundingBox().grow(range));
		
		for (EntityLiving entity : entities)
			if (!entity.isDead && this.entity != entity && isPossibleRider(entity))
			{
				if (entity.getEntityData().getInteger(MOUNT_SEARCHER) == 0)
					entity.getEntityData().setInteger(MOUNT_SEARCHER, entity.world.rand.nextInt(chance) == 0 || entity.isRiding() ? IS_SEARCHER : NO_SEARCHER);
				
				if (entity.getEntityData().getInteger(MOUNT_SEARCHER) == IS_SEARCHER && !entity.isRiding())
				{
					mountSearcher = entity;
					return true;
				}
			}
		
		mountSearcher = null;
		return false;
	}
	
	@Override
	public void updateTask() {
		
		this.mountSearcher.getNavigator().setPath(mountSearcher.getNavigator().getPathToEntityLiving(entity), 1);
		
		if (this.entity.getDistanceSq(this.mountSearcher) <= 2 && this.mountSearcher != this.entity)
		{
			if (this.entity instanceof AbstractHorse)
			{
				AbstractHorse horse = (AbstractHorse) this.entity;
				horse.hurtResistantTime = 60;
		        horse.enablePersistence();
		        horse.setHorseTamed(true);
			}
			
			this.mountSearcher.startRiding(this.entity);
			this.mountSearcher.getNavigator().clearPath();
		}
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return (!(this.mountSearcher instanceof EntityCreature) || ((EntityCreature)this.mountSearcher).isWithinHomeDistanceFromPosition(entity.getPosition())) && this.mountSearcher.getAttackTarget() == null && !this.entity.isDead && !this.entity.isBeingRidden() && !this.mountSearcher.isRiding() && this.mountSearcher != this.entity;
    }
	
	@Override
	public void resetTask() {
		this.mountSearcher.getNavigator().clearPath();
		this.mountSearcher = null;
	}
	
	private boolean isPossibleRider(EntityLivingBase entity) {
		
		for (Class<? extends Entity> clazz : possibleRiders)
			if (clazz.isInstance(entity))
				return true;
		
		return false;
	}
}
