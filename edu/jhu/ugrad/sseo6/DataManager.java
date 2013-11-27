package edu.jhu.ugrad.sseo6;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;

import edu.jhu.ugrad.sseo6.util.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class DataManager {
	
	private boolean initialized = false;
	
	private HashMap<String, Pair<Integer, Long> > timeAtLogin;
	
	public DataManager(){
		timeAtLogin = new HashMap<String, Pair<Integer, Long> >();
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
		if(player.worldObj.isRemote)
			return;
		
		Connection con = DBServerMain.instance().sqlManager.getConnection();
		if(con != null)
		{
			if(!checkPlayerEntry(player.username, con))
			{
				setupPlayerEntries(player, con);
			}
		}
		
		try {
			con.close();
		} catch (SQLException e) {}
		
		timeAtLogin.put(player.username, new Pair<Integer, Long>(player.dimension, Calendar.getInstance().getTimeInMillis()));
	}
	
	public void playerLoggedOut(EntityPlayer player){
		//player.experienceLevel
		//player.getScore()
	}

	public void playerChangedDimension(EntityPlayer player) {
		Pair<Integer, Long> prev = timeAtLogin.remove(player.username);
		int timeSpent = (int)((Calendar.getInstance().getTimeInMillis() - prev.b) / 1000);
		
		Connection con = DBServerMain.instance().sqlManager.getConnection();
		try {
		timeSpent += Integer.parseInt(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Time FROM Time_Spent WHERE Player = " + player.username, con));
		DBServerMain.instance().sqlManager.updateQuery(
				"UPDATE Time_Spent SET Time=" + timeSpent + " WHERE Player = " + player.username, con);
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.out.println("  Was the database incorrectly initialized?");
		}
		try {
			con.close();
		} catch (SQLException e) {}
		
		timeAtLogin.put(player.username, new Pair<Integer, Long>(player.dimension, Calendar.getInstance().getTimeInMillis()));
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
	
	/**
	 * Checks if a username is in the database.
	 * @param username A String object that is the username to check.
	 * @param connection The SQL connection to use if not null.
	 * @return True if the username is in the database.
	 */
	private boolean checkPlayerEntry(String username, Connection connection){
		if(DBServerMain.instance().sqlManager.standardQuery("SELECT Username FROM Player WHERE Username = " + username, connection) == null)
			return false;
		return true;
	}
	
	private void setupPlayerEntries(EntityPlayer player, Connection connection){
		Calendar calObj = Calendar.getInstance();
		java.sql.Date dateObj = new java.sql.Date(calObj.getTimeInMillis());
		java.sql.Time timeObj = new java.sql.Time(calObj.getTimeInMillis());
		String strTime = dateObj.toString() + " " + timeObj.toString();
		
		DBServerMain.instance().sqlManager.updateQuery("INSERT INTO Player VALUES ('" + player.username + "', '" + strTime + "', " +
				player.experienceLevel + ", " + player.getScore() + ")", connection);
		
		
		Integer[] dims = DimensionManager.getIDs();
		
		for(Integer dim : dims){
			DBServerMain.instance().sqlManager.updateQuery("INSERT INTO Time_Spent VALUES ('" + player.username +
					"', " + dim + ", 0)", connection);
		}
	}
}
