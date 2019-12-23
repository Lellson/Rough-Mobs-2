package de.lellson.roughmobs2.proxy;

import de.lellson.roughmobs2.RoughApplier;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy {
	
	private RoughApplier applier;
	
	public void preInit(FMLPreInitializationEvent event) {
		
		applier = new RoughApplier();
		applier.preInit();
	}
	
	public void postInit(FMLPostInitializationEvent event) {
		
		applier.postInit();
	}
}
