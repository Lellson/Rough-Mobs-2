package de.lellson.roughmobs2.features;

import java.lang.reflect.Method;

import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.FeatureHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class SlimeFeatures extends EntityFeatures {

	private int splitChance;
	private float knockBackMultiplier;
	
	public SlimeFeatures() {
		super("slime", EntitySlime.class, EntityMagmaCube.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		splitChance = RoughConfig.getInteger(name, "SplitChance", 6, 0, MAX, "Chance (1 in X minus %s size) that a bigger %s summons a baby %s when hit\nSet to 0 to disable this feature");
		knockBackMultiplier = RoughConfig.getFloat(name, "KnockBackMultiplier", 0.2F, 0F, MAX, "Amount of extra knockback a %s deals\nCalculated with this value times the slime size\nSet to 0 to disable this feature");
	}
	
	@Override
	public void onAttack(Entity attacker, Entity immediateAttacker, Entity target, LivingAttackEvent event) {
		
		if (knockBackMultiplier > 0 && target instanceof EntityLivingBase) 
		{
			FeatureHelper.knockback(attacker, (EntityLivingBase)target, Math.max(getSlimeSize(attacker), 1)*knockBackMultiplier, 0.3F);
		}
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (splitChance <= 0)
			return;
		
		int chance = Math.max(1, splitChance - getSlimeSize(target));
		
		if (!isSmallSlime(target) && attacker == immediateAttacker && RND.nextInt(chance) == 0)
		{
			EntitySlime slime = (EntitySlime) EntityRegistry.getEntry(target.getClass()).newInstance(target.getEntityWorld());
			slime.setPosition(target.posX + Math.random() - Math.random(), target.posY + Math.random(), target.posZ + Math.random() - Math.random());
			slime.onInitialSpawn(target.getEntityWorld().getDifficultyForLocation(target.getPosition()), null);
			setSlimeSize(slime, 1);
			target.world.spawnEntity(slime);
		}
	}
	
	private boolean isSmallSlime(Entity entity) {
		return entity instanceof EntitySlime && ((EntitySlime)entity).isSmallSlime();
	}
	
	private int getSlimeSize(Entity entity) {
		return entity instanceof EntitySlime ? ((EntitySlime)entity).getSlimeSize() : 2;
	}
	
	private void setSlimeSize(EntitySlime slime, int size) {
		
		try 
		{
			Method setSlimeSize = EntitySlime.class.getDeclaredMethod("setSlimeSize", int.class, boolean.class);
			setSlimeSize.setAccessible(true);		
			setSlimeSize.invoke(slime, size, true);
		} 
		catch (Exception e) 
		{
			try 
			{
				Method setSlimeSize = EntitySlime.class.getDeclaredMethod("func_70799_a", int.class, boolean.class);
				setSlimeSize.setAccessible(true);		
				setSlimeSize.invoke(slime, size, true);
			} 
			catch (Exception e2) 
			{
				e2.printStackTrace();
			} 
		} 
	}
}
