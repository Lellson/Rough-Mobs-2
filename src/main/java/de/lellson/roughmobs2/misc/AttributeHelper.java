package de.lellson.roughmobs2.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

import de.lellson.roughmobs2.RoughMobs;
import de.lellson.roughmobs2.config.RoughConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class AttributeHelper {
	
	private static Multimap<Class<? extends Entity>, AttributeEntry> map;
	public static final String KEY_ATTRIBUTES = Constants.unique("attributesApplied");
	
	public static class AttributeEntry {
		
		private UUID uuid;
		private String attribute;
		private int operator;
		private double value;
		private int dimension;
		private int child;
		
		public AttributeEntry(UUID uuid, String attribute, int operator, double value, int dimension, int child) {
			this.uuid = uuid;
			this.attribute = attribute;
			this.operator = operator;
			this.value = value;
			this.dimension = dimension;
			this.child = child;
		}
		
		public UUID getUuid() {
			return uuid;
		}
		
		public String getAttribute() {
			return attribute;
		}
		
		public int getOperator() {
			return operator;
		}
		
		public double getValue() {
			return value;
		}
		
		public int getDimension() {
			return dimension;
		}
		
		public int getChild() {
			return child;
		}
		
		public boolean hasDimension() {
			return dimension != Integer.MIN_VALUE;
		}

		public boolean checkChild(EntityLivingBase entity) {
			return child == 0 || (child == 1 && !entity.isChild()) || (child == 2 && entity.isChild());
		}
	}

	public static void initAttributeOption() {
		
		RoughConfig.getConfig().addCustomCategoryComment("attributes", "Add attribute modifiers to entities to change their stats. Takes 4-6 values seperated by a semicolon:\n"
																		+ "Format: entity;attribute;operator;value;dimension;child\n"
																		+ "entity:\t\tentity name\n"
																		+ "attribute:\tattribute name (Possible attributes: " + getAttributesString() + ")\n"
																		+ "operator:\t\toperator type (0 = add, 1 = multiply and add)\n"
																		+ "value:\t\tvalue which will be used for the calculation\n"
																		+ "dimension:\tdimension (ID) in which the entity should get the boost (optional! Leave this blank or use a \"/\" for any dimension)\n"
																		+ "child:\t0 = the modifier doesn't care if the entity is a child or not, 1 = adults only, 2 = childs only (optional! Leave this blank for 0)");
		
		String[] options = RoughConfig.getStringArray("attributes", "Modifier", Constants.ATTRIBUTE_DEFAULT, "Attributes:");
		fillMap(options);
	}
	
	public static boolean applyAttributeModifier(EntityLivingBase entity, IAttribute attribute, String name, int operator, double amount) {
		
		if (amount != 0) 
		{
			IAttributeInstance instance = entity.getEntityAttribute(attribute);
			AttributeModifier modifier = new AttributeModifier(Constants.unique(name), amount, operator);
			
			if (instance != null && !instance.hasModifier(modifier)) 
			{
				instance.applyModifier(modifier);
				if (attribute == SharedMonsterAttributes.MAX_HEALTH) 
					entity.setHealth(entity.getMaxHealth());	
				return true;
			}
		}
		
		return false;
	}
	
	
	public static void addAttributes(EntityLivingBase entity) {
		
		if (entity.getEntityData().getBoolean(KEY_ATTRIBUTES))
			return;
		
		Collection<AttributeEntry> attributes = map.get(entity.getClass());
		
		int i = 0;
		for (AttributeEntry attribute : attributes) 
		{
			if (!attribute.checkChild(entity))
				continue;
			
			IAttributeInstance instance = entity.getAttributeMap().getAttributeInstanceByName(attribute.attribute);
			if (instance != null) 
			{
				AttributeModifier modifier = new AttributeModifier(attribute.getUuid(), Constants.unique("mod" + i), attribute.getValue(), attribute.getOperator());
				instance.applyModifier(modifier);
				
				if (instance.getAttribute() == SharedMonsterAttributes.MAX_HEALTH)
					entity.setHealth(entity.getMaxHealth());
			}
			else
				RoughMobs.logError("Error on attribute modification: \"" + attribute.attribute + "\" is not a valid attribute");
			
			i++;
		}
		
		entity.getEntityData().setBoolean(KEY_ATTRIBUTES, true);
	}

	private static void fillMap(String[] options) {	
		map = ArrayListMultimap.create();
		
		for (String line : options) 
		{
			String[] pars = line.split(";");
			
			if (pars.length >= 4) 
			{
				Class<? extends Entity> entityClass = EntityList.getClass(new ResourceLocation(pars[0]));
				if (entityClass != null) 
				{
					try 
					{
						int operator = Integer.parseInt(pars[2]);
						double value = Double.parseDouble(pars[3]);
						int dimension = pars.length >= 5 && !pars[4].equals("/") ? Integer.parseInt(pars[4]) : Integer.MIN_VALUE;
						int child = pars.length >= 6 ? Integer.parseInt(pars[5]) : 0;
						
						if (child < 0 || child > 2)
						{
							RoughMobs.logError("Error on attribute initialization: child is not between 0 and 2: " + line);
							continue;
						}
						
						map.put(entityClass, new AttributeEntry(UUID.randomUUID(), pars[1], operator, value, dimension, child));
					}
					catch(NumberFormatException e) 
					{
						RoughMobs.logError("Error on attribute initialization: Invalid numbers: " + line);
					}
				}
				else
					RoughMobs.logError("Error on attribute initialization: Entity " + pars[0] + " does not exist");
			}
			else
				RoughMobs.logError("Error on attribute initialization: Wrong amount of arguments: " + line);
		}
	}

	private static String getAttributesString() {
		
		String attributes = "";
		
		try
		{
			for (Field field : SharedMonsterAttributes.class.getFields()) 
			{
				field.setAccessible(true);
				Object obj = field.get(null);
				if (obj != null && obj instanceof IAttribute) 
				{
					attributes += ", " + ((IAttribute)obj).getName();
				}
			}
		}
		catch(Exception e) {}
		
		return attributes.length() > 2 ? attributes.substring(2) : "";	
	    //return "generic.maxHealth, generic.followRange, generic.knockbackResistance, generic.movementSpeed, generic.flyingSpeed, generic.attackDamage, generic.attackSpeed, generic.armor, generic.armorToughness, generic.luck";
	}
}
