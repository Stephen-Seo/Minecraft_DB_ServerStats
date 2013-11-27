package edu.jhu.ugrad.sseo6;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class DataManager {
	
	private boolean initialized = false;
	
	public DataManager(){
		
	}
	
	public boolean initialize(Connection con){
		Statement statement;
		ResultSet result;
		try {
			statement = con.createStatement();
			result = statement.executeQuery("SELECT Username FROM Player");
			
			while(result.next()){
				
			}
			try {
				result.close();
			} catch(SQLException e) {}
			try {
				statement.close();
			} catch(SQLException e) {}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		initialized = true;
		return true;
	}
	
	public void playerLoggedIn(EntityPlayer player){
		
	}
	
	public void playerLoggedOut(EntityPlayer player){
		//player.experienceLevel
		//player.getScore()
	}

	public void playerChangedDimension(EntityPlayer player) {
		
	}
	
	public void playerCraftedItem(EntityPlayer player, ItemStack item) {
		
	}
	
	@ForgeSubscribe
	public void playerDeathEvent(LivingDeathEvent event){
		if(!(event.entityLiving instanceof EntityPlayer) && event.entityLiving.worldObj.isRemote)
			return;
		//event.source.getSourceOfDamage().getEntityName()
	}
	
	@ForgeSubscribe
	public void playerChatEvent(ServerChatEvent event){
		
	}
	
	@ForgeSubscribe
	public void playerPickupEvent(EntityItemPickupEvent event){
		
	}
	
	@ForgeSubscribe
	public void playerInteractEvent(PlayerInteractEvent event){
		
	}
	
	@ForgeSubscribe
	public void playerItemBreakEvent(PlayerDestroyItemEvent event){
		
	}
	
	public void serverShuttingDown(){
		
	}
}
