package janus.application;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class JanusWindow extends JFrame {
	public JanusWindow(String title) {
		super(title);
		
		UIBuilder.buildUI(this);
		
		EventHandlerBuilder.buildHandlers();
	}
}