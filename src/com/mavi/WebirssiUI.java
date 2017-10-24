package com.mavi;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;

import org.schwering.irc.lib.IRCConnection;
import org.vaadin.dialogs.ConfirmDialog;

import com.mavi.webirssi.layout.ChannelLayout;
import com.mavi.webirssi.layout.InfoLayout;
import com.mavi.webirssi.layout.TabSheetLayout;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.UIEvents;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
@Theme("webirssi")
public class WebirssiUI extends UI {

	private IRCConnectionManager connectionManager;

	private final boolean connect = true;

	private String nick;

	// temporary
	// ChannelLayout mainLayout;
	InfoLayout infoLayout;

	TabSheet tabSheet;

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = WebirssiUI.class)
	public static class Servlet extends VaadinServlet implements SessionDestroyListener {

		// http://moepii.sourceforge.net/irclib/Client.java

		public Servlet() {

		}

		public void init() throws javax.servlet.ServletException {
		}

		@Override
		public void sessionDestroy(SessionDestroyEvent event) {
			((IRCConnection) getServletContext().getAttribute("conn")).close();
		}

	}

	private void initIRC() {

		Object cm = VaadinServlet.getCurrent().getServletContext().getAttribute("connectionManager");
		if (cm == null) {
			connectionManager = new IRCConnectionManager(this);
			VaadinServlet.getCurrent().getServletContext().setAttribute("connectionManager", connectionManager);
		} else {
			connectionManager = (IRCConnectionManager) cm;
		}

	}

	public void print(String msg) {
		System.out.println(msg);
	}

	private void buildLayout() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		setContent(layout);

		layout.addComponent(buildMenuBar());
		layout.addComponent(buildToolBar());

		infoLayout = new InfoLayout(connectionManager);

		tabSheet = new TabSheet();
		tabSheet.addTab(infoLayout, "Info");
		tabSheet.setHeight("100%");

		tabSheet.setCloseHandler(new CloseHandler() {
			@Override
			public void onTabClose(TabSheet tabsheet, Component tabContent) {
				Tab tab = tabsheet.getTab(tabContent);
				// Notification.show("Closing " + tab.getCaption());

				// We need to close it explicitly in the handler
				// showConfirmDialog();
				handleConfirmDialog(tab);
				// TODO: leave channel on irc
			}
		});

		layout.addComponent(tabSheet);

		layout.setExpandRatio(tabSheet, 1.0f);

		HorizontalLayout statusBar = new HorizontalLayout();
		statusBar.setWidth("100%");
		Label statusLabel = new Label("Status");
		statusBar.addComponent(statusLabel);
		layout.addComponent(statusBar);

	}

	public void addChannel(String channel) {
		ChannelLayout channelLayout = new ChannelLayout(connectionManager, channel);
		tabSheet.addTab(channelLayout, channel);
		tabSheet.getTab(channelLayout).setClosable(true);
	}

	public void handleConfirmDialog(Tab tab) {
		String channel = tab.getCaption();
		ConfirmDialog.show(this, "Please Confirm:", "Leave " + channel + ". Are you really sure?", "I am", "Not quite",
				new ConfirmDialog.Listener() {

					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							// Confirmed to continue
							tabSheet.removeTab(tab);
							connectionManager.doPart(channel);
						}
					}
				});
	}

	public void addChannelTab(String channel) {
		ChannelLayout channelLayout = new ChannelLayout(connectionManager, channel);
		tabSheet.addTab(channelLayout, channel);

		// TODO: add close dialog
		// TODO: leave channel

	}

	@Override
	protected void init(VaadinRequest request) {

		// TODO: refresh only the window that is active

		setPollInterval(1000);
		addPollListener(new UIEvents.PollListener() {
			@Override
			public void poll(UIEvents.PollEvent event) {
				if (connect && connectionManager != null && connectionManager.isReady()) {
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

	private void refreshContent() {

		// TODO: check if this works?
		Component tabComponent = tabSheet.getSelectedTab();
		if (tabComponent != null && tabComponent instanceof TabSheetLayout) {
			((TabSheetLayout) tabComponent).refreshContent();
		}

	}

	private HorizontalLayout buildToolBar() {
		HorizontalLayout toolbar = new HorizontalLayout();
		Button connectButton = new Button("Connect");
		toolbar.addComponent(connectButton);
		Button addChannelButton = new Button("Add");
		toolbar.addComponent(addChannelButton);
		return toolbar;
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

				IRCConnectionManager cm = (IRCConnectionManager) VaadinServlet.getCurrent().getServletContext()
						.getAttribute("connectionManager");
				IRCConnection connection = cm.getConnection();
				connection.close();
				// TODO: add some info to all channels?
			}
		});

		MenuItem about = menuBar.addItem("About", null, new MenuBar.Command() {
			public void menuSelected(MenuItem selectedItem) {
				showAboutDialog();
			}
		});

		return menuBar;
	}

	// private void showConfirmDialog() {
	// Window subWindow = new Window("Confirm");
	// VerticalLayout subContent = new VerticalLayout();
	// subContent.setMargin(true);
	// subContent.addComponent(new Label("Leave channel"));
	//
	// Button yes = new Button("Yes");
	// Button no = new Button("No");
	// HorizontalLayout hl = new HorizontalLayout();
	// hl.addComponent(yes);
	// hl.addComponent(no);
	//
	// subWindow.setContent(subContent);
	// subWindow.center();
	// subWindow.setResizable(false);
	// addWindow(subWindow);
	// }

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
			IRCConnectionManager cm = (IRCConnectionManager) VaadinServlet.getCurrent().getServletContext()
					.getAttribute("connectionManager");
			if (cm != null) {
				nickField.setValue(cm.getNick());
				channelField.setValue(cm.getChannel());
			}

			Button connectButton = new Button("Connect");
			Button cancelButton = new Button("Cancel");
			subContent.addComponent(connectButton);
			subContent.addComponent(cancelButton);

			connectButton.addClickListener(new ClickListener() {

				// TODO: if already connected!

				@Override
				public void buttonClick(ClickEvent event) {
					try {

						IRCConnectionManager cm = (IRCConnectionManager) VaadinServlet.getCurrent().getServletContext()
								.getAttribute("connectionManager");

						String nickValue = nickField.getValue();
						String channelValue = channelField.getValue();

						boolean validNick = nickValue != null && nickValue.length() > 0;
						// TODO: # in channel?
						boolean validChannel = channelValue != null && channelValue.length() > 0;

						if (validNick && validChannel) {
							cm.setNick(nickValue);
							nick = nickValue;
							IRCConnection connection = cm.getConnection();
							connection.connect();
							connection.doJoin(channelValue);
							addChannel(channelValue);
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