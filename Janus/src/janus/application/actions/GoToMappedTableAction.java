package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.ClsTree;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedTableAction extends AbstractAction {
	
	public GoToMappedTableAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClsTree clsTree = UIRegistry.getClsTree();
		DBTree dbTree = UIRegistry.getDBTree();
		
		String mappedTable = Janus.mappingMetadata.getMappedTableNameToClass(clsTree.getSelectedClass());
		
		TreePath path = dbTree.getTreePathOfTable(mappedTable);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.DATABASE));
			
			dbTree.setSelectionPath(path);
		}
	}

}
