package de.lellson.roughmobs2.misc;

import java.util.Random;

import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.EquipHelper.EquipmentApplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;

public class BossHelper {
	
	public static final Random RND = new Random();
	public static final String BOSS = Constants.unique("isBoss");
	
	public static abstract class BossApplier {
		
		private EquipmentApplier equipApplier;
		
		private final String name;
		private final int defaultBossChance;
		private final float defaultEnchMultiplier;
		private final float defaultDropChance;
		private final String[] defaultBossNames;
		
		private int bossChance;
		private String[] bossNames;
		
		public BossApplier(String name, int defaultBossChance, float defaultEnchMultiplier, float defaultDropChance, String[] defaultBossNames) {
			this.name = name;
			this.defaultBossChance = defaultBossChance;
			this.defaultEnchMultiplier = defaultEnchMultiplier;
			this.defaultDropChance = defaultDropChance;
			this.defaultBossNames = defaultBossNames;
			
			equipApplier = new EquipmentApplier(name + " boss", 1, 1, 1, defaultEnchMultiplier, defaultDropChance);
		}

		public void initConfig() {

			equipApplier.initConfig(Constants.DEFAULT_BOSS_MAINHAND, 
									Constants.DEFAULT_BOSS_OFFHAND, 
									Constants.DEFAULT_BOSS_HELMETS, 
									Constants.DEFAULT_BOSS_CHESTPLATES, 
									Constants.DEFAULT_BOSS_LEGGINGS, 
									Constants.DEFAULT_BOSS_BOOTS, 
									Constants.DEFAULT_WEAPON_ENCHANTS, 
									Constants.DEFAULT_ARMOR_ENCHANTS, 
									true);
			
			bossChance = RoughConfig.getInteger(name, "BossChance", defaultBossChance, 0, Short.MAX_VALUE, "Chance (1 in X) for a newly spawned " + name + " to become a boss " + name);
			bossNames = RoughConfig.getStringArray(name, "BossNames", defaultBossNames, name + " boss names. Please be more creative than I am... :P");
		}

		public void postInit() {
			equipApplier.createPools();
		}
		
		public void trySetBoss(EntityLiving entity) {
			
			if (bossChance <= 0 || RND.nextInt(bossChance) != 0 || (entity instanceof EntityZombie && ((EntityZombie)entity).isChild()))
				return;
			
			AttributeHelper.applyAttributeModifier(entity, SharedMonsterAttributes.MAX_HEALTH, name + "BossHealth", 0, entity.getMaxHealth()*2);
			AttributeHelper.applyAttributeModifier(entity, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, name + "BossKnock", 1, 1);
			
			equipApplier.equipEntity(entity);
			
			entity.setCustomNameTag(bossNames[RND.nextInt(bossNames.length)]);
			entity.getEntityData().setBoolean(BOSS, true);
			
			addBossFeatures(entity);
		}
		
		public abstract void addBossFeatures(EntityLiving entity);
	}

	public static boolean isBoss(Entity entity) {
		return entity.getEntityData() != null && entity.getEntityData().getBoolean(BOSS);
	}
}
