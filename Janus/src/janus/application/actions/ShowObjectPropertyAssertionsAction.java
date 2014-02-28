package janus.application.actions;

import janus.ImageURIs;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.ontdata.AssertionsPane;
import janus.application.ontscheme.OntTree;
import janus.mapping.OntEntity;

import java.awt.Component;
import java.awt.event.ActionEvent;
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
		
		OntEntity op = ontTree.getSelectedOntEntity();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.ASSERTIONS));
		
		JTabbedPane assertionsPane = UIRegistry.getAssertionsTab();
		
		if (!alreadyExists(assertionsPane, op)) {
			JSplitPane newPane = new AssertionsPane(op);
			assertionsPane.addTab(op.toString(), new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP), newPane);
			assertionsPane.setToolTipTextAt(assertionsPane.indexOfComponent(newPane), op.getToolTipText());
		}
		assertionsPane.setSelectedIndex(indexOfTab(assertionsPane, op));
	}
	
	private boolean alreadyExists(JTabbedPane assertionsPane, OntEntity op) {
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(op.getToolTipText()))
				return true;
		
		return false;
	}
	
	private int indexOfTab(JTabbedPane assertionsPane, OntEntity op) {
		int index = -1;
		
		int tabCount = assertionsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (assertionsPane.getToolTipTextAt(i).equals(op.getToolTipText())) {
				index = i;
				break;
			}
		
		return index;
	}
	
	private OntTree getOntTreeToEventSource(ActionEvent e) {
		return (OntTree)((JPopupMenu)((Component)e.getSource()).getParent()).getInvoker();
	}
}
