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
public class ShowObjectPropertyAssertionsAction extends AbstractAction {
	private static final String NAME = "Show Object Property Assertions";
	
	public ShowObjectPropertyAssertionsAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OntTree ontTree = getOntTreeToEventSource(e);
		
		OntEntity entity = ontTree.getSelectedEntity();
		
		URI op = entity.getURI();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.ASSERTIONS));
		
		JTabbedPane assertionsPane = UIRegistry.getAssertionsTab();
		
		if (!alreadyExists(assertionsPane, op)) {
			JSplitPane newPane = new AssertionsPane(entity);
			assertionsPane.addTab(op.getFragment(), new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP), newPane);
			assertionsPane.setToolTipTextAt(assertionsPane.indexOfComponent(newPane), op.toString());
		}
		assertionsPane.setSelectedIndex(indexOfTab(assertionsPane, op));
	}
	
	private boolean alreadyExists(JTabbedPane assertionsPane, URI op) {
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(op.toString()))
				return true;
		
		return false;
	}
	
	private int indexOfTab(JTabbedPane assertionsPane, URI op) {
		int index = -1;
		
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(op.toString())) {
				index = i;
				break;
			}
		
		return index;
	}
	
	private OntTree getOntTreeToEventSource(ActionEvent e) {
		return (OntTree)((JPopupMenu)((Component)e.getSource()).getParent()).getInvoker();
	}
}
