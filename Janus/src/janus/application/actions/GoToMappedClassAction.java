package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.dbscheme.DBTreeNodeTypes;
import janus.application.ontscheme.ClsTree;
import janus.database.Column;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedClassAction extends AbstractAction {
	
	public GoToMappedClassAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DBTree dbTree = UIRegistry.getDBTree();
		ClsTree clsTree = UIRegistry.getClsTree();
		
		DBTreeNodeTypes selectedNodeType = dbTree.getTypeOfSelectedNode();
		
		URI mappedClass = null;
		
		if (selectedNodeType.equals(DBTreeNodeTypes.TABLE))
			mappedClass = Janus.mappingMetadata.getMappedClass(dbTree.getSelectedTable());
		if (selectedNodeType.equals(DBTreeNodeTypes.PRIMARY) 
				|| selectedNodeType.equals(DBTreeNodeTypes.KEY)) {
			Column column = dbTree.getSelectedColumn();
			mappedClass = Janus.mappingMetadata.getMappedClass(column.getTableName(), column.getColumnName());
		}
		
		TreePath path = clsTree.getTreePathOfClass(mappedClass);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.ONTOLOGY));
			
			clsTree.setSelectionPath(path);
		}
	}

}
