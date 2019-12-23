package de.lellson.roughmobs2.features;

import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

public class GuardianFeatures extends EntityFeatures {

	private boolean dropWater;
	
	public GuardianFeatures() {
		super("guardian", EntityGuardian.class, EntityElderGuardian.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		dropWater = RoughConfig.getBoolean(name, "DropWater", true, "Set to false to prevent %ss from dropping water on death");
	}
	
	@Override
	public void onDeath(Entity deadEntity, DamageSource source) {

		if (dropWater) 
		{
			BlockPos pos1 = deadEntity.getPosition();
			BlockPos pos2 = deadEntity.getPosition().up();
			
			if (deadEntity.world.getBlockState(pos1).getBlock() == Blocks.AIR) 
				deadEntity.world.setBlockState(pos1, Blocks.WATER.getDefaultState(), 11);
			
			if (deadEntity.world.getBlockState(pos2).getBlock() == Blocks.AIR) 
				deadEntity.world.setBlockState(pos2, Blocks.FLOWING_WATER.getStateFromMeta(1));
		}
	}
}
