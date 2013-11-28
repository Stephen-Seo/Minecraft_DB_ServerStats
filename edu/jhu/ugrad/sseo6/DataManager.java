package edu.jhu.ugrad.sseo6;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import edu.jhu.ugrad.sseo6.util.Pair;
import edu.jhu.ugrad.sseo6.util.Utility;

public class DataManager {
	
	private boolean initialized = false;
	
	private HashMap<String, Pair<Integer, Long> > timeAtLogin;
	
	public DataManager(){
		timeAtLogin = new HashMap<String, Pair<Integer, Long> >();
	}
	
	public boolean initialize(Connection con){
		if(!checkInitTables(con))
		{
			initialized = false;
			return false;
		}
		setupItems(con);
		
		initialized = true;
		return true;
	}
	
	/**
	 * Checks if the tables have been initialized in the database.
	 * If the tables have not been initialized, an attempt is made
	 * to initialize them using a mc_db.sql file that should be
	 * located in the DBServerStats folder that is auto-generated
	 * by this mod.
	 * @param con The SQL connection to use if if not null.
	 */
	private boolean checkInitTables(Connection con) {
		if(DBServerMain.instance().sqlManager.querySQLExceptionCheck("SELECT * FROM Player", con))
		{
			File sqlFile = new File(DBServerMain.modDir+"/mc_db.sql");
			Scanner fs = null;
			try {
				fs = new Scanner(sqlFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("ERROR: Cannot initialize database as there is no mc_db.sql file in");
				System.out.println("the directory "+DBServerMain.modDir+" .");
				return false;
			}
			String res;
			
			while(fs.hasNext())
			{
				res = fs.nextLine();
				if(!res.equals(""))
				{
					System.out.println("Executing: " + res);
					DBServerMain.instance().sqlManager.anyQuery(res, con);
				}
			}
			
			fs.close();
			return true;
		}
		else
			return true;
	}

	public void playerLoggedIn(EntityPlayer player){
		if(player.worldObj.isRemote)
			return;
		
		if(!initialized)
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
		if(!initialized)
			return;
		
		int expLevel = player.experienceLevel;
		int score = player.getScore();
		Pair<Integer, Long> prev = timeAtLogin.remove(player.username);
		
		Connection con = DBServerMain.instance().sqlManager.getConnection();
		
		DBServerMain.instance().sqlManager.updateQuery(
				"UPDATE Player SET Exp_Level = " + expLevel +
				", Score = " + score + " WHERE Player = " + player.username, con);
		
		int timeSpent = (int)((Calendar.getInstance().getTimeInMillis() - prev.b) / 1000);
		timeSpent += Integer.parseInt(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Time FROM Time_Spent WHERE Player = " + player.username, con));
		DBServerMain.instance().sqlManager.updateQuery(
				"UPDATE Time_Spent SET Time = " + timeSpent +
				" WHERE Player = " + player.username + " AND Dimension_ID = " + player.dimension, con);
		
		try {
			con.close();
		} catch (SQLException e) {}
	}

	public void playerChangedDimension(EntityPlayer player) {
		if(!initialized)
			return;
		
		Pair<Integer, Long> prev = timeAtLogin.remove(player.username);
		int timeSpent = (int)((Calendar.getInstance().getTimeInMillis() - prev.b) / 1000);
		
		Connection con = DBServerMain.instance().sqlManager.getConnection();
		try {
		timeSpent += Integer.parseInt(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Time FROM Time_Spent WHERE Player = " + player.username, con));
		DBServerMain.instance().sqlManager.updateQuery(
				"UPDATE Time_Spent SET Time = " + timeSpent +
				" WHERE Player = " + player.username + " AND Dimension_ID = " + player.dimension, con);
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
		if(!initialized)
			return;
		
	}
	
	@ForgeSubscribe
	public void playerDeathEvent(LivingDeathEvent event){
		if(!initialized)
			return;
		
		if(!(event.entityLiving instanceof EntityPlayer) && event.entityLiving.worldObj.isRemote)
			return;
		//event.source.getSourceOfDamage().getEntityName()
	}
	
	@ForgeSubscribe
	public void playerChatEvent(ServerChatEvent event){
		if(!initialized)
			return;
		
		Calendar calObj = Calendar.getInstance();
		java.sql.Date dateObj = new java.sql.Date(calObj.getTimeInMillis());
		java.sql.Time timeObj = new java.sql.Time(calObj.getTimeInMillis());
		String strTime = dateObj.toString() + " " + timeObj.toString();
		
		DBServerMain.instance().sqlManager.updateQuery(
				"INSERT INTO Chat_Log (Time, Player, Message) VALUES (" + strTime +
				", " + event.username + ", \"" + Utility.escapeDoubleQuotes(event.message) + "\")", null);
		
	}
	
	@ForgeSubscribe
	public void playerPickupEvent(EntityItemPickupEvent event){
		if(!initialized)
			return;
		
	}
	
	@ForgeSubscribe
	public void playerInteractEvent(PlayerInteractEvent event){
		if(!initialized)
			return;
		
	}
	
	@ForgeSubscribe
	public void playerItemBreakEvent(PlayerDestroyItemEvent event){
		if(!initialized)
			return;
		
	}
	
	public void serverShuttingDown(){
		if(!initialized)
			return;
		
	}
	
	/**
	 * Checks if a username is in the database.
	 * @param username A String object that is the username to check.
	 * @param connection The SQL connection to use if not null.
	 * @return True if the username is in the database.
	 */
	private boolean checkPlayerEntry(String username, Connection connection){
		if(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Username FROM Player WHERE Username = " + username, connection) == null)
			return false;
		return true;
	}
	
	/**
	 * Checks if an entry for the primary key pair username/itemID exists
	 * in the Item_info table on the database.
	 * @param username A String object that is the username to check.
	 * @param itemID An int that is the itemID to check.
	 * @param connection The SQL connection to use if not null.
	 * @return True if the username/itemID entry is in the database.
	 */
	private boolean checkItemEntryForPlayer(String username, int itemID, Connection connection){
		if(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Username FROM Item_Info WHERE Player = " + username
				+ " AND Item_ID = " + itemID, connection) == null)
			return false;
		return true;
	}
	
	private void setupPlayerEntries(EntityPlayer player, Connection connection){
		Calendar calObj = Calendar.getInstance();
		java.sql.Date dateObj = new java.sql.Date(calObj.getTimeInMillis());
		java.sql.Time timeObj = new java.sql.Time(calObj.getTimeInMillis());
		String strTime = dateObj.toString() + " " + timeObj.toString();
		
		DBServerMain.instance().sqlManager.updateQuery(
				"INSERT INTO Player VALUES ('" + player.username + "', '" + strTime + "', " +
				player.experienceLevel + ", " + player.getScore() + ")", connection);
		
		
		Integer[] dims = DimensionManager.getIDs();
		
		for(Integer dim : dims){
			DBServerMain.instance().sqlManager.updateQuery("INSERT INTO Time_Spent VALUES ('" + player.username +
					"', " + dim + ", 0)", connection);
		}
	}
	
	private void setupItems(Connection connection){
		boolean preserveConnection = false;
		Connection con = null;
		if(connection == null)
			con = DBServerMain.instance().sqlManager.getConnection();
		else
		{
			preserveConnection = true;
			con = connection;
		}
		
		if(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT ID FROM Items WHERE ID = 1", con) == null)
		{
			for(Item item : Item.itemsList){
				if(item != null)
				{
					DBServerMain.instance().sqlManager.updateQuery(
							"INSERT INTO Items VALUES (" + item.itemID + ", \"" + item.getItemDisplayName(new ItemStack(item)) + "\")", con);
				}
			}
		}
		
		if(!preserveConnection && con != null)
			try {
				con.close();
			} catch (SQLException e) {}
	}
}