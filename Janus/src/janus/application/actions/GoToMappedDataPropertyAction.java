package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.DPTree;
import janus.database.Column;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedDataPropertyAction extends AbstractAction {
	
	public GoToMappedDataPropertyAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DBTree dbTree = UIRegistry.getDBTree();
		DPTree dpTree = UIRegistry.getDPTree();
		
		Column column = dbTree.getSelectedColumn();
		
		URI mappedDP = Janus.mappingMetadata.getMappedDataProperty(column.getTableName(), column.getColumnName());
		
		TreePath path = dpTree.getTreePathOfDataProperty(mappedDP);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.ONTOLOGY));
			
			JTabbedPane propertiesTab = UIRegistry.getPropertiesTab();
			if (propertiesTab.getSelectedIndex() != propertiesTab.indexOfTab(TabNames.DATA_PROPERTIES))
				propertiesTab.setSelectedIndex(propertiesTab.indexOfTab(TabNames.DATA_PROPERTIES));
			
			dpTree.setSelectionPath(path);
		}
	}

}
