package janus.application.components;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ClosableTabbedPane extends JTabbedPane {
	public ClosableTabbedPane(int tabPlacement) {
		super(tabPlacement, JTabbedPane.WRAP_TAB_LAYOUT);
	}
	
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		
		setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this));
	}
	
	public void addTab(String title, Icon icon, Component component) {
		super.addTab(title, icon, component);
		
		setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this));
	}
}
