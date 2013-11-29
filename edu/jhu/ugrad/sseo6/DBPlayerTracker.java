package edu.jhu.ugrad.sseo6;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.IPlayerTracker;

public class DBPlayerTracker implements IPlayerTracker, ICraftingHandler{

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		if(player.worldObj.isRemote)
			return;
		DBServerMain.instance().dataManager.playerLoggedIn(player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		if(player.worldObj.isRemote)
			return;
		DBServerMain.instance().dataManager.playerLoggedOut(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		if(player.worldObj.isRemote)
			return;
		DBServerMain.instance().dataManager.playerChangedDimension(player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		
	}

	@Override
	public void onCrafting(EntityPlayer player, ItemStack item,
			IInventory craftMatrix) {
		if(player.worldObj.isRemote)
			return;
		DBServerMain.instance().dataManager.playerCraftedItem(player, item);
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {
		if(player.worldObj.isRemote)
			return;
		DBServerMain.instance().dataManager.playerCraftedItem(player, item);
	}

}
