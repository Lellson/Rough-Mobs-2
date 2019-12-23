package de.lellson.roughmobs2.config;

import de.lellson.roughmobs2.RoughApplier;
import de.lellson.roughmobs2.features.EntityFeatures;
import de.lellson.roughmobs2.misc.AttributeHelper;
import de.lellson.roughmobs2.misc.SpawnHelper;
import net.minecraft.block.BlockDoor;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class RoughConfig {
	
	private static Configuration config;
	
	public RoughConfig(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
	}

	public static void loadFeatures() {
		
		config.load();
		
		for (EntityFeatures features : RoughApplier.FEATURES) 
			features.initConfig();
	}
	
	public static void saveFeatures() {
		
		if (config.hasChanged())
			config.save();
	}
	
	public static Configuration getConfig() {
		return config;
	}
	
	public static boolean getBoolean(String name, String id, boolean defaultValue, String description, boolean important) {
		return config.getBoolean((important ? "_" : "") + name + id, name, defaultValue, description.replace("%s", name));
	}
	
	public static boolean getBoolean(String name, String id, boolean defaultValue, String description) {
		return getBoolean(name, id, defaultValue, description, false);
	}
	
	public static int getInteger(String name, String id, int defaultValue, int min, int max, String description, boolean important) {
		return config.getInt((important ? "_" : "") + name + id, name, defaultValue, min, max, description.replace("%s", name));
	}
	
	public static int getInteger(String name, String id, int defaultValue, int min, int max, String description) {
		return getInteger(name, id, defaultValue, min, max, description, false);
	}
	
	public static float getFloat(String name, String id, float defaultValue, float min, float max, String description, boolean important) {
		return config.getFloat((important ? "_" : "") + name + id, name, defaultValue, min, max, description.replace("%s", name));
	}
	
	public static float getFloat(String name, String id, float defaultValue, float min, float max, String description) {
		return getFloat(name, id, defaultValue, min, max, description, false);
	}
	
	public static String[] getStringArray(String name, String id, String[] defaultValue, String description, boolean important) {
		return config.getStringList((important ? "_" : "") + name + id, name, defaultValue, description.replace("%s", name));
	}
	
	public static String[] getStringArray(String name, String id, String[] defaultValue, String description) {
		return getStringArray(name, id, defaultValue, description, false);
	}
	
	public static String getString(String name, String id, String defaultValue, String description, boolean important) {
		return config.getString((important ? "_" : "") + name + id, name, defaultValue, description.replace("%s", name));
	}
	
	public static String getString(String name, String id, String defaultValue, String description) {
		return getString(name, id, defaultValue, description, false);
	}
}
