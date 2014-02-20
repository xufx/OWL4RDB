package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.OntTree;
import janus.database.Column;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedColumnAction extends AbstractAction {
	private static final String NAME = "Go to Mapped Column";
	
	public GoToMappedColumnAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OntTree ontTree = getOntTreeToEventSource(e);
		
		URI entity = ontTree.getSelectedEntity();
		
		
		
		Column mappedColumn = Janus.mappingMetadata.getMappedColumnToClass(ontTree.getSelectedEntity());
		
		DBTree dbTree = UIRegistry.getDBTree();
		
		TreePath path = dbTree.getTreePathOfColumn(mappedColumn);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.DATABASE));
			
			dbTree.setSelectionPath(path);
		}
	}
	
	private OntTree getOntTreeToEventSource(ActionEvent e) {
		return (OntTree)((JPopupMenu)((Component)e.getSource()).getParent()).getInvoker();
	}
}
