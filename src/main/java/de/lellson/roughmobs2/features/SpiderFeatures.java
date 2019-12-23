package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.Constants;
import de.lellson.roughmobs2.misc.FeatureHelper;
import de.lellson.roughmobs2.misc.MountHelper.Rider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class SpiderFeatures extends EntityFeatures {

	private float ignoreFallDamageMult;
	
	private int slownessChance;
	private int slownessDuration;
	private boolean slownessCreateWeb;
	
	private Rider rider;
	
	public SpiderFeatures() {
		super("spider", EntitySpider.class, EntityCaveSpider.class);
	}
	
	@Override
	public void preInit() {
		rider = new Rider(name, Constants.DEFAULT_SPIDER_RIDERS, 10);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		ignoreFallDamageMult = RoughConfig.getFloat(name, "IgnoreFallDamageMult", 0.0f, 0.0f, 1.0f, "The fall damage %ss take is multiplied by this value (0.0 means no fall damage, 1.0 means normal full damage)");
		
		slownessDuration = RoughConfig.getInteger(name, "SlownessDuration", 200, 1, MAX, "Duration in ticks of the applied slowness effect (20 ticks = 1 second)");
		slownessChance = RoughConfig.getInteger(name, "SlownessChance", 1, 0, MAX, "Chance (1 in X) for a %s to apply the slowness effect on attack\nSet to 0 to disable this feature");
		slownessCreateWeb = RoughConfig.getBoolean(name, "SlownessCreateWeb", true, "Set this to false to prevent %ss from creating webs on slowed targets");
	
		rider.initConfigs();
	}
	
	@Override
	public void postInit() {
		rider.postInit();
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		if (entity instanceof EntityLiving)
			rider.addAI((EntityLiving) entity);
	}
	
	@Override
	public void addFeatures(EntityJoinWorldEvent event, Entity entity) {
		if (entity instanceof EntityLivingBase)
			rider.tryAddRider((EntityLivingBase) entity);
	}
	
	@Override
	public void onFall(Entity entity, LivingFallEvent event) {
		
		if (ignoreFallDamageMult == 1)
			return;
		
		if (ignoreFallDamageMult == 0)
			event.setCanceled(true);
		
		event.setDamageMultiplier(event.getDamageMultiplier() * ignoreFallDamageMult);
	}
	
	@Override
	public void onAttack(Entity attacker, Entity immediateAttacker, Entity target, LivingAttackEvent event) {
		
		if (target instanceof EntityLivingBase && slownessChance > 0)
		{
			EntityLivingBase living = (EntityLivingBase)target;
			int maxAmp = 4;
			
			FeatureHelper.addEffect(living, MobEffects.SLOWNESS, slownessDuration, 0, slownessChance, true, maxAmp);
			PotionEffect active = living.getActivePotionEffect(MobEffects.SLOWNESS);
			
			if (slownessCreateWeb && active != null && active.getAmplifier() >= maxAmp && RND.nextInt(slownessChance) == 0 && target.getEntityWorld().getBlockState(target.getPosition()).getBlock() == Blocks.AIR)
			{
				target.getEntityWorld().setBlockState(target.getPosition(), Blocks.WEB.getDefaultState());
			}
		}
	}
}	
