package de.lellson.roughmobs2.ai.combat;

import de.lellson.roughmobs2.features.ZombieFeatures;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.MathHelper;

public class RoughAILeapAtTargetChanced extends EntityAILeapAtTarget {

	private int chance;
	protected EntityLiving leaperProtected;
	
	public RoughAILeapAtTargetChanced(EntityLiving leapingEntity, float leapMotionYIn, int chance) {
		super(leapingEntity, leapMotionYIn);
		this.leaperProtected = leapingEntity;
		this.chance = chance;
	}
	
	@Override
	public boolean shouldExecute() {
		return super.shouldExecute() && chance > 0 && leaperProtected.getRNG().nextInt(chance) == 0;
	}
}
