package edu.jhu.ugrad.sseo6;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;

@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels = {})

@Mod(modid="mod_sseo6_DBServerStats", name="DB Server Stats", version="0.0.1")
public class DBServerMain {
	@EventHandler
	public void initialize(FMLInitializationEvent event){
		
	}
	
	@EventHandler
	public void shutdown(FMLServerStoppingEvent event){
		
	}
}
