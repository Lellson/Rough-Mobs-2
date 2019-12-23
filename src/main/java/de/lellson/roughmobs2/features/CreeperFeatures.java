package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.ai.combat.RoughAIInvisibleTarget;
import de.lellson.roughmobs2.ai.misc.RoughAIBurnExplosion;
import de.lellson.roughmobs2.ai.misc.RoughAISunlightBurn;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CreeperFeatures extends EntityFeatures {
	
	private float invisibleRange;
	
	private int fuseTime;
	private int explosionRadius;
	
	private boolean creeperBurn;
	private boolean creeperBurnExplosion;
	
	public CreeperFeatures() {
		super("creeper", EntityCreeper.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		invisibleRange = RoughConfig.getFloat(name, "InvisibleRange", 6F, 0F, MAX, "Block range to the target in which %ss become invisible\nSet to 0 to prevent %ss from becoming invisible");
	
		fuseTime = RoughConfig.getInteger(name, "FuseTime", 20, 0, MAX, "Creeper fuse time (In ticks, 20 ticks = 1 second)\nThe vanilla default is 30");
		explosionRadius = RoughConfig.getInteger(name, "ExplosionRadius", 4, 0, MAX, "Creeper explosion radius\nThe vanilla default is 3");
		
		creeperBurn = RoughConfig.getBoolean(name, "CreeperBurn", false, "If true %ss burn in sunlight");
		creeperBurnExplosion = RoughConfig.getBoolean(name, "CreeperBurnExplosion", false, "If true %ss explode if they catch fire");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (!(entity instanceof EntityLiving))
			return;
		
		if (invisibleRange > 0)
			tasks.addTask(2, new RoughAIInvisibleTarget((EntityLiving) entity, invisibleRange));
		
		if (entity instanceof EntityCreeper) 
		{
			if (fuseTime != 30)
				ReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper)entity, fuseTime, 5);
			
			if (explosionRadius != 3)
				ReflectionHelper.setPrivateValue(EntityCreeper.class, (EntityCreeper)entity, explosionRadius, 6);
			
			if (creeperBurnExplosion)
				tasks.addTask(0, new RoughAIBurnExplosion((EntityCreeper)entity));
		}
		
		if (creeperBurn)
			tasks.addTask(0, new RoughAISunlightBurn((EntityLiving) entity, false));
	}
}
