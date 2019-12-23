package de.lellson.roughmobs2.features;

import java.lang.reflect.InvocationTargetException;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class SilverfishFeatures extends EntityFeatures {
	
	private int splitChance;
	
	public SilverfishFeatures() {
		super("silverfish", EntitySilverfish.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		splitChance = RoughConfig.getInteger(name, "SplitChance", 3, 0, MAX, "Chance (1 in X) to split a %s in two when attacked\nSet to 0 to disable this feature");
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (target instanceof EntityLiving)
		{
			float health = ((EntityLiving)target).getHealth();
			
			if (splitChance > 0 && health > 0 && target.getEntityWorld().rand.nextInt(splitChance) == 0)
			{
				try 
				{
					EntityLiving silverfish = (EntityLiving) target.getClass().getConstructor(World.class).newInstance(target.getEntityWorld());
					silverfish.setPosition(target.posX, target.posY, target.posZ);
					silverfish.onInitialSpawn(target.getEntityWorld().getDifficultyForLocation(target.getPosition()), null);
					silverfish.setHealth(health);
					
					target.world.spawnEntity(silverfish);
				} 
				catch (Exception e) 
				{
					RoughMobs.logError("Couldn't create an entity from default constructor: " + target.getName());
				}
			}
		}
	}
}
