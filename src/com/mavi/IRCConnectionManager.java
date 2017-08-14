package com.mavi;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.schwering.irc.lib.IRCConnection;

import com.vaadin.server.VaadinServlet;

public class IRCConnectionManager {

	private String host = "irc.saunalahti.fi";
	// ircnet.nerim.fr
	private int port = 6667;
	private String pass = "";
	private String nick = "mavibot";
	private String user = "user MV";
	private String name = "name MV";
	private String channel = "#mavtest";

	private IRCConnection connection;
	private CustomIrcEventListener listener;

	private WebirssiUI ui;

	public IRCConnectionManager(WebirssiUI ui) {
		this.ui = ui;
		readProperties();
	}

	public IRCConnection getConnection() {
		if (connection == null) {
			createConnection();
		}
		return connection;
	}

	private void readProperties() {
		try {
			InputStream inputStream = VaadinServlet.getCurrent().getServletContext()
					.getResourceAsStream("/WEB-INF/irc.properties");
			Properties properties = new Properties();
			properties.load(inputStream);
			host = properties.getProperty("host");
			port = Integer.parseInt(properties.getProperty("port"));
			pass = properties.getProperty("pass");
			nick = properties.getProperty("nick");
			user = properties.getProperty("user");
			name = properties.getProperty("name");
			channel = properties.getProperty("channel");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createConnection() {
		listener = new CustomIrcEventListener(ui, nick);

		connection = new IRCConnection(host, new int[] { port }, pass, nick, user, name);
		connection.addIRCEventListener(listener);
		connection.setEncoding("UTF-8");
		connection.setPong(true);
		connection.setDaemon(false);
		connection.setColors(false);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
		// TODO: set to connection
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
		// TODO: set to connection
	}

	public List<IRCMessage> getMessages(String chan) {
		return listener.getMessages(chan);
	}

	public List<String> getNicks() {
		return listener.getNicks();
	}

	public List<String> getInfos() {
		return listener.getInfos();
	}

	public void doMessage(String message) {
		connection.doPrivmsg(channel, message);
		listener.addMessage(channel, nick, message);
	}

	public boolean isReady() {
		return connection != null && listener != null;
	}
}
