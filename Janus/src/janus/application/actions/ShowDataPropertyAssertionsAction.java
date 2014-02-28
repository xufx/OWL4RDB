package janus.application.actions;

import janus.ImageURIs;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.ontdata.AssertionsPane;
import janus.application.ontscheme.OntTree;
import janus.mapping.OntEntity;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
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
		OntTree ontTree = getOntTreeToEventSource(e);
		
		OntEntity entity = ontTree.getSelectedOntEntity();
		
		URI dp = entity.getURI();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.ASSERTIONS));
		
		JTabbedPane assertionsPane = UIRegistry.getAssertionsTab();
		
		if (!alreadyExists(assertionsPane, dp)) {
			JSplitPane newPane = new AssertionsPane(entity);
			assertionsPane.addTab(dp.getFragment(), new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP), newPane);
			assertionsPane.setToolTipTextAt(assertionsPane.indexOfComponent(newPane), dp.toString());
		}
		assertionsPane.setSelectedIndex(indexOfTab(assertionsPane, dp));
	}
	
	private boolean alreadyExists(JTabbedPane assertionsPane, URI dp) {
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(dp.toString()))
				return true;
		
		return false;
	}
	
	private int indexOfTab(JTabbedPane assertionsPane, URI dp) {
		int index = -1;
		
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(dp.toString())) {
				index = i;
				break;
			}
		
		return index;
	}
	
	private OntTree getOntTreeToEventSource(ActionEvent e) {
		return (OntTree)((JPopupMenu)((Component)e.getSource()).getParent()).getInvoker();
	}
}
