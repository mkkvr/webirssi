package com.mavi;

import java.io.Serializable;
import java.util.Date;

public class IRCMessage implements Serializable {

	private String nick;
	private String msg;
	private Date date;
	private boolean personal;

	public IRCMessage(String nick, String msg, Date date, boolean personal) {
		this.nick = nick;
		this.msg = msg;
		this.date = date;
		this.personal = personal;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isPersonal() {
		return personal;
	}

	public void setPersonal(boolean personal) {
		this.personal = personal;
	}

}
