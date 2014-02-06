package janus.application;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class JanusWindow extends JFrame {
	public JanusWindow() {
		UIBuilder.buildUI(this);
		
		EventHandlerBuilder.buildHandlers();
	}
}