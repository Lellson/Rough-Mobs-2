package de.lellson.roughmobs2.features;

import java.util.Arrays;
import java.util.List;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.ai.combat.RoughAILeapAtTargetChanced;
import de.lellson.roughmobs2.ai.misc.RoughAIBreakBlocks;
import de.lellson.roughmobs2.ai.misc.RoughAISunlightBurn;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.Constants;
import de.lellson.roughmobs2.misc.EquipHelper;
import de.lellson.roughmobs2.misc.EquipHelper.EquipmentApplier;
import de.lellson.roughmobs2.misc.EquipHelper.EquipmentPool;
import de.lellson.roughmobs2.misc.FeatureHelper;
import de.lellson.roughmobs2.misc.MountHelper;
import de.lellson.roughmobs2.misc.BossHelper.BossApplier;
import de.lellson.roughmobs2.misc.MountHelper.HorseType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLLog;

public class ZombieFeatures extends EntityFeatures {
	
	public static final String BOSS_MINION = Constants.unique("bossMinion");
	
	private float leapHeight; 
	private int leapChance;
	
	private int hungerDuration;
	private int hungerChance;
	
	private int horseChance;
	private int horseMinY;
	
	private boolean babyBurn;
	private boolean helmetBurn;
	
	private EquipmentApplier equipApplier;
	
	private BossApplier bossApplier;

	private String[] breakBlocks;
	private List<Block> allowedBreakBlocks;
	
	public ZombieFeatures() {
		super("zombie", EntityZombie.class, EntityZombieVillager.class, EntityHusk.class, EntityPigZombie.class);
	}
	
	@Override
	public void preInit() {
		equipApplier = new EquipmentApplier(name, 3, 4, 8, 0.5f, 0.085F);
		bossApplier = new BossApplier(name, 200, 1F, 0.2F, new String[]{"Zombie King", "Flesh King", "Dr. Zomboss", "Azog", "Zon-Goku", "Amy", "Z0mb3y"}) {
			@Override
			public void addBossFeatures(EntityLiving entity) {
				
				for (int i = 0; i < 4; i++) 
				{
					EntityZombie zombieMinion = new EntityZombie(entity.getEntityWorld());
					zombieMinion.setPosition(entity.posX, entity.posY, entity.posZ);
					zombieMinion.onInitialSpawn(entity.getEntityWorld().getDifficultyForLocation(entity.getPosition()), null);
					zombieMinion.setChild(true);
					zombieMinion.getEntityData().setBoolean(BOSS_MINION, true);
					
					entity.world.spawnEntity(zombieMinion);
				}
			}
		};
	}
	
	@Override
	public void postInit() {
		equipApplier.createPools();
		bossApplier.postInit();
		allowedBreakBlocks = FeatureHelper.getBlocksFromNames(breakBlocks);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		leapChance = RoughConfig.getInteger(name, "LeapChance", 5, 0, MAX, "Chance (1 in X) for a %s to leap to the target\nSet to 0 to disable this feature");
		leapHeight = RoughConfig.getFloat(name, "LeapHeight", 0.2F, 0, MAX, "Amount of blocks the %s jumps on leap attack");
		
		hungerDuration = RoughConfig.getInteger(name, "HungerDuration", 200, 1, MAX, "Duration in ticks of the applied hunger effect (20 ticks = 1 second)");
		hungerChance = RoughConfig.getInteger(name, "HungerChance", 1, 0, MAX, "Chance (1 in X) for a %s to apply the hunger effect on attack\nSet to 0 to disable this feature");
		
		horseChance = RoughConfig.getInteger(name, "HorseChance", 10, 0, MAX, "Chance (1 in X) that a %s spawns riding a %s horse\nSet to 0 to disable this feature");
		horseMinY = RoughConfig.getInteger(name, "HorseMinY", 63, 0, MAX, "Minimal Y position above %s horses may spawn");
		
		babyBurn = RoughConfig.getBoolean(name, "BabyBurn", true, "Set this to false to prevent baby %ss from burning in sunlight");
		helmetBurn = RoughConfig.getBoolean(name, "HelmetBurn", false, "Set this to true to make all %ss burn in sunlight even if they wear a helmet");
		
		breakBlocks = RoughConfig.getStringArray(name, "BreakBlocks", Constants.DEFAULT_DESTROY_BLOCKS, "Blocks which can be destroyed by %ss if they have no attack target\nDelete all lines to disable this feature");
		
		equipApplier.initConfig(
			Constants.DEFAULT_MAINHAND,
			Constants.DEFAULT_OFFHAND,
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
		
		if (!(entity instanceof EntityLiving))
			return;
		
		if (leapChance > 0)
			tasks.addTask(1, new RoughAILeapAtTargetChanced((EntityLiving) entity, leapHeight, leapChance));
		
		if (babyBurn && entity instanceof EntityZombie && ((EntityZombie)entity).isChild() && !entity.isImmuneToFire())
			tasks.addTask(0, new RoughAISunlightBurn((EntityLiving) entity, false));
		
		if (helmetBurn)
			tasks.addTask(0, new RoughAISunlightBurn((EntityLiving) entity, true));
		
		if (allowedBreakBlocks.size() > 0)
			tasks.addTask(1, new RoughAIBreakBlocks((EntityLiving) entity, 8, allowedBreakBlocks));
	}
	
	@Override
	public void addFeatures(EntityJoinWorldEvent event, Entity entity) {
		
		if (!(entity instanceof EntityLiving) || entity.getEntityData().getBoolean(BOSS_MINION))
			return;
		
		equipApplier.equipEntity((EntityLiving) entity);
		bossApplier.trySetBoss((EntityLiving) entity);
		MountHelper.tryMountHorse(entity, HorseType.ZOMBIE, horseChance, horseMinY);
	}
	
	@Override
	public void onAttack(Entity attacker, Entity immediateAttacker, Entity target, LivingAttackEvent event) {
		
		if (target instanceof EntityLivingBase && hungerChance > 0)
			FeatureHelper.addEffect((EntityLivingBase)target, MobEffects.HUNGER, hungerDuration, 0, hungerChance, true, 4);
	}
}
