package janus.application.components;

import java.awt.Component;

import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class DisplayTabbedPane extends JTabbedPane {
	public DisplayTabbedPane(int tabPlacement) {
		super(tabPlacement, JTabbedPane.WRAP_TAB_LAYOUT);
	}
	
	public void addTab(String title, Component component, boolean closable) {
		super.addTab(title, component);
		
		if (closable)
			setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this));
	}
}
