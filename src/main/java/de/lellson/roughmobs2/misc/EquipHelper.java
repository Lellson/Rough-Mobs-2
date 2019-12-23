package de.lellson.roughmobs2.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ibm.icu.impl.CalendarAstronomer.Equatorial;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.EquipHelper.EquipmentPool;

import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;

public class EquipHelper {
	
	private static final String KEY_APPLIED = Constants.unique("equipApplied");
	private static final Random RND = new Random();
	
	public static class EquipmentApplier {
		
		private final String name;
		private final int chancePerWeaponDefault;
		private final int chancePerPieceDefault;
		private final int enchChanceDefault;
		private final float enchMultiplierDefault;
		private final float dropChanceDefault;
		
		private EquipmentPool poolMainhand;
		private EquipmentPool poolOffhand;
		
		private EquipmentPool poolHelmet;
		private EquipmentPool poolChestplate;
		private EquipmentPool poolLeggings;
		private EquipmentPool poolBoots;
		
		private int chancePerWeapon;
		private int chancePerPiece;
		private int enchChance;
		private float enchMultiplier;
		private float dropChance;
		
		private String[] equipMainhand;
		private String[] equipOffhand;
		
		private String[] equipHelmet;
		private String[] equipChestplate;
		private String[] equipLeggings;
		private String[] equipBoots;
		
		private String[] equipWeaponEnchants;
		private String[] equipArmorEnchants;
		
		public EquipmentApplier(String name, int chancePerWeaponDefault, int chancePerPieceDefault, int enchChanceDefault, float enchMultiplierDefault, float dropChanceDefault) {
			this.name = name;
			this.chancePerWeaponDefault = chancePerWeaponDefault;
			this.chancePerPieceDefault = chancePerPieceDefault;
			this.enchChanceDefault = enchChanceDefault;
			this.enchMultiplierDefault = enchMultiplierDefault;
			this.dropChanceDefault = dropChanceDefault;
		}
		
		public EquipmentPool getPoolMainhand() {
			return poolMainhand;
		}
		
		public EquipmentPool getPoolOffhand() {
			return poolOffhand;
		}
		
		public EquipmentPool getPoolHelmet() {
			return poolHelmet;
		}
		
		public EquipmentPool getPoolChestplate() {
			return poolChestplate;
		}
		
		public EquipmentPool getPoolLeggings() {
			return poolLeggings;
		}
		
		public EquipmentPool getPoolBoots() {
			return poolBoots;
		}
		
		public void setPoolMainhand(EquipmentPool poolMainhand) {
			this.poolMainhand = poolMainhand;
		}
		
		public void setPoolOffhand(EquipmentPool poolOffhand) {
			this.poolOffhand = poolOffhand;
		}
		
		public void setPoolHelmet(EquipmentPool poolHelmet) {
			this.poolHelmet = poolHelmet;
		}
		
		public void setPoolChestplate(EquipmentPool poolChestplate) {
			this.poolChestplate = poolChestplate;
		}
		
		public void setPoolLeggings(EquipmentPool poolLeggings) {
			this.poolLeggings = poolLeggings;
		}
		
		public void setPoolBoots(EquipmentPool poolBoots) {
			this.poolBoots = poolBoots;
		}
		
		public void equipEntity(EntityLiving entity) {
			
			if (entity == null || entity.getEntityData().getBoolean(KEY_APPLIED))
				return;
			
			EquipmentPool[] pools = new EquipmentPool[] {
					poolMainhand, poolOffhand, poolBoots, poolLeggings, poolChestplate, poolHelmet
			};
			
			for (int i = 0; i < pools.length; i++) 
			{
				EquipmentPool pool = pools[i];
				EntityEquipmentSlot slot = EntityEquipmentSlot.values()[i];
				
				int rnd = i <= 1 ? chancePerWeapon : chancePerPiece;
				if (rnd > 0 && RND.nextInt(rnd) == 0) 
				{
					ItemStack stack = pool.getRandom(entity, enchChance, enchMultiplier);
					if (stack != null) 
					{
						entity.setItemStackToSlot(slot, stack);
						entity.setDropChance(slot, dropChance);
					}
				}
			}
			
			entity.getEntityData().setBoolean(KEY_APPLIED, true);
		}

		public String initConfig(String[] defaultMainhand, String[] defaultOffhand, String[] defaultHelmets, String[] defaultChestplates, String[] defaultLeggings, String[] defaultBoots, String[] defaultWeaponEnchants, String[] defaultArmorEnchants, boolean skipChanceOptions) {

			String formatName = name.toLowerCase().replace(" ", "") + "Equipment";
			RoughConfig.getConfig().addCustomCategoryComment(formatName, "Add enchanted armor and weapons to a newly spawned " + name + ". Takes 2-3 values seperated by a semicolon:\n"
																				 + "Format: item or enchantment;chance;dimension\n"
																				 + "item or enchantment:\tthe item/enchantment id\n"
																				 + "chance:\t\t\t\tthe higher this number the more this item/enchantment gets selected\n"
																				 + "dimension:\t\t\tdimension (ID) in which the item/enchantment can be selected (optional! Leave this blank for any dimension)");
			
			if (skipChanceOptions) 
			{
				chancePerWeapon = chancePerWeaponDefault;
				chancePerPiece = chancePerPieceDefault;
				enchChance = enchChanceDefault;
			}
			else
			{
				chancePerWeapon = RoughConfig.getInteger(formatName, "WeaponChance", chancePerWeaponDefault, 0, Short.MAX_VALUE, "Chance (1 in X per hand) to give a " + name + " new weapons on spawn\nSet to 0 to disable new weapons", true);
				chancePerPiece = RoughConfig.getInteger(formatName, "ArmorChance", chancePerPieceDefault, 0, Short.MAX_VALUE, "Chance (1 in X per piece) to give a " + name + " new armor on spawn\nSet to 0 to disable new armor", true);
				enchChance = RoughConfig.getInteger(formatName, "EnchantChance", enchChanceDefault, 0, Short.MAX_VALUE, "Chance (1 in X per item) to enchant newly given items\nSet to 0 to disable item enchanting", true);
			}
			
			enchMultiplier = RoughConfig.getFloat(formatName, "EnchantMultiplier", enchMultiplierDefault, 0F, 1F, "Multiplier for the applied enchantment level with the max. level. The level can still be a bit lower\ne.g. 0.5 would make sharpness to be at most level 3 (5 x 0.5 = 2.5 and [2.5] = 3) and fire aspect would always be level 1 (2 x 0.5 = 1)", true);
			dropChance = RoughConfig.getFloat(formatName, "DropChance", dropChanceDefault, 0F, 1F, "Chance (per slot) that the " + name + " drops the equipped item (1 = 100%, 0 = 0%)", true);
			
			equipMainhand = RoughConfig.getStringArray(formatName, "Mainhand", defaultMainhand, "Items which can be wielded by a " + name + " in their mainhand");
			equipOffhand = RoughConfig.getStringArray(formatName, "Offhand", defaultOffhand, "Items which can be wielded by a " + name + " in their offhand");
			equipHelmet = RoughConfig.getStringArray(formatName, "Helmet", defaultHelmets, "Helmets which can be worn by a " + name + " in their helmet slot");
			equipChestplate = RoughConfig.getStringArray(formatName, "Chestplate", defaultChestplates, "Chestplates which can be worn by a " + name + " in their chestplate slot");
			equipLeggings = RoughConfig.getStringArray(formatName, "Leggings", defaultLeggings, "Leggings which can be worn by a " + name + " in their leggings slot");
			equipBoots = RoughConfig.getStringArray(formatName, "Boots", defaultBoots, "Boots which can be worn by a " + name + " in their boots slot");
			
			equipWeaponEnchants = RoughConfig.getStringArray(formatName, "WeaponEnchants", defaultWeaponEnchants, "Enchantments which can be applied to mainhand and offhand items");
			equipArmorEnchants = RoughConfig.getStringArray(formatName, "ArmorEnchants", defaultArmorEnchants, "Enchantments which can be applied to armor items");
			
			return formatName;
		}
		
		public void createPools() {
			
			setPoolMainhand(EquipmentPool.createEquipmentPool("mainhand", equipMainhand, equipWeaponEnchants));
			setPoolOffhand(EquipmentPool.createEquipmentPool("offhand", equipOffhand, equipWeaponEnchants));
			setPoolHelmet(EquipmentPool.createEquipmentPool("helmet", equipHelmet, equipArmorEnchants));
			setPoolChestplate(EquipmentPool.createEquipmentPool("chestplate", equipChestplate, equipArmorEnchants));
			setPoolLeggings(EquipmentPool.createEquipmentPool("leggings", equipLeggings, equipArmorEnchants));
			setPoolBoots(EquipmentPool.createEquipmentPool("boots", equipBoots, equipArmorEnchants));
		}
	}
	
	public static class EquipmentPool {
		
		public final EntryPool<ItemStack> ITEM_POOL = new EntryPool<ItemStack>();
		public final EntryPool<Enchantment> ENCHANTMENT_POOL = new EntryPool<Enchantment>();
		
		public static EquipmentPool createEquipmentPool(String name, String[] arrayItems, String[] arrayEnchants) {
			
			EquipmentPool pool = new EquipmentPool();
			
			List<String> errorItems = pool.addItemsFromNames(arrayItems);
			if (!errorItems.isEmpty()) 
				RoughMobs.logError(Constants.MODNAME + ": error on creating the " + name + " item pool! " + String.join(", ", errorItems));
			
			List<String> errorEnchants = pool.addEnchantmentsFromNames(arrayEnchants);
			if (!errorEnchants.isEmpty()) 
				RoughMobs.logError(Constants.MODNAME + ": error on creating the " + name + " enchantment pool! " + String.join(", ", errorEnchants));
			
			return pool;
		}
		
		public List<String> addEnchantmentsFromNames(String[] array) {
			
			List<String> errors = new ArrayList<String>();
			for (String s : array)
			{
				String error = addEnchantmentFromName(s);
				if (error != null)
					errors.add(error);
			}	
			
			return errors;
		}
		
		private String addEnchantmentFromName(String s) {
			
			String[] parts = s.split(";");
			
			if (parts.length >= 2) 
			{
				try 
				{
					Enchantment ench = Enchantment.getEnchantmentByLocation(parts[0]);
					int probability = Integer.parseInt(parts[1]);
					int dimension = parts.length > 2 ? Integer.parseInt(parts[2]) : Integer.MIN_VALUE;
					
					if (ench == null)
						return "Invalid enchantment: " + parts[0] + " in line: " + s;
					else
						addEnchantment(ench, probability, dimension);
				}
				catch(NumberFormatException e) 
				{
					return "Invalid numbers in line: " + s;
				}
			}
			else
			{
				return "Invalid format for line: \"" + s + "\" Please change to enchantment;probability;dimensionID";
			}
			
			return null;
		}

		public List<String> addItemsFromNames(String[] array) {
			
			List<String> errors = new ArrayList<String>();
			for (String s : array)
			{
				String error = addItemFromName(s);
				if (error != null)
					errors.add(error);
			}
			
			return errors;
		}
		
		private String addItemFromName(String s) {
			
			String[] parts = s.split(";");
			
			if (parts.length >= 2) 
			{
				try 
				{
					Item item = Item.REGISTRY.getObject(new ResourceLocation(parts[0]));
					int probability = Integer.parseInt(parts[1]);
					int dimension = parts.length >= 3 ? Integer.parseInt(parts[2]) : Integer.MIN_VALUE;
					int meta = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0;
					String nbt = parts.length >= 5 ? parts[4] : "";
					
					if (item == null)
						return "Invalid item: " + parts[0] + " in line: " + s;
					else
						addItem(new ItemStack(item, 1, meta), probability, dimension, nbt);
				}
				catch(NumberFormatException e) 
				{
					return "Invalid numbers in line: " + s;
				}
			}
			else
			{
				return "Invalid format for line: \"" + s + "\" Please change to item;probability;meta";
			}
			
			return null;
		}

		public void addItem(ItemStack stack, int probability, int dimension, String nbt) {
			ITEM_POOL.addEntry(stack, probability, dimension == Integer.MIN_VALUE ? "ALL" : dimension, nbt);
		}
		
		public void addEnchantment(Enchantment enchantment, int probability, int dimension) {
			ENCHANTMENT_POOL.addEntry(enchantment, probability, dimension == Integer.MIN_VALUE ? "ALL" : dimension);
		}
		
		public ItemStack getRandom(Entity entity, int enchChance, float levelMultiplier) {
			
			if (ITEM_POOL.POOL.isEmpty()) 
				return null;
			
			ItemStack randomStack = ITEM_POOL.getRandom(entity);
			
			if (!ENCHANTMENT_POOL.POOL.isEmpty() && enchChance > 0 && RND.nextInt(enchChance) == 0) 
			{
				Enchantment ench = ENCHANTMENT_POOL.getRandom(entity);
				
				int i = 10;
				while (!ench.canApply(randomStack) && i > 0) 
				{
					ench = ENCHANTMENT_POOL.getRandom(entity);
					i--;
				}
				
				if (!ench.canApply(randomStack))
					return randomStack;
				
				double maxLevel = Math.max(ench.getMinLevel(), Math.min(ench.getMaxLevel(), Math.round(ench.getMaxLevel() * levelMultiplier)));
				int level = (int)Math.round(maxLevel * (0.5 + Math.random()/2));
				
				if (!randomStack.isItemEnchanted())
					randomStack.addEnchantment(ench, level);
			}
			
			return randomStack;
		}
	}
	
	public static class EntryPool<T> {

		public final Map<T, Object[]> POOL = new HashMap<T, Object[]>();
		private List<T> entries = null;
		private List<String> dimensions = new ArrayList<String>();
		private boolean needsReload;
		
		public void addEntry(T t, Object... data) {
			POOL.put(t, data);
			needsReload = true;
		}
		
		public T getRandom(Entity entity) {
			
			if (entries == null || needsReload) 
			{
				entries = new ArrayList<T>();
				for (Entry<T, Object[]> entry : POOL.entrySet()) 
				{
					Object[] data = entry.getValue();
					
					T key = entry.getKey();
					if (key instanceof ItemStack && data.length > 2 && ((String)data[2]).length() != 0) 
					{
						try 
						{
							((ItemStack)key).setTagCompound(JsonToNBT.getTagFromJson((String) data[2]));
						} 
						catch (NBTException e) 
						{
							RoughMobs.logError("NBT Tag invalid: %s", e.toString());
							e.printStackTrace();
						}
					}
					
					for (int i = 0; i < (int)data[0]; i++)
					{
						entries.add(key);
						dimensions.add(String.valueOf(data[1]));
					}
				}
				
				needsReload = false;
			}
			
			int rnd = RND.nextInt(entries.size());
			T entry = entries.get(rnd);
			String dimension = dimensions.get(rnd);
			int i = 100;
			
			while (!isDimension(entity, dimension) && i > 0) 
			{
				rnd = RND.nextInt(entries.size());
				entry = entries.get(rnd);
				dimension = dimensions.get(rnd);
				i--;
			}
			
			return entry;
		}
		
		private static boolean isDimension(Entity entity, String dimension) {
			return dimension.trim().toUpperCase().equals("ALL") || String.valueOf(entity.dimension).equals(dimension);
		}
	}
}
