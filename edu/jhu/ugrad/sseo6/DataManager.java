package edu.jhu.ugrad.sseo6;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import edu.jhu.ugrad.sseo6.util.Pair;
import edu.jhu.ugrad.sseo6.util.Utility;

public class DataManager {
	
	private boolean initialized = false;
	
	private HashMap<String, Pair<Integer, Long> > timeAtLogin;
	private HashMap<Pair<String, Integer>, Integer[] > itemInfo;
	
	public DataManager(){
		timeAtLogin = new HashMap<String, Pair<Integer, Long> >();
		itemInfo = new HashMap<Pair<String, Integer>, Integer[] >();
	}
	
	public boolean initialize(Connection con){
		if(!checkInitTables(con))
		{
			initialized = false;
			return false;
		}
		//setupItems(con);
		
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
		if(!initialized)
			return;
		
		new Thread(new playerLoggedInRunnable(player)).run();
	}
	
	private class playerLoggedInRunnable implements Runnable{
		EntityPlayer player = null;
		
		public playerLoggedInRunnable(EntityPlayer player)
		{
			this.player = player;
		}
		
		@Override
		public void run() {
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
			
			synchronized(timeAtLogin){
				timeAtLogin.put(player.username, new Pair<Integer, Long>(player.dimension, Calendar.getInstance().getTimeInMillis()));
			}
		}
	}
	
	public void playerLoggedOut(EntityPlayer player){
		if(!initialized)
			return;
		
		new Thread(new playerLoggedOutRunnable(player)).run();
	}
	
	private class playerLoggedOutRunnable implements Runnable {
		EntityPlayer player = null;
		
		public playerLoggedOutRunnable(EntityPlayer player) {
			this.player = player;
		}
		
		@Override
		public void run() {
			int expLevel = player.experienceLevel;
			int score = player.getScore();
			Pair<Integer, Long> prev;
			synchronized(timeAtLogin){
				prev = timeAtLogin.remove(player.username);
			}
			
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			
			DBServerMain.instance().sqlManager.updateQuery(
					"UPDATE Player SET Exp_Level = " + expLevel +
					", Score = " + score + " WHERE Username = '" + player.username + "'", con);
			
			int timeSpent = (int)((Calendar.getInstance().getTimeInMillis() - prev.b) / 1000);
			timeSpent += Integer.parseInt(DBServerMain.instance().sqlManager.standardQuery(
					"SELECT Time FROM Time_Spent WHERE Player = '" + player.username + "'", con));
			DBServerMain.instance().sqlManager.updateQuery(
					"UPDATE Time_Spent SET Time = " + timeSpent +
					" WHERE Player = '" + player.username + "' AND Dimension_ID = " + player.dimension, con);
			
			try {
				con.close();
			} catch (SQLException e) {}
			
		}
	}

	public void playerChangedDimension(EntityPlayer player) {
		if(!initialized)
			return;
		
		new Thread(new playerChangedDimensionRunnable(player)).run();
	}
	
	private class playerChangedDimensionRunnable implements Runnable{
		EntityPlayer player;
		
		public playerChangedDimensionRunnable(EntityPlayer player) {
			this.player = player;
		}
		
		@Override
		public void run() {
			Pair<Integer, Long> prev;
			synchronized(timeAtLogin){
				prev = timeAtLogin.remove(player.username);
			}
			int timeSpent = (int)((Calendar.getInstance().getTimeInMillis() - prev.b) / 1000);
			
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			try {
			timeSpent += Integer.parseInt(DBServerMain.instance().sqlManager.standardQuery(
					"SELECT Time FROM Time_Spent WHERE Player = '" + player.username + "'", con));
			DBServerMain.instance().sqlManager.updateQuery(
					"UPDATE Time_Spent SET Time = " + timeSpent +
					" WHERE Player = '" + player.username + "' AND Dimension_ID = " + player.dimension, con);
			} catch (NullPointerException e) {
				e.printStackTrace();
				System.out.println("  Was the database incorrectly initialized?");
			}
			try {
				con.close();
			} catch (SQLException e) {}
			
			synchronized(timeAtLogin){
				timeAtLogin.put(player.username, new Pair<Integer, Long>(player.dimension, Calendar.getInstance().getTimeInMillis()));
			}
		}
	}
	
	@ForgeSubscribe
	public void playerDeathEvent(LivingDeathEvent event){
		if(!initialized)
			return;
		
		if(!((event.source.getSourceOfDamage() != null && event.source.getSourceOfDamage() instanceof EntityPlayer) || (event.entityLiving instanceof EntityPlayer))
				|| event.entityLiving.worldObj.isRemote)
			return;
		
		String deadEName;
		String killerEName;
		
		deadEName = event.entityLiving.getEntityName();
		
		if(event.source.getSourceOfDamage() == null)
			killerEName = event.source.damageType;
		else
			killerEName = event.source.getSourceOfDamage().getEntityName();

		Calendar calObj = Calendar.getInstance();
		java.sql.Date dateObj = new java.sql.Date(calObj.getTimeInMillis());
		java.sql.Time timeObj = new java.sql.Time(calObj.getTimeInMillis());
		String strTime = dateObj.toString() + " " + timeObj.toString();
		
		//Kills
		if(event.source.getSourceOfDamage() != null && event.source.getSourceOfDamage() instanceof EntityPlayer){
			boolean killedIsPlayer = false;
			if(event.entityLiving instanceof EntityPlayer)
				killedIsPlayer = true;
			
			Integer itemID = null;
			if(((EntityPlayer)event.source.getSourceOfDamage()).getHeldItem() != null)
				itemID = ((EntityPlayer)event.source.getSourceOfDamage()).getHeldItem().itemID;
			
			new Thread(new playerKillRunnable(killerEName, deadEName, killedIsPlayer, itemID, strTime)).run();
		}
		
		//Deaths
		if(event.entityLiving instanceof EntityPlayer){
			boolean killerIsPlayer = false;
			if(event.source.getSourceOfDamage() != null && event.source.getSourceOfDamage() instanceof EntityPlayer)
				killerIsPlayer = true;
			
			Integer itemID = null;
			if(event.source.getSourceOfDamage() != null && ((EntityLiving)event.source.getSourceOfDamage()).getHeldItem() != null)
				itemID = ((EntityLiving)event.source.getSourceOfDamage()).getHeldItem().itemID;
			
			new Thread(new playerDeathRunnable(deadEName, killerEName, killerIsPlayer, itemID, strTime)).run();
		}
	}
	
	private class playerKillRunnable implements Runnable {
		String username;
		String entityName;
		boolean isPlayer;
		Integer itemID;
		String time;
		
		public playerKillRunnable(String username, String entityName, boolean isPlayer, Integer itemID, String time){
			this.username = username;
			this.entityName = entityName;
			this.isPlayer = isPlayer;
			this.itemID = itemID;
			this.time = time;
		}
		
		@Override
		public void run() {
			if(itemID != null)
				DBServerMain.instance().sqlManager.updateQuery(
						"INSERT INTO Kills (Player, Entity_Name, Is_Player, Item_ID, Time) VALUES ('" + username + "', '" + entityName + "', " +
						isPlayer + ", " + itemID + ", '" + time + "')", null);
			else
				DBServerMain.instance().sqlManager.updateQuery(
						"INSERT INTO Kills (Player, Entity_Name, Is_Player, Time) VALUES ('" + username + "', '" + entityName + "', " +
						isPlayer + ", '" + time + "')", null);
		}
	}
	
	private class playerDeathRunnable implements Runnable {
		String username;
		String entityName;
		boolean isPlayer;
		Integer itemID;
		String time;
		
		public playerDeathRunnable(String username, String entityName, boolean isPlayer, Integer itemID, String time){
			this.username = username;
			this.entityName = entityName;
			this.isPlayer = isPlayer;
			this.itemID = itemID;
			this.time = time;
		}
		
		@Override
		public void run() {
			if(itemID != null)
				DBServerMain.instance().sqlManager.updateQuery(
						"INSERT INTO Deaths (Player, Entity_Name, Is_Player, Item_ID, Time) VALUES ('" + username + "', '" + entityName + "', " +
						isPlayer + ", " + itemID + ", '" + time + "')", null);
			else
				DBServerMain.instance().sqlManager.updateQuery(
						"INSERT INTO Deaths (Player, Entity_Name, Is_Player, Time) VALUES ('" + username + "', '" + entityName + "', " +
						isPlayer + ", '" + time + "')", null);
		}
	}
	
	@ForgeSubscribe
	public void playerChatEvent(ServerChatEvent event){
		if(!initialized)
			return;
		
		if(event.player.worldObj.isRemote)
			return;
		
		new Thread(new playerChatEventRunnable(event)).run();
	}
	
	private class playerChatEventRunnable implements Runnable{
		ServerChatEvent event;
		
		public playerChatEventRunnable(ServerChatEvent event){
			this.event = event;
		}
		
		@Override
		public void run() {
			Calendar calObj = Calendar.getInstance();
			java.sql.Date dateObj = new java.sql.Date(calObj.getTimeInMillis());
			java.sql.Time timeObj = new java.sql.Time(calObj.getTimeInMillis());
			String strTime = dateObj.toString() + " " + timeObj.toString();
			
			DBServerMain.instance().sqlManager.updateQuery(
					"INSERT INTO Chat_Log (Time, Player, Message) VALUES ('" + strTime +
					"', '" + event.username + "', \"" + Utility.escapeSQLQuery(event.message) + "\")", null);
		}
	}
	
	@ForgeSubscribe
	public void playerPickupEvent(EntityItemPickupEvent event){
		if(!initialized)
			return;
		
		if(event.entityPlayer.worldObj.isRemote)
			return;

		updateItemEntryLocal(event.entityPlayer.username, event.item.getEntityItem().itemID, 0, event.item.getEntityItem().stackSize);
	}
	
	@ForgeSubscribe
	public void playerItemBreakEvent(PlayerDestroyItemEvent event){
		if(!initialized)
			return;
		
		if(event.entityPlayer.worldObj.isRemote)
			return;
		
		updateItemEntryLocal(event.entityPlayer.username, event.original.itemID, 1, 1);
	}
	
	@ForgeSubscribe
	public void playerInteractEvent(PlayerInteractEvent event){
		if(!initialized)
			return;
		
		if(event.entityPlayer.worldObj.isRemote)
			return;
		
		if(event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK ||
				event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
		{
			if(event.entityPlayer.getHeldItem() != null)
				updateItemEntryLocal(event.entityPlayer.username, event.entityPlayer.getHeldItem().itemID, 2, 1);
		}
	}
	
	@ForgeSubscribe
	public void playerAttackEvent(AttackEntityEvent event){
		if(!initialized)
			return;
		
		if(event.entityPlayer.worldObj.isRemote)
			return;
		
		if(event.entityPlayer.getHeldItem() != null)
			updateItemEntryLocal(event.entityPlayer.username, event.entityPlayer.getHeldItem().itemID, 2, 1);
	}
	
	public void playerCraftedItem(EntityPlayer player, ItemStack item) {
		if(!initialized)
			return;
		
		updateItemEntryLocal(player.username, item.itemID, 3, item.stackSize);
	}
	
	public void serverShuttingDown(){
		if(!initialized)
			return;
		String username;
		int itemID;
		int collected;
		int broken;
		int used;
		int created;
		Iterator<Entry<Pair<String,Integer>, Integer[] > > iiter = itemInfo.entrySet().iterator();
		Connection con = DBServerMain.instance().sqlManager.getConnection();
		while(iiter.hasNext())
		{
			Entry<Pair<String,Integer>, Integer[] > entry = iiter.next();
			
			username = entry.getKey().a;
			itemID = entry.getKey().b;
			collected = entry.getValue()[0];
			broken = entry.getValue()[1];
			used = entry.getValue()[2];
			created = entry.getValue()[3];
			
			checkItemEntryForPlayer(username, itemID, con);
			
			Collection<String> res = DBServerMain.instance().sqlManager.standardQueryRow(
					"SELECT Collected, Broken, Used, Created FROM Item_Info WHERE Player = '" + username +
					"' AND Item_ID = " + itemID, con, 4);
			
			String[] resA = res.toArray(new String[4]);
			collected += Integer.parseInt(resA[0]);
			broken += Integer.parseInt(resA[1]);
			used += Integer.parseInt(resA[2]);
			created += Integer.parseInt(resA[3]);
			
			DBServerMain.instance().sqlManager.updateQuery(
					"UPDATE Item_Info SET Collected = " + collected + ", Broken = " + broken + ", Used = " + used +
					", Created = " + created + " WHERE Player = '" + username + "' AND Item_ID = " + itemID, con);
		}
		
		try {
			con.close();
		} catch (SQLException e) {}
	}
	
	/**
	 * Checks if a username is in the database.
	 * @param username A String object that is the username to check.
	 * @param connection The SQL connection to use if not null.
	 * @return True if the username is in the database.
	 */
	private boolean checkPlayerEntry(String username, Connection connection){
		if(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Username FROM Player WHERE Username = '" + username + "'", connection) == null)
			return false;
		return true;
	}
	
	/**
	 * Checks if an entry for the primary key pair username/itemID exists
	 * in the Item_info table on the database.
	 * @param username A String object that is the username to check.
	 * @param itemID An int that is the itemID to check.
	 * @param connection The SQL connection to use if not null.
	 */
	private void checkItemEntryForPlayer(String username, int itemID, Connection connection){
		if(DBServerMain.instance().sqlManager.standardQuery(
				"SELECT Player FROM Item_Info WHERE Player = '" + username
				+ "' AND Item_ID = " + itemID, connection) == null){
			DBServerMain.instance().sqlManager.updateQuery(
					"INSERT INTO Item_Info VALUES ('" + username + "', " + itemID + ", 0, 0, 0, 0)", connection);
		}
	}
	
	/**
	 * Checks if an entry for the primary key pair username/itemID exists
	 * locally for later storage into the database.
	 * @param username A String object that is the username to check.
	 * @param itemID An int that is the itemID to check.
	 * @return The username/itemID pair.
	 */
	private Pair<String, Integer> checkItemEntryForPlayerLocal(String username, int itemID){
		Pair<String, Integer> pair = new Pair<String,Integer>(username,itemID);
		
		synchronized(itemInfo){
			if(itemInfo.containsKey(pair))
				return pair;
		
			Integer[] ints = {0, 0, 0, 0};
			
			itemInfo.put(pair, ints);
		}
		return pair;
	}
	
	/**
	 * Updates itemInfo with the specified amount to increment using
	 * the username/itemID key pair.
	 * @param username The username of the player.
	 * @param itemID The itemID of the item.
	 * @param index The index of the array to edit.
	 * @param amount The amount to increment with.
	 */
	private void updateItemEntryLocal(String username, int itemID, int index, int amount){
		new Thread(new updateItemEntryLocalRunnable(username,itemID,index,amount)).run();
	}
	
	private class updateItemEntryLocalRunnable implements Runnable{
		String username;
		int itemID;
		int index;
		int amount;
		
		public updateItemEntryLocalRunnable(String username, int itemID, int index, int amount){
			this.username = username;
			this.itemID = itemID;
			this.index = index;
			this.amount = amount;
		}

		@Override
		public void run() {
			Pair<String, Integer> pair = checkItemEntryForPlayerLocal(username, itemID);
			
			synchronized(itemInfo){
				itemInfo.get(pair)[index] += amount;
			}
		}
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
	
	//Only works client-side, crashes in server environment.
	@Deprecated
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