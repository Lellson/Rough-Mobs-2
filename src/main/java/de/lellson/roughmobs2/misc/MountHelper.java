package de.lellson.roughmobs2.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.lellson.roughmobs2.ai.misc.RoughAISearchForRider;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.features.HostileHorseFeatures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class MountHelper {
	
	public static final Random RND = new Random();
	
	public static class Rider {
		
		public static final String RIDER = Constants.unique("isrider");
		
		private final String name;
		private final String[] defaultEntities;
		private final int defaultChance;
		
		private String[] entities;
		private int chance;
		private int randomRiderChance;
		
		private List<EntityEntry> entries;
		
		public Rider(String name, String[] defaultEntities, int defaultChance) {
			this.name = name;
			this.defaultEntities = defaultEntities;
			this.defaultChance = defaultChance;
		}
		
		public void initConfigs() {
			
			chance = RoughConfig.getInteger(name, "RiderChance", defaultChance, 0, Short.MAX_VALUE, "Chance (1 in X) for a " + name + " to spawn with another entity riding it\nSet to 0 to disable this feature");
			entities = RoughConfig.getStringArray(name, "RiderEntities", defaultEntities, "Entities which may ride on " + name + "s");
			randomRiderChance = RoughConfig.getInteger(name, "RiderChanceRandom", 10, 0, Short.MAX_VALUE, "Chance (1 in X) that a randomly spawned entity from the RiderEntities list can start riding on random " + name + "s\nSet to 0 to disable this feature");
		}
		
		public void postInit() {
			entries = FeatureHelper.getEntitiesFromNames(entities);
		}
		
		public void addAI(EntityLiving mount) {
			if (randomRiderChance > 0)
				mount.tasks.addTask(1, new RoughAISearchForRider(mount, getPossibleRiders(), 32, randomRiderChance));
		}
		
		public void tryAddRider(EntityLivingBase mount) {
			
			if (chance <= 0 || mount == null || entries.isEmpty() || mount.getEntityData().getBoolean(RIDER) || RND.nextInt(chance) != 0)
				return;
			
			EntityEntry entry = entries.get(RND.nextInt(entries.size()));
			
			Entity entity = entry.newInstance(mount.getEntityWorld());
			entity.setPosition(mount.posX, mount.posY, mount.posZ);
			entity.hurtResistantTime = 60;
			entity.getEntityData().setBoolean(RIDER, true);
			
			mount.getEntityWorld().spawnEntity(entity);
			if (!entity.isRiding() && !entity.isBeingRidden() && !mount.isRiding() && !mount.isBeingRidden())
				entity.startRiding(mount);
		}
		
		public boolean isPossibleRider(Entity entity) {
			
			for (EntityEntry entry : entries)
				if (entry.getEntityClass() == entity.getClass())
					return true;
			
			return false;
		}
		
		public List<Class<? extends Entity>> getPossibleRiders() {
			
			List<Class<? extends Entity>> list = new ArrayList<Class<? extends Entity>>();
			
			for (EntityEntry entry : entries)
				list.add(entry.getEntityClass());
			
			return list;
		}
	}
	
	public enum HorseType {
		NORMAL, ZOMBIE, SKELETON;
		
		public AbstractHorse createInstance(World world) {
			
			switch(this) 
			{
				case ZOMBIE: return new EntityZombieHorse(world);
				case SKELETON: return new EntitySkeletonHorse(world);
				default: return new EntityHorse(world);
			}
		}
	}
	
	public static AbstractHorse createHorse(World world, Entity owner, HorseType type) {
		
		AbstractHorse horse = type.createInstance(world);
		horse.setPosition(owner.posX, owner.posY, owner.posZ);
		horse.onInitialSpawn(world.getDifficultyForLocation(owner.getPosition()), (IEntityLivingData)null);
		horse.hurtResistantTime = 60;
        horse.setHorseTamed(true);
        horse.setGrowingAge(0);
        horse.getEntityData().setBoolean(HostileHorseFeatures.ROUGH_HORSE, true);
		
		world.spawnEntity(horse);
		return horse;
	}
	
	public static boolean tryMountHorse(Entity rider, HorseType type, int chance, int minY) {
		
		if (rider.posY < minY)
			return false;
		
		if (!BossHelper.isBoss(rider) && (chance <= 0 || RND.nextInt(chance) != 0 || rider.isRiding() || (rider instanceof EntityZombie && ((EntityZombie)rider).isChild())))
			return false;
		
		if (rider.getEntityData().getBoolean(Rider.RIDER))
			return false;
		
		AbstractHorse mount = createHorse(rider.world, rider, type);
		rider.startRiding(mount);
		return true;
	}
}
