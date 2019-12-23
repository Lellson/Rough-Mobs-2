package de.lellson.roughmobs2;

import de.lellson.roughmobs2.config.RoughConfig;
import de.lellson.roughmobs2.misc.AttributeHelper;
import de.lellson.roughmobs2.misc.Constants;
import de.lellson.roughmobs2.proxy.ServerProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MODID, name = Constants.MODNAME, version = Constants.MODVERSION, acceptableRemoteVersions="*")
public class RoughMobs {
	
	@SidedProxy(clientSide = "de.lellson.roughmobs2.proxy.ClientProxy", serverSide = "de.lellson.roughmobs2.proxy.ServerProxy")
	public static ServerProxy proxy;
	
	
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		new RoughConfig(event);
		proxy.preInit(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
	
	public static void logError(String format, Object... data) {
		FMLLog.bigWarning("[" + Constants.MODNAME + "]: " + format, data);
	}
}
