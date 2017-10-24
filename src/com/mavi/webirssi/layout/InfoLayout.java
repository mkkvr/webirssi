package com.mavi.webirssi.layout;

import java.util.List;

import com.mavi.IRCConnectionManager;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class InfoLayout extends VerticalLayout implements TabSheetLayout {

	private RichTextArea area;
	private TextField tf;

	private IRCConnectionManager connectionManager;

	public InfoLayout(IRCConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		buildLayout();
	}

	private void buildLayout() {
		setSizeFull();

		// TODO: needed?
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();

		area = new RichTextArea();
		area.addStyleName("no-toolbar-top");
		area.addStyleName("no-toolbar-bottom");
		// area.setImmediate(true);
		area.setSizeFull();
		mainLayout.addComponent(area);
		mainLayout.setExpandRatio(area, 1.0f);

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
				// TODO: replace with command!
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

	public void refreshContent() {
		String content = "";
		List<String> infos = connectionManager.getInfos();

		for (String info : infos) {
			content = content + info + "<br>";
		}
		area.setValue(content);
	}

}
