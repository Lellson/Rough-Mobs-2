package de.lellson.roughmobs2.ai.misc;

import java.util.ArrayList;
import java.util.List;

import de.lellson.roughmobs2.misc.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;

public class RoughAIBreakBlocks extends EntityAIBase {
	
	public static final String BANS = Constants.unique("breakbans");
	public static final String LONG_POS = Constants.unique("breakpos");
	
	protected EntityLiving breaker;
	protected int range;
	protected World world;
	protected List<Block> allowedBlocks;
	protected BlockPos target;
	protected int breakingTime;
	protected int previousBreakProgress = -1;
	protected Block block;
	protected int neededTime;
	protected int idleTime;
	protected int curDistance;

	public RoughAIBreakBlocks(EntityLiving breaker, int range, List<Block> allowedBlocks) {
		this.breaker = breaker;
		this.range = range;
		this.world = breaker.world;
		this.allowedBlocks = allowedBlocks;
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {

		if (!world.getGameRules().getBoolean("mobGriefing") || this.breaker.getAttackTarget() != null)
			return false;
		
		BlockPos target = getFirstTarget();
		if (target != null) 
		{
			this.target = target;
			return true;
		}
        
        return false;
	}
	
	@Override
	public void startExecuting() {
		
		this.breakingTime = 0;
		this.idleTime = 0;
		this.block = world.getBlockState(target).getBlock();
		this.neededTime = getBreakSpeed(); 
	}
	
	private int getBreakSpeed() {

		float multiplier = 100;
		ItemStack held = breaker.getHeldItemMainhand();
		IBlockState state = world.getBlockState(target);
		
		if (held != null && held.getItem() instanceof ItemTool) 
		{
			multiplier -= ((ItemTool)held.getItem()).getDestroySpeed(held, state)*10;
		}
		
		return (int)(state.getBlockHardness(world, target) * Math.max(0, multiplier) + 10);
	}

	@Override
	public void updateTask() {
		
		this.breaker.getNavigator().setPath(breaker.getNavigator().getPathToPos(target), 1);
		
		Entity breakerEntity = breaker;
		while (breakerEntity.isRiding()) 
		{
			breakerEntity = breakerEntity.getRidingEntity();
		}
		
		++this.idleTime;
		
		double distance = this.target.distanceSq(breakerEntity.posX, breakerEntity.posY, breakerEntity.posZ);
		
		if ((int)Math.round(distance) == curDistance)
			this.idleTime++;
		else
			this.idleTime = 0;
		
		if (this.idleTime >= 160)
			banishTarget();
		
		curDistance = (int) Math.round(distance);
		
		if (distance <= 2 && this.target != null) 
		{
	        ++this.breakingTime;
	        int i = (int)((float)this.breakingTime / (float)this.neededTime * 10.0F);

	        if (i != this.previousBreakProgress)
	        {
	            this.world.sendBlockBreakProgress(this.breaker.getEntityId(), this.target, i);
	            this.previousBreakProgress = i;
	        }

	        if (this.breakingTime >= this.neededTime)
	        {
	            this.world.setBlockToAir(this.target);
	            this.world.playEvent(2001, this.target, Block.getIdFromBlock(this.block));
	            this.breakingTime = 0;
	        }
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return target != null && (!(this.breaker instanceof EntityCreature) || ((EntityCreature)this.breaker).isWithinHomeDistanceFromPosition(target)) && world.getBlockState(target).getBlock() == this.block && this.breaker.getAttackTarget() == null;
    }
	
	@Override
	public void resetTask() {
		this.breaker.getNavigator().clearPath();
	}
	
	private BlockPos getFirstTarget() {

		List<BlockPos> positions = new ArrayList<BlockPos>();
		BlockPos curPos, target = null;
		double distance, bestDistance = Double.MAX_VALUE;
		
		Entity breakerEntity = breaker;
		while (breakerEntity.isRiding()) 
		{
			breakerEntity = breakerEntity.getRidingEntity();
		}
		
		for (int i = -range; i <= range; i++) 
		{
			for (int j = -range; j <= range; j++) 
			{
				for (int k = -range; k <= range; k++) 
				{
					curPos = new BlockPos(breakerEntity.posX + i, breakerEntity.posY + j, breakerEntity.posZ + k);
					Block block = world.getBlockState(curPos).getBlock();
					
					if (block instanceof BlockMobSpawner)
						return null;
					
					if (allowedBlocks.contains(block) && !isBanned(curPos)) 
					{
						positions.add(curPos);
					}
				}
			}
		}

		for (BlockPos pos : positions)
		{
			distance = pos.distanceSq(breakerEntity.posX, breakerEntity.posY, breakerEntity.posZ);
			if (distance < bestDistance) 
			{
				target = pos;
				bestDistance = distance;
			}
		}
		
		return target;
	}
	
	private void banishTarget() {
		
		if (breaker.getEntityData().getTag(BANS) == null)
			breaker.getEntityData().setTag(BANS, new NBTTagList());
		
		NBTTagCompound comp = new NBTTagCompound();
		comp.setLong(LONG_POS, target.toLong());
		
		((NBTTagList)breaker.getEntityData().getTag(BANS)).appendTag(comp);
		
		this.target = null;
		resetTask();
	}

	private boolean isBanned(BlockPos pos) {
		
		long l = pos.toLong();
		NBTTagList list = (NBTTagList) breaker.getEntityData().getTag(BANS);
		
		if (list == null)
			return false;
		
		for (int i = 0; i < list.tagCount(); i++)
		{
			if (list.getCompoundTagAt(i).getLong(LONG_POS) == l)
				return true;
		}

		return false;
	}
}
