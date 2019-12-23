package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.ai.combat.RoughAILavaRegen;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public class MagmaCubeFeatures extends EntityFeatures {
	
	private boolean regenInLava;
	private boolean dropLava;
	
	public MagmaCubeFeatures() {
		super("magma cube", EntityMagmaCube.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		regenInLava = RoughConfig.getBoolean(name, "RegenInLava", true, "Set to false to prevent %ss from regenerating health while in lava");
		dropLava = RoughConfig.getBoolean(name, "DropLava", true, "Set to false to prevent small %ss from dropping lava on death");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (regenInLava && entity instanceof EntityLiving)
			tasks.addTask(1, new RoughAILavaRegen((EntityLiving)entity));
	}
	
	@Override
	public void onDeath(Entity deadEntity, DamageSource source) {
		
		if (dropLava && deadEntity instanceof EntitySlime && ((EntitySlime)deadEntity).getSlimeSize() <= 1)
		{
			BlockPos pos1 = deadEntity.getPosition();
			BlockPos pos2 = deadEntity.getPosition().up();
			
			if (deadEntity.world.getBlockState(pos1).getBlock() == Blocks.AIR) 
				deadEntity.world.setBlockState(pos1, Blocks.FLOWING_LAVA.getStateFromMeta(11));
			
			if (deadEntity.world.getBlockState(pos2).getBlock() == Blocks.AIR) 
				deadEntity.world.setBlockState(pos2, Blocks.FLOWING_LAVA.getStateFromMeta(1));
		}
	}
}
