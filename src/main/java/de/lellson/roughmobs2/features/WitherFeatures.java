package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.ai.combat.RoughAISummonSkeleton;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class WitherFeatures extends EntityFeatures {
	
	private boolean pushAttackersAway;
	private int summonSkeletonTimer;
	
	public WitherFeatures() {
		super("wither", EntityWither.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		pushAttackersAway = RoughConfig.getBoolean(name, "PushAttackersAway", true, "Set to false to prevent %ss from pushing attackers away");
		summonSkeletonTimer = RoughConfig.getInteger(name, "SummonSkeletonTimer", 200, 0, MAX, "Delay in ticks between each spawned Skeleton\nSet to 0 to disable this feature");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (entity instanceof EntityLiving && summonSkeletonTimer > 0)
			tasks.addTask(1, new RoughAISummonSkeleton((EntityLiving)entity, summonSkeletonTimer, true));
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (pushAttackersAway && attacker instanceof EntityLivingBase && attacker == immediateAttacker)
		{
			FeatureHelper.knockback(target, (EntityLivingBase) attacker, 1F, 0.05F);
			attacker.attackEntityFrom(DamageSource.GENERIC, 4F);
			
			FeatureHelper.playSound(target, SoundEvents.ENTITY_WITHER_BREAK_BLOCK, 0.7f, 1.0f);
		}
	}
}
