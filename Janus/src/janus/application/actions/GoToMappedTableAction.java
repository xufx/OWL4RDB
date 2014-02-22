package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.OntTree;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedTableAction extends AbstractAction {
	private static final String NAME = "Go to Mapped Table";
	
	public GoToMappedTableAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OntTree clsTree = UIRegistry.getClsTree();
		DBTree dbTree = UIRegistry.getDBTree();
		
		String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(clsTree.getSelectedURI());
		
		TreePath path = dbTree.getTreePathOfTable(mappedTable);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.DATABASE));
			
			dbTree.setSelectionPath(path);
		}
	}
}
