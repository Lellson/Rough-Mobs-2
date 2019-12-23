package de.lellson.roughmobs2.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.biome.BiomeHell;
import net.minecraft.world.biome.BiomeVoid;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SpawnHelper {
	
	public static final List<SpawnEntry> ENTRIES = new ArrayList<SpawnEntry>();
	
	public static class SpawnEntry {
		
		public static final Map<String, Type> TYPE_MAP = ReflectionHelper.getPrivateValue(BiomeDictionary.Type.class, null, 0);
		public static final String OW_TYPE = "OVERWORLD";
		public static final String DISABLE_KEY = "!";
		
		private boolean valid = true;
		private String error = "";
		
		public Class<? extends EntityLiving> entityClass;
		public int prob;
		public int min;
		public int max;
		public EnumCreatureType type;
		public Tuple<Biome[], Biome[]> biomes;
		
		public SpawnEntry(String entityName, String prob, String min, String max, String type, String... biomes) {
			this.entityClass = getEntityClass(entityName);
			this.prob = getInteger(prob);
			this.min = getInteger(min);
			this.max = getInteger(max);
			this.type = getType(type);
			this.biomes = getBiomes(biomes);
		}

		private Tuple<Biome[], Biome[]> getBiomes(String[] biomes) {
			
			List<Biome> listAdd = new ArrayList<Biome>();
			List<Biome> listRemove = new ArrayList<Biome>();
			
			for (String biomeId : biomes) 
			{
				List<Biome> list = biomeId.startsWith(DISABLE_KEY) ? listRemove : listAdd;
				if (biomeId.startsWith(DISABLE_KEY))
					biomeId = biomeId.substring(1);
				
				Type type = TYPE_MAP.get(biomeId);
				if (biomeId.equals(OW_TYPE))
				{
					for (Biome biome : SpawnHelper.getOverworldBiomes())
					{
						list.add(biome);
					}
				}
				else if (type != null) 
				{
					list.addAll(BiomeDictionary.getBiomes(type));
				}
				else
				{
					Biome biome = Biome.REGISTRY.getObject(new ResourceLocation(biomeId));
					if (biome != null) 
					{
						list.add(biome);
					}
					else
					{
						int id = getInteger(biomeId);
						if (id >= 0) 
						{
							biome = Biome.getBiomeForId(id);
							if (biome != null)
								list.add(biome);
							else 
							{
								this.valid = false;
								this.error = "Biomes with the id/name " + id + " don't exist!";
							}
						}
					}
				}
			}
			
			if (biomes.length == 0) 
			{
				for (Biome biome : Biome.REGISTRY) 
					listAdd.add(biome);
			}
			
			Biome[] resultToAdd = new Biome[listAdd.size()];
			Biome[] resultToRemove = new Biome[listRemove.size()];
			return new Tuple<Biome[], Biome[]>(listAdd.toArray(resultToAdd), listRemove.toArray(resultToRemove));
		}

		private EnumCreatureType getType(String type) {

			switch(type.toUpperCase()) 
			{
				case "AMBIENT": return EnumCreatureType.AMBIENT;
				case "CREATURE": return EnumCreatureType.CREATURE;
				case "MONSTER": return EnumCreatureType.MONSTER;
				case "WATER_CREATURE": return EnumCreatureType.WATER_CREATURE;
			}
			
			this.valid = false;
			this.error = "Creature type " + type + " doesn't exist!";
			return null;
		}

		private int getInteger(String str) {
			
			try 
			{
				return Integer.parseInt(str);
			}
			catch(NumberFormatException e)
			{
				this.valid = false;
				error = prob + " is not a valid number!";
				return -1;
			}
		}

		private Class<? extends EntityLiving> getEntityClass(String entityName) {
			
			Class<? extends Entity> clazz = EntityList.getClass(new ResourceLocation(entityName));
			
			if (clazz != null) 
			{
				try 
				{
					return (Class<? extends EntityLiving>)clazz;
				}
				catch(Exception e) 
				{
					this.valid = false;
					error = entityName + " is not a valid living entity!";
				}
			}
			else
			{
				this.valid = false;
				error = entityName + " is not a valid entity!";
			}
			
			return null;
		}
		
		public boolean isValid() {
			return valid;
		}
		
		public String getError() {
			return error;
		}
	}
	
	public static void initSpawnOption() {
		
		RoughConfig.getConfig().addCustomCategoryComment("spawnEntries", "Add custom entity spawn entries or override old ones. Takes 5+ values seperated by a semicolon:\n" +
														"Format: entity;chance;min;max;type;biome1;biome2;...\n" +
														"entity:\tEntity name\n" + 
														"chance:\tSpawn chance\n" +
														"min:\t\tMinimal group size. Must be greater than 0\n" +
														"max:\t\tMaximal group size\n" +
														"type:\t\tSpawn Type (AMBIENT = day and night, CREATURE = day only, MONSTER = night only and not in peaceful mode, WATER_CREATURE = only in water)\n" +
														"biomes:\tBiome name/id/type (Can be more than one). Put a \"!\" in front of the biome to revert this feature and disable entity spawning in the biome. Use " + SpawnEntry.OW_TYPE + " for all non nether/end biomes (Doesn't work with BoP hell biomes). Leave this blank for every biome!");
		
		String[] options = RoughConfig.getStringArray("spawnEntries", "List", Constants.DEFAULT_SPAWN_ENTRIES, "");
		fillEntries(options);
	}

	public static List<Biome> getOverworldBiomes() {

		List<Biome> biomes = new ArrayList<Biome>();
		
		for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection())
		{
			if (biome instanceof BiomeVoid || biome instanceof BiomeHell || biome instanceof BiomeEnd)
				continue;
			
			if (!biomes.contains(biome))
				biomes.add(biome);
		}
		
		return biomes;
	}

	private static void fillEntries(String[] options) {
		
		for (String option : options) 
		{
			String[] parts = option.split(";");
			if (parts.length >= 5) 
			{
				String[] biomes = new String[parts.length - 5];
				for (int i = 0; i < biomes.length; i++)
					biomes[i] = parts[5+i];
				
				SpawnEntry entry = new SpawnEntry(parts[0], parts[1], parts[2], parts[3], parts[4], biomes);
				
				if (entry.isValid())
					ENTRIES.add(entry);
				else
					RoughMobs.logError("Spawn Entries: " + entry.getError());
			}
			else
			{
				RoughMobs.logError("Spawn Entries: Entry \"" + option + "\" needs at least 5 values!");
			}
		}
	}

	public static void addEntries() {
		
		for (SpawnEntry entry : ENTRIES) 
		{
			EntityRegistry.addSpawn(entry.entityClass, entry.prob, entry.min, entry.max, entry.type, entry.biomes.getFirst());
			
			
			if (entry.biomes.getSecond().length != 0)
				for (Biome b : entry.biomes.getSecond())
					System.out.println(b.getBiomeName());
			
			EntityRegistry.removeSpawn(entry.entityClass, entry.type, entry.biomes.getSecond());
		}
	}
}
