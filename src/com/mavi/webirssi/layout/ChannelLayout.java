package com.mavi.webirssi.layout;

import java.util.List;

import com.mavi.IRCConnectionManager;
import com.mavi.IRCMessage;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ChannelLayout extends VerticalLayout implements TabSheetLayout {

	private RichTextArea area;
	private TextArea nickList;
	private TextField tf;

	private IRCConnectionManager connectionManager;

	private String channel;

	public ChannelLayout(IRCConnectionManager connectionManager, String channel) {
		this.connectionManager = connectionManager;
		buildLayout();
		this.channel = channel;
	}

	private void buildLayout() {
		setSizeFull();

		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();

		area = new RichTextArea();
		area.addStyleName("no-toolbar-top");
		area.addStyleName("no-toolbar-bottom");
		// area.setImmediate(true);
		area.setSizeFull();
		mainLayout.addComponent(area);
		mainLayout.setExpandRatio(area, 1.0f);

		// TODO:
		// Update this
		// Make uneditable
		nickList = new TextArea();
		// nickList.setImmediate(true);
		nickList.setHeight("100%");
		nickList.setEnabled(false);
		mainLayout.addComponent(nickList);

		HorizontalLayout submitLayout = new HorizontalLayout();
		submitLayout.setWidth("100%");

		tf = new TextField();
		tf.setSizeFull();
		submitLayout.addComponent(tf);
		submitLayout.setExpandRatio(tf, 1.0f);

		final EnterListener enterListener = new EnterListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null);

		tf.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				tf.addShortcutListener(enterListener);

			}
		});

		tf.addBlurListener(new BlurListener() {

			@Override
			public void blur(BlurEvent event) {
				tf.removeShortcutListener(enterListener);

			}
		});

		Button button = new Button("Send");
		button.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				String msg = tf.getValue();
				sendMessage(msg);
			}
		});
		submitLayout.addComponent(button);

		addComponent(mainLayout);
		addComponent(submitLayout);

		setExpandRatio(mainLayout, 1.0f);

	}

	private class EnterListener extends ShortcutListener {

		public EnterListener(String caption, int keyCode, int... modifierKeys) {
			super(caption, keyCode, modifierKeys);
		}

		@Override
		public void handleAction(Object sender, Object target) {
			String msg = tf.getValue();
			sendMessage(msg);
		}

	}

	private void sendMessage(String msg) {
		if (msg != null) {
			connectionManager.doMessage(msg);
			tf.setValue("");
		}
	}

	private String getNick() {
		return connectionManager.getNick();
	}

	public void refreshContent() {
		String content = "";
		List<IRCMessage> messages = connectionManager.getMessages(channel);

		// TODO: parse links
		// http://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string

		String nick = getNick();

		for (IRCMessage message : messages) {

			boolean personal = message.isPersonal();
			boolean bold = message.getMsg().contains(nick);

			int minutes = message.getDate().getMinutes();
			String minutesString = (minutes < 10) ? "0" + minutes : "" + minutes;

			int hours = message.getDate().getHours();
			String hoursString = (hours < 10) ? "0" + hours : "" + hours;

			String line = "[" + hoursString + ":" + minutesString + "] [" + message.getNick() + "]";
			if (personal)
				line += " --> [" + nick + "]";
			else
				line += ":";
			line += message.getMsg();

			if (bold) {
				content = content + "<b>" + line + "</b><br>";
			} else {
				content = content + line + "<br>";
			}
		}
		area.setValue(content);

		List<String> nicks = connectionManager.getNicks();
		String nickContent = "";
		for (String listNick : nicks) {
			nickContent += listNick + "\n";
		}
		nickList.setValue(nickContent);

	}

}
