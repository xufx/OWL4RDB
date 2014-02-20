package janus.application.actions;

import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.ontdata.IndividualPane;
import janus.application.ontscheme.ClsTree;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ShowMembersAction extends AbstractAction {
	private static final String NAME = "Show Members";
	
	public ShowMembersAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClsTree tree = UIRegistry.getClsTree();
		
		URI cls = tree.getSelectedClass();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.INDIVIDUALS));
		
		JTabbedPane individualsPane = UIRegistry.getIndividualsTab();
		
		if (!alreadyExists(individualsPane, cls)) {
			JSplitPane newPane = new IndividualPane(cls);
			individualsPane.addTab(cls.getFragment(), newPane);
			individualsPane.setToolTipTextAt(individualsPane.indexOfComponent(newPane), cls.toString());
		}
		individualsPane.setSelectedIndex(indexOfTab(individualsPane, cls));
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
