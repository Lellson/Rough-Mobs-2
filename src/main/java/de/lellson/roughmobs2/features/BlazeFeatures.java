package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.ai.combat.RoughAIAggressiveTouch;
import de.lellson.roughmobs2.ai.combat.RoughAIFlameTouch;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class BlazeFeatures extends EntityFeatures {

	private boolean pushAttackersAway;
	private boolean flameTouch;
	private float deathExplosionStrength;
	
	public BlazeFeatures() {
		super("blaze", EntityBlaze.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		pushAttackersAway = RoughConfig.getBoolean(name, "PushAttackersAway", true, "Set to false to prevent %ss from pushing attackers away");
		flameTouch = RoughConfig.getBoolean(name, "FlameTouch", true, "Set to false to prevent %ss from igniting entities which touch their hitbox");
		deathExplosionStrength = RoughConfig.getFloat(name, "DeathExplosionStrength", 1.0F, 0F, MAX, "Explosion strength of the explosions, which %ss create on death\nSet to 0 to disable this feature");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (entity instanceof EntityLiving && flameTouch)
			tasks.addTask(1, new RoughAIFlameTouch((EntityLiving) entity));
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (pushAttackersAway && attacker instanceof EntityLivingBase && attacker == immediateAttacker)
		{
			FeatureHelper.knockback(target, (EntityLivingBase) attacker, 1F, 0.05F);
			attacker.attackEntityFrom(DamageSource.GENERIC, 4F);
			attacker.setFire(8);
			
			FeatureHelper.playSound(target, SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.0f);
		}
	}
	
	@Override
	public void onDeath(Entity deadEntity, DamageSource source) {
		
		if (deathExplosionStrength > 0 && !(source.getTrueSource() instanceof FakePlayer))
		{
			deadEntity.world.createExplosion(deadEntity, deadEntity.posX, deadEntity.posY, deadEntity.posZ, deathExplosionStrength, true);
		}
	}
}
