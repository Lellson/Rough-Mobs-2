package de.lellson.roughmobs2.features;

import java.util.List;
import java.util.Map;

import de.lellson.roughmobs2.ai.combat.RoughAIMobBuff;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.Constants;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class WitchFeatures extends EntityFeatures {
	
	private float applyEffectsRange;
	private String[] applyEffectNames;
	
	protected int lingeringChance;
	
	private int batsOnDeath;
	
	private Map<Potion, Integer> effects;
	
	public WitchFeatures() {
		super("witch", EntityWitch.class);
	}
	
	public EntityFeatures addPotionHandler(List<EntityFeatures> features) {
		features.add(new WitchPotionFeatures(this));
		return this;
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		applyEffectsRange = RoughConfig.getFloat(name, "ApplyEffectsRange", 10, 0, MAX, "Range in each direction from the %ses position in which allied mobs get buffed\nSet to 0 to disable this feature");
		applyEffectNames = RoughConfig.getStringArray(name, "ApplyEffectsNames", Constants.DEFAULT_WITCH_BUFFS, "Potion effects which may be added to nearby mobs\nFormat: effect;amplifier");
		
		lingeringChance = RoughConfig.getInteger(name, "LingeringChance", 5, 0, MAX, "Chance (1 in X) for a %ses thrown potion to become a lingering potion\nSet to 0 to disable this feature");
		
		batsOnDeath = RoughConfig.getInteger(name, "BatsOnDeath", 5, 0, MAX, "Amount of bats which spawn on %ses death\nSet to 0 to disable this feature");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {

		if (entity instanceof EntityLivingBase && applyEffectsRange > 0 && !effects.isEmpty())
			tasks.addTask(1, new RoughAIMobBuff((EntityLivingBase) entity, effects, applyEffectsRange));
	}
	
	@Override
	public void onDeath(Entity deadEntity, DamageSource source) {
		
		for (int i = 0; i < batsOnDeath; i++)
		{
			EntityBat bat = new EntityBat(deadEntity.getEntityWorld());
			bat.setPosition(deadEntity.posX + Math.random() - Math.random(), deadEntity.posY + Math.random(), deadEntity.posZ + Math.random() - Math.random());
			bat.onInitialSpawn(deadEntity.getEntityWorld().getDifficultyForLocation(deadEntity.getPosition()), null);
			
			deadEntity.getEntityWorld().spawnEntity(bat);
		}
	}
	
	@Override
	public void postInit() {
		effects = FeatureHelper.getPotionsFromNames(applyEffectNames);
	}
	
	public static class WitchPotionFeatures extends EntityFeatures {
		
		private WitchFeatures features;
		
		public WitchPotionFeatures(WitchFeatures features) {
			super("potion", EntityPotion.class);
			this.features = features;
		}
		
		@Override
		public void addFeatures(EntityJoinWorldEvent event, Entity entity) {

			if (features.lingeringChance <= 0 || !(entity instanceof EntityPotion) || !(((EntityPotion)entity).getThrower() instanceof EntityWitch))
				return;
			
			if (entity.getEntityWorld().rand.nextInt(features.lingeringChance) == 0)
			{
				ItemStack potion = new ItemStack(Items.LINGERING_POTION);
				PotionUtils.addPotionToItemStack(potion, PotionUtils.getPotionFromItem(((EntityPotion)entity).getPotion()));
				((EntityPotion)entity).setItem(potion);
			}
		}
		
		@Override
		public boolean hasDefaultConfig() {
			return false;
		}
	}
}
