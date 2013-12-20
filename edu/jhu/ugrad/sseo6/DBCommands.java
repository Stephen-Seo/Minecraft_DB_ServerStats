package edu.jhu.ugrad.sseo6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;

public class DBCommands implements ICommand {
	
	private List aliases;
	
	public DBCommands() {
		aliases = new ArrayList(0);
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "DBSI";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/DBSI <deaths/kills/item/rank/time>";
	}

	@Override
	public List getCommandAliases() {
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if(astring.length == 0 || astring.length > 1)
		{
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid command use."));
			return;
		}
		
		String name = icommandsender.getCommandSenderName();
		
		if(astring[0].equals("deaths")) {
			String query = "SELECT count(*) AS Total FROM Deaths WHERE Player = '" + name + "'";
			String query2 = "SELECT Entity_Name, count(*) AS Deaths FROM Deaths"
					+ " WHERE Player = '" + name + "' GROUP BY Entity_Name ORDER BY Deaths DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			String result = DBServerMain.instance().sqlManager.standardQuery(query, con);
			Collection<String> result2 = null;
			if(!result.equals("0"))
				result2 = DBServerMain.instance().sqlManager.standardQueryRow(query2, con, 2);
			try {
				con.close();
			} catch (SQLException e) {}
			if(result.equals("0"))
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You died "
						+ result + " times."));
			else {
				String[] res = result2.toArray(new String[2]);
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You died "
						+ result + " times. You were killed the most by " + res[0] + " " + res[1] + " times."));
			}
		}
		else if(astring[0].equals("kills")) {
			String query = "SELECT count(*) AS Total FROM Kills WHERE Player = '" + name + "'";
			String query2 = "SELECT Entity_Name, count(*) AS Kills FROM Kills"
					+ " WHERE Player = '" + name + "' GROUP BY Entity_Name ORDER BY Kills DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			String result = DBServerMain.instance().sqlManager.standardQuery(query, con);
			Collection<String> result2 = null;
			if(!result.equals("0"))
				result2 = DBServerMain.instance().sqlManager.standardQueryRow(query2, con, 2);
			try {
				con.close();
			} catch (SQLException e) {}
			if(result.equals("0"))
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You killed "
						+ result + " times."));
			else {
				String[] res = result2.toArray(new String[2]);
				icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You killed "
						+ result + " times. You killed mostly " + res[0] + " " + res[1] + " times."));
			}
		}
		else if(astring[0].equals("item")) {
			String query = "SELECT I.Name, II.Collected FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Collected DESC";
			String query2 = "SELECT I.Name, II.Broken FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Broken DESC";
			String query3 = "SELECT I.Name, II.Used FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Used DESC";
			String query4 = "SELECT I.Name, II.Created FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Created DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			Collection<String> result = DBServerMain.instance().sqlManager.standardQueryRow(query, con, 2);
			Collection<String> result2 = DBServerMain.instance().sqlManager.standardQueryRow(query2, con, 2);
			Collection<String> result3 = DBServerMain.instance().sqlManager.standardQueryRow(query3, con, 2);
			Collection<String> result4 = DBServerMain.instance().sqlManager.standardQueryRow(query4, con, 2);
			try {
				con.close();
			} catch (SQLException e) {}
			String[] res = result.toArray(new String[2]);
			String[] res2 = result2.toArray(new String[2]);
			String[] res3 = result3.toArray(new String[2]);
			String[] res4 = result4.toArray(new String[2]);
			
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You collected "
					+ "mostly " + res[0] + " " + res[1] + " times."));
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You broke "
					+ "mostly " + res2[0] + " " + res2[1] + " times."));
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You used "
					+ "mostly " + res3[0] + " " + res3[1] + " times."));
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You created "
					+ "mostly " + res4[0] + " " + res4[1] + " times."));
		}
		else if(astring[0].equals("rank")) {
			String query = "SELECT Rank, Score FROM Ranks, Player WHERE Player = '" + name
					+ "' AND Player = Username";
			Collection<String> result = DBServerMain.instance().sqlManager.standardQueryRow(query, null, 2);
			String[] res = result.toArray(new String[2]);
			
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Your rank is "
					+ res[0] + " with Minecraft score " + res[1] + "."));
		}
		else if(astring[0].equals("time")) {
			String query = "SELECT SUM(Time) AS Total FROM Time_Spent WHERE Player = '" + name + "'";
			String query2 = "SELECT Dimension_ID, Time FROM Time_Spent WHERE Player = '" + name + "' AND Time >= ALL "
					+ "(SELECT Time FROM Time_Spent WHERE Player = '" + name + "')";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			String result = DBServerMain.instance().sqlManager.standardQuery(query, con);
			Collection<String> result2 = DBServerMain.instance().sqlManager.standardQueryRow(query2, con, 2);
			try {
				con.close();
			} catch (SQLException e) {}
			String[] res2 = result2.toArray(new String[2]);
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You spent "
					+ result + " seconds in the game."));
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("You spent "
					+ "the most time in dimension " + res2[0] + " with " + res2[1] + " seconds."));
		}
		else
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid command use."));
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
		if(icommandsender instanceof EntityPlayer)
			return true;
		return false;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender icommandsender,
			String[] astring) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}

}
