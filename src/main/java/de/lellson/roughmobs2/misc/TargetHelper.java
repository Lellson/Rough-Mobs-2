package de.lellson.roughmobs2.misc;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class TargetHelper {
	
	public static final String CATEGORY = "targetBlocker";
	private static boolean enableTargetBlock;
	private static final List<TargetEntry> LIST = new ArrayList<TargetEntry>();
	
	public static void init() {
		
		RoughConfig.getConfig().addCustomCategoryComment(CATEGORY, "Entities which can't be targeted by other entities."
																+ "\ne.g. Skeletons can't target other Skeletons by shooting them accidentally"
																+ "\nTakes 2 arguments divided by a semicolon per entry. victim;attacker"
																+ "\nvictim: The entity which should not be targeted if attacked by the attacker (entity name)"
																+ "\nattacker: the attacker entity which can't target the victim (entity name)"
																+ "\nUse \"*\" instead of the victim or attacker if you want this for all entities except players");
		String[] options = RoughConfig.getStringArray(CATEGORY, "List", Constants.DEFAULT_TARGET_BLOCKER, "");
		enableTargetBlock = RoughConfig.getBoolean(CATEGORY, "Enabled", false, "Set to true to enable the target blocker feature");
		
		fillList(options);
	}
	
	private static void fillList(String[] options) {
		
		for (String option : options) 
		{
			String[] split = option.split(";");
			if (split.length >= 2) 
			{
				Class[] entities = new Class[2];
				boolean success = true;
				for (int i = 0; i < 2; i++)
				{
					if (split[i].trim().equals("*"))
					{
						entities[i] = Entity.class;
					}
					else
					{
						Class<? extends Entity> clazz = EntityList.getClass(new ResourceLocation(split[i].trim()));
						if (clazz == null)
						{
							RoughMobs.logError("Target Blocker: \"" + split[1] + "\" is not a valid entity!");
							success = false;
							break;
						}
						entities[i] = clazz;
					}
				}
				
				if (success)
					LIST.add(new TargetEntry(entities[1], entities[0]));
			}
			else
				RoughMobs.logError("Target Blocker: each option needs at least 2 arguments! (" + option + ")");
		}
	}

	public static Class<? extends Entity> getBlockerEntityForTarget(Entity target) {

		for (TargetEntry entry : LIST) 
		{
			if (target.getClass().equals(entry.getTargetClass()))
				return entry.getAttackerClass();
		}
		
		return null;
	}
	
	public static boolean targetBlockerEnabled() {
		return enableTargetBlock;
	}
	
	static class TargetEntry {
		
		private final Class<? extends Entity> attackerClass;
		private final Class<? extends Entity> targetClass;
		
		public TargetEntry(Class<? extends Entity> attackerClass, Class<? extends Entity> targetClass) {
			this.attackerClass = attackerClass;
			this.targetClass = targetClass;
		}
		
		public Class<? extends Entity> getAttackerClass() {
			return attackerClass;
		}
		
		public Class<? extends Entity> getTargetClass() {
			return targetClass;
		}
	}
}
