package de.lellson.roughmobs2.features;

import java.lang.reflect.Field;

import de.lellson.roughmobs2.ai.combat.RoughAIWeaponSwitch;
import de.lellson.roughmobs2.ai.misc.RoughAISunlightBurn;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.Constants;
import de.lellson.roughmobs2.misc.FeatureHelper;
import de.lellson.roughmobs2.misc.MountHelper;
import de.lellson.roughmobs2.misc.BossHelper.BossApplier;
import de.lellson.roughmobs2.misc.EquipHelper.EquipmentApplier;
import de.lellson.roughmobs2.misc.MountHelper.HorseType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SkeletonFeatures extends EntityFeatures {

	private boolean changeWeapons;
	
	private int horseChance;
	private int horseMinY;
	
	private int bowCooldown;
	
	private boolean helmetBurn;
	
	private EquipmentApplier equipApplier;
	
	private BossApplier bossApplier;

	public SkeletonFeatures() {
		super("skeleton", EntitySkeleton.class, EntityStray.class, EntityWitherSkeleton.class);
	}
	
	@Override
	public void preInit() {
		equipApplier = new EquipmentApplier(name, 1, 4, 8, 0.5f, 0.085F);
		bossApplier = new BossApplier(name, 200, 1F, 0.2F, new String[]{"Lich King", "Skeleton Lord", "Stallord", "Skeletron", "Skeletron Prime", "Krosis", "Wolnir", "Stalmaster"}) {
			@Override
			public void addBossFeatures(EntityLiving entity) {}
		};
	}
	
	@Override
	public void postInit() {
		equipApplier.createPools();
		bossApplier.postInit();
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
	
		changeWeapons = RoughConfig.getBoolean(name, "ChangeWeapons", true, "Set this to false to prevent %ss from switching their weapons");
		
		horseChance = RoughConfig.getInteger(name, "HorseChance", 10, 0, MAX, "Chance (1 in X) that a %s spawns riding a %s horse\nSet to 0 to disable this feature");
		horseMinY = RoughConfig.getInteger(name, "HorseMinY", 63, 0, MAX, "Minimal Y position above %s horses may spawn");
		
		bowCooldown = RoughConfig.getInteger(name, "BowCooldown", 0, 0, MAX, "Bow cooldown of %ss in ticks (The vanilla default is 20)");
		
		helmetBurn = RoughConfig.getBoolean(name, "HelmetBurn", false, "Set this to true to make all %ss burn in sunlight even if they wear a helmet");
		
		equipApplier.initConfig(
			Constants.SKELETON_MAINHAND,
			Constants.DEFAULT_MAINHAND,
			Constants.DEFAULT_HELMETS,
			Constants.DEFAULT_CHESTPLATES,
			Constants.DEFAULT_LEGGINGS,
			Constants.DEFAULT_BOOTS,
			Constants.DEFAULT_WEAPON_ENCHANTS,
			Constants.DEFAULT_ARMOR_ENCHANTS,
			false
		);
		
		bossApplier.initConfig();
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (changeWeapons && entity instanceof EntityLiving)
			targetTasks.addTask(1, new RoughAIWeaponSwitch((EntityLiving) entity, 12D));
		
		if (helmetBurn)
			tasks.addTask(0, new RoughAISunlightBurn((EntityLiving) entity, true));
		
		if (entity instanceof AbstractSkeleton)
		{
			EntityAIAttackRangedBow<AbstractSkeleton> ai = ReflectionHelper.getPrivateValue(AbstractSkeleton.class, (AbstractSkeleton) entity, 1);
			if (ai != null)
				ai.setAttackCooldown(bowCooldown);
		}
	}
	
	@Override
	public void addFeatures(EntityJoinWorldEvent event, Entity entity) {
		
		if (entity instanceof EntitySkeleton && entity.getEntityWorld().provider.getDimension() == -1)
			changeToWither(event, (EntitySkeleton)entity);
		else if (entity instanceof EntityLiving)
		{
			equipApplier.equipEntity((EntityLiving) entity);
			bossApplier.trySetBoss((EntityLiving) entity);
			MountHelper.tryMountHorse(entity, HorseType.SKELETON, horseChance, horseMinY);
		}
	}

	private void changeToWither(EntityJoinWorldEvent event, EntitySkeleton entity) {
		
		EntityWitherSkeleton newSkeleton = new EntityWitherSkeleton(entity.getEntityWorld());
		newSkeleton.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
		newSkeleton.onInitialSpawn(event.getWorld().getDifficultyForLocation(entity.getPosition()), null);
		
		entity.getEntityWorld().spawnEntity(newSkeleton);
		entity.setDead();
		
		event.setCanceled(true);
	}
}
