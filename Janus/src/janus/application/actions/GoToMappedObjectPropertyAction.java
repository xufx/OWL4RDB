package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.OPTree;
import janus.database.Column;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class GoToMappedObjectPropertyAction extends AbstractAction {
	
	public GoToMappedObjectPropertyAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		DBTree dbTree = UIRegistry.getDBTree();
		OPTree opTree = UIRegistry.getOPTree();
		
		Column column = dbTree.getSelectedColumn();
		
		URI mappedOP = Janus.mappingMetadata.getMappedObjectProperty(column.getTableName(), column.getColumnName());
		
		TreePath path = opTree.getTreePathOfObjectProperty(mappedOP);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.ONTOLOGY));
			
			JTabbedPane propertiesTab = UIRegistry.getPropertiesTab();
			if (propertiesTab.getSelectedIndex() != propertiesTab.indexOfTab(TabNames.OBJECT_PROPERTIES))
				propertiesTab.setSelectedIndex(propertiesTab.indexOfTab(TabNames.OBJECT_PROPERTIES));
			
			opTree.setSelectionPath(path);
		}
	}

}
