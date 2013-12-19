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
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("Invalid command use."));
		
		String query = "";
		String query2 = "";
		String name = icommandsender.getCommandSenderName();
		String result = "";
		Collection<String> result2 = null;
		
		if(astring[0].equals("deaths")) {
			query = "SELECT count(*) AS Total FROM Deaths WHERE Player = '" + name + "'";
			query2 = "SELECT Entity_Name, count(*) AS Deaths FROM Deaths"
					+ " WHERE Player = '" + name + "' GROUP BY Entity_Name DESC";
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
			
		}
		else if(astring[0].equals("item")) {
			
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
