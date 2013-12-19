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
		
		String query = "";
		String query2 = "";
		String query3 = "";
		String query4 = "";
		String name = icommandsender.getCommandSenderName();
		String result = "";
		Collection<String> result2 = null;
		Collection<String> result3 = null;
		Collection<String> result4 = null;
		Collection<String> result5 = null;
		
		if(astring[0].equals("deaths")) {
			query = "SELECT count(*) AS Total FROM Deaths WHERE Player = '" + name + "'";
			query2 = "SELECT Entity_Name, count(*) AS Deaths FROM Deaths"
					+ " WHERE Player = '" + name + "' GROUP BY Entity_Name ORDER BY Deaths DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			result = DBServerMain.instance().sqlManager.standardQuery(query, con);
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
			query = "SELECT count(*) AS Total FROM Kills WHERE Player = '" + name + "'";
			query2 = "SELECT Entity_Name, count(*) AS Kills FROM Kills"
					+ " WHERE Player = '" + name + "' GROUP BY Entity_Name ORDER BY Kills DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			result = DBServerMain.instance().sqlManager.standardQuery(query, con);
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
			query = "SELECT I.Name, II.Collected FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Collected DESC";
			query2 = "SELECT I.Name, II.Broken FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Broken DESC";
			query3 = "SELECT I.Name, II.Used FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Used DESC";
			query4 = "SELECT I.Name, II.Created FROM Items I, Item_Info II WHERE"
					+ " II.Player = '" + name + "' AND I.ID = II.Item_ID ORDER BY II.Created DESC";
			Connection con = DBServerMain.instance().sqlManager.getConnection();
			if(con == null)
				return;
			result2 = DBServerMain.instance().sqlManager.standardQueryRow(query, con, 2);
			result3 = DBServerMain.instance().sqlManager.standardQueryRow(query2, con, 2);
			result4 = DBServerMain.instance().sqlManager.standardQueryRow(query3, con, 2);
			result5 = DBServerMain.instance().sqlManager.standardQueryRow(query4, con, 2);
			try {
				con.close();
			} catch (SQLException e) {}
			String[] res = result2.toArray(new String[2]);
			String[] res2 = result3.toArray(new String[2]);
			String[] res3 = result4.toArray(new String[2]);
			String[] res4 = result5.toArray(new String[2]);
			
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
			
		}
		else if(astring[0].equals("time")) {
			
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
