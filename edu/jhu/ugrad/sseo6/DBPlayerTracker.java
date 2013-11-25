package edu.jhu.ugrad.sseo6;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.IPlayerTracker;

public class DBPlayerTracker implements IPlayerTracker{

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		DBServerMain.instance().dataManager.playerLoggedIn(player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		DBServerMain.instance().dataManager.playerLoggedOut(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		DBServerMain.instance().dataManager.playerChangedDimension(player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		
	}

}
