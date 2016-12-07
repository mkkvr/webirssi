package com.mavi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.IRCUtil;

public class CustomIrcEventListener implements IRCEventListener {

	private List<IRCMessage> messages;
	private List<String> nicks;

	private String nick;

	public CustomIrcEventListener(String nick) {
		messages = new ArrayList<IRCMessage>();
		nicks = new ArrayList<String>();
		this.nick = nick;
	}

	/**
	 * Own methods
	 */
	public void addMessage(String chan, String user, String msg) {
		boolean personal = nick.equalsIgnoreCase(chan);
		messages.add(new IRCMessage(user, msg, new Date(), personal));
	}

	public List<IRCMessage> getMessages() {
		return messages;
	}

	public List<String> getNicsk() {
		return nicks;
	}

	/**
	 * Interface
	 */

	public void print(String msg) {
		System.out.println(msg);
	}

	public void onRegistered() {
		print("Connected");
	}

	public void onDisconnected() {
		print("Disconnected");
	}

	public void onError(String msg) {
		print("Error: " + msg);
	}

	public void onError(int num, String msg) {
		print("Error #" + num + ": " + msg);
	}

	public void onInvite(String chan, IRCUser u, String nickPass) {
		print(chan + "> " + u.getNick() + " invites " + nickPass);
	}

	public void onJoin(String chan, IRCUser u) {
		print(chan + "> " + u.getNick() + " joins");
	}

	public void onKick(String chan, IRCUser u, String nickPass, String msg) {
		print(chan + "> " + u.getNick() + " kicks " + nickPass);
	}

	public void onMode(IRCUser u, String nickPass, String mode) {
		print("Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass);
	}

	public void onMode(String chan, IRCUser u, IRCModeParser mp) {
		print(chan + "> " + u.getNick() + " sets mode: " + mp.getLine());
	}

	public void onNick(IRCUser u, String nickNew) {
		print("Nick: " + u.getNick() + " is now known as " + nickNew);
	}

	public void onNotice(String target, IRCUser u, String msg) {
		print(target + "> " + u.getNick() + " (notice): " + msg);
	}

	public void onPart(String chan, IRCUser u, String msg) {
		print(chan + "> " + u.getNick() + " parts");
	}

	/**
	 * message mavibotti test1 <--> message #mavtest test2
	 */

	public void onPrivmsg(String chan, IRCUser u, String msg) {
		addMessage(chan, u.getNick(), msg);
	}

	public void onQuit(IRCUser u, String msg) {
		print("Quit: " + u.getNick());
	}

	public void onReply(int num, String value, String msg) {
		if (num == IRCUtil.RPL_NAMREPLY) {
			StringTokenizer tokenizer = new StringTokenizer(msg, " ");
			nicks = new ArrayList<String>();
			while (tokenizer.hasMoreElements()) {
				String nick = tokenizer.nextToken();
				nicks.add(nick);
			}
		}
		print("Reply #" + num + ": " + value + " " + msg);
	}

	public void onTopic(String chan, IRCUser u, String topic) {
		print(chan + "> " + u.getNick() + " changes topic into: " + topic);
	}

	public void onPing(String p) {

	}

	public void unknown(String a, String b, String c, String d) {
		print("UNKNOWN: " + a + " b " + c + " " + d);
	}

}
