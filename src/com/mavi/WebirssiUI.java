package com.mavi;

import java.io.IOException;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.schwering.irc.lib.IRCConnection;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.event.UIEvents;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("webirssi")
public class WebirssiUI extends UI {

	private RichTextArea area;
	private TextArea nickList;
	private TextField tf;

	private IRCConnectionManager connectionManager;

	private final boolean connect = true;

	private String nick;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = WebirssiUI.class)
	public static class Servlet extends VaadinServlet implements
			SessionDestroyListener {

		// http://moepii.sourceforge.net/irclib/Client.java

		public Servlet() {

		}

		public void init() throws javax.servlet.ServletException {
		}

		@Override
		public void sessionDestroy(SessionDestroyEvent event) {
			// TODO: refactor to close straight without getting manager
			// ((IRCConnectionManager) getServletContext().getAttribute(
			// "connectionManager")).getConnection().close();
			((IRCConnection) getServletContext().getAttribute("conn")).close();
		}

	}

	private void initIRC() {

		Object cm = VaadinServlet.getCurrent().getServletContext()
				.getAttribute("connectionManager");
		if (cm == null) {
			connectionManager = new IRCConnectionManager();
			VaadinServlet.getCurrent().getServletContext()
					.setAttribute("connectionManager", connectionManager);
		} else {
			connectionManager = (IRCConnectionManager) cm;
		}

	}

	public void print(String msg) {
		System.out.println(msg);
	}

	private void refreshContent() {
		String content = "";
		List<IRCMessage> messages = connectionManager.getMessages();

		// TODO: parse links
		// http://stackoverflow.com/questions/5713558/detect-and-extract-url-from-a-string

		for (IRCMessage message : messages) {

			boolean personal = message.isPersonal();
			boolean bold = message.getMsg().contains(nick);

			int minutes = message.getDate().getMinutes();
			String minutesString = (minutes < 10) ? "0" + minutes : ""
					+ minutes;

			int hours = message.getDate().getHours();
			String hoursString = (hours < 10) ? "0" + hours : "" + hours;

			String line = "[" + hoursString + ":" + minutesString + "] ["
					+ message.getNick() + "]";
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
		for (String nick : nicks) {
			nickContent += nick + "\n";
		}
		nickList.setValue(nickContent);

	}

	private void buildLayout() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		setContent(layout);

		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();

		area = new RichTextArea();
		area.addStyleName("no-toolbar-top");
		area.addStyleName("no-toolbar-bottom");
		area.setImmediate(true);
		area.setSizeFull();
		mainLayout.addComponent(area);
		mainLayout.setExpandRatio(area, 1.0f);

		nickList = new TextArea();
		nickList.setImmediate(true);
		nickList.setHeight("100%");
		mainLayout.addComponent(nickList);

		layout.addComponent(buildMenuBar());
		layout.addComponent(mainLayout);

		HorizontalLayout submitLayout = new HorizontalLayout();
		submitLayout.setWidth("100%");

		tf = new TextField();
		tf.setSizeFull();
		submitLayout.addComponent(tf);
		submitLayout.setExpandRatio(tf, 1.0f);

		final EnterListener enterListener = new EnterListener("Shortcut Name",
				ShortcutAction.KeyCode.ENTER, null);

		// tf.setImmediate(true);

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

		layout.addComponent(submitLayout);
		layout.setExpandRatio(mainLayout, 1.0f);

		HorizontalLayout statusBar = new HorizontalLayout();
		statusBar.setWidth("100%");
		Label statusLabel = new Label("Status");
		statusBar.addComponent(statusLabel);
		layout.addComponent(statusBar);

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

	@Override
	protected void init(VaadinRequest request) {

		setPollInterval(1000);
		addPollListener(new UIEvents.PollListener() {
			@Override
			public void poll(UIEvents.PollEvent event) {
				if (connect && connectionManager != null
						&& connectionManager.isReady()) {
					refreshContent();
				}
			}
		});

		// https://vaadin.com/book/-/page/advanced.global.html
		// https://vaadin.com/wiki/-/wiki/Main/Setting+and+reading+session+attributes
		// http://stackoverflow.com/questions/3106452/how-do-servlets-work-instantiation-session-variables-and-multithreading/3106909#3106909

		if (connect) {
			initIRC();
		}
		buildLayout();

	}

	private MenuBar buildMenuBar() {

		MenuBar menuBar = new MenuBar();
		menuBar.setWidth("100%");

		MenuItem connection = menuBar.addItem("Connection", null, null);

		connection.addItem("Connect", null, new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				showConnectionDialog();
			}
		});

		connection.addItem("Disconnect", null, new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				((IRCConnection) VaadinServlet.getCurrent().getServletContext()
						.getAttribute("conn")).close();
			}
		});

		MenuItem about = menuBar.addItem("About", null, new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				showAboutDialog();
			}
		});

		return menuBar;
	}

	private void showAboutDialog() {
		Window subWindow = new Window("About");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		subContent.addComponent(new Label("webirssi 0.1"));
		subWindow.setContent(subContent);
		subWindow.center();
		subWindow.setResizable(false);
		addWindow(subWindow);
	}

	private void showConnectionDialog() {
		MySub subWindow = new MySub("Connect");
		subWindow.center();
		addWindow(subWindow);
	}

	private class MySub extends Window {

		public MySub(String text) {
			VerticalLayout subContent = new VerticalLayout();
			subContent.setMargin(true);
			setContent(subContent);

			final TextField nickField = new TextField("Nick");
			final TextField channelField = new TextField("Channel");
			subContent.addComponent(nickField);
			subContent.addComponent(channelField);
			IRCConnectionManager cm = (IRCConnectionManager) VaadinServlet
					.getCurrent().getServletContext()
					.getAttribute("connectionManager");
			if (cm != null) {
				nickField.setValue(cm.getNick());
				channelField.setValue(cm.getChannel());
			}

			Button connectButton = new Button("Connect");
			Button cancelButton = new Button("Calcel");
			subContent.addComponent(connectButton);
			subContent.addComponent(cancelButton);

			connectButton.addClickListener(new ClickListener() {

				// TODO: if already connected!

				@Override
				public void buttonClick(ClickEvent event) {
					try {

						IRCConnectionManager cm = (IRCConnectionManager) VaadinServlet
								.getCurrent().getServletContext()
								.getAttribute("connectionManager");

						String nickValue = nickField.getValue();
						String channelValue = channelField.getValue();

						boolean validNick = nickValue != null
								&& nickValue.length() > 0;
						boolean validChannel = channelValue != null
								&& channelValue.length() > 0;

						if (validNick && validChannel) {
							cm.setNick(nickValue);
							nick = nickValue;
							IRCConnection connection = cm.getConnection();
							connection.connect();
							connection.doJoin(channelValue);
						}
						close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			});
		}
	}

}