package de.lellson.roughmobs2.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.lellson.roughmobs2.RoughMobs;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class FeatureHelper {
	
	public static boolean removeTask(EntityCreature entity, Class<? extends EntityAIBase> aiClass) {
		return tryRemoveTask(entity, entity.tasks, aiClass) || tryRemoveTask(entity, entity.targetTasks, aiClass);
	}
	
	private static boolean tryRemoveTask(EntityCreature entity, EntityAITasks tasks, Class<? extends EntityAIBase> aiClass) {
		
		for (EntityAITaskEntry ai : tasks.taskEntries) 
		{
			if (aiClass.isInstance(ai.action)) 
			{
				tasks.removeTask(ai.action);
				return true;
			}
		}
		
		return false;
	}

	public static boolean addEffect(EntityLivingBase entity, Potion effect, int duration, int startAmplifier, int chance, boolean isIncreasing, int maxAmplifier) {
		
		if (entity == null || effect == null || startAmplifier < 0 || chance <= 0 || duration <= 0 || entity.getRNG().nextInt(chance) != 0)
			return false;
		
		int amplifier = startAmplifier;
		if (isIncreasing) 
		{
			PotionEffect active = entity.getActivePotionEffect(effect);
			if (active != null) 
			{
				if (active.getDuration() == duration)
					return false;
				
				amplifier = Math.min(startAmplifier + active.getAmplifier() + 1, maxAmplifier);
			}
		}
		
		entity.addPotionEffect(new PotionEffect(effect, duration, amplifier));
		return true;
	}
	
	public static boolean addEffect(EntityLivingBase entity, Potion effect, int duration, int amplifier, int chance) {
		return addEffect(entity, effect, duration, amplifier, chance, false, 127);
	}
	
	public static boolean addEffect(EntityLivingBase entity, Potion effect, int duration, int amplifier) {
		return addEffect(entity, effect, duration, amplifier, 1);
	}
	
	public static boolean addEffect(EntityLivingBase entity, Potion effect, int duration) {
		return addEffect(entity, effect, duration, 0);
	}
	
	public static void spawnParticle(Entity entity, EnumParticleTypes type, float spread, int amount) {
		
		Random rnd = entity.world.rand; 
		for (int i = 0; i < amount; i++) 
		{
			double moveX = (rnd.nextDouble() - 0.5D) * 2.0D * spread;
			double moveY = -rnd.nextDouble() * spread;
			double moveZ = (rnd.nextDouble() - 0.5D) * 2.0D * spread;
			((WorldServer)entity.world).spawnParticle(type, entity.posX + (rnd.nextDouble() - 0.5D) * (double)entity.width, entity.posY + rnd.nextDouble() * (double)entity.height - 0.25D, entity.posZ + (rnd.nextDouble() - 0.5D) * (double)entity.width, moveX, moveY, moveZ, new int[0]);
		}
	}
	
	public static void spawnParticle(Entity entity, EnumParticleTypes type, int amount) {
		spawnParticle(entity, type, 1, amount);
	}
	
	public static void spawnParticle(Entity entity, EnumParticleTypes type) {
		spawnParticle(entity, type, 1);
	}
	
	public static void playSound(Entity entity, SoundEvent event, float volume, float pitch) {
		
		entity.world.playSound((EntityPlayer)null, entity.prevPosX, entity.prevPosY, entity.prevPosZ, event, entity.getSoundCategory(), volume, pitch);
    	entity.playSound(event, volume, pitch);
	}
	
	public static void playSound(Entity entity, SoundEvent event) {
		playSound(entity, event, 1.0F, 1.0F);
	}
	
	public static void knockback(Entity attacker, EntityLivingBase target, float strength, float extraLeap) {
		
		double xRatio = attacker.posX - target.posX;
		double zRatio = attacker.posZ - target.posZ;
		target.knockBack(attacker, strength, xRatio, zRatio);
		target.motionY += extraLeap;
	}

	public static List<Block> getBlocksFromNames(String[] breakBlocks) {

		List<Block> blocks = new ArrayList<Block>();
		
		for (String name : breakBlocks) 
		{
			Block block = Block.getBlockFromName(name);
			if (block == null) 
				RoughMobs.logError(name + " isn't a valid block!");
			else
				blocks.add(block);
		}
		
		return blocks;
	}
	
	public static List<EntityEntry> getEntitiesFromNames(String[] entitieNames) {

		List<EntityEntry> entities = new ArrayList<EntityEntry>();
		
		for (String name : entitieNames) 
		{
			EntityEntry entity = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name));
			if (entity == null) 
				RoughMobs.logError(name + " isn't a valid entity!");
			else
				entities.add(entity);
		}
		
		return entities;
	}
	
	public static Map<Potion, Integer> getPotionsFromNames(String[] potionNames) {

		Map<Potion, Integer> potions = new HashMap<Potion, Integer>();
		
		for (String name : potionNames) 
		{
			String[] parts = name.split(";");
			
			if (parts.length < 2)
				continue;
			
			Potion potion = Potion.getPotionFromResourceLocation(parts[0]);
			if (potion == null) 
				RoughMobs.logError(parts[0] + " isn't a valid potion effect!");
			else
			{
				try
				{
					potions.put(potion, Integer.parseInt(parts[1]));
				}
				catch(NumberFormatException e)
				{
					RoughMobs.logError(parts[1] + " isn't a valid number!");
				}
			}
		}
		
		return potions;
	}
}
