package janus.application.actions;

import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.ontdata.AssertionsPane;
import janus.application.ontscheme.DPTree;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ShowDataPropertyAssertionsAction extends AbstractAction {
	private static final String NAME = "Show Data Property Assertions";
	
	public ShowDataPropertyAssertionsAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DPTree tree = UIRegistry.getDPTree();
		
		URI dp = tree.getSelectedDataProperty();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.ASSERTIONS));
		
		JTabbedPane individualsPane = UIRegistry.getAssertionsTab();
		
		if (!alreadyExists(individualsPane, dp)) {
			JSplitPane newPane = new AssertionsPane(dp);
			individualsPane.addTab(dp.getFragment(), newPane);
			individualsPane.setToolTipTextAt(individualsPane.indexOfComponent(newPane), dp.toString());
		}
		individualsPane.setSelectedIndex(indexOfTab(individualsPane, dp));
	}
	
	private boolean alreadyExists(JTabbedPane individualsPane, URI cls) {
		int tabCount = individualsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (individualsPane.getToolTipTextAt(i).equals(cls.toString()))
				return true;
		
		return false;
	}
	
	private int indexOfTab(JTabbedPane individualsPane, URI cls) {
		int index = -1;
		
		int tabCount = individualsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (individualsPane.getToolTipTextAt(i).equals(cls.toString())) {
				index = i;
				break;
			}
		
		return index;
	}
}
