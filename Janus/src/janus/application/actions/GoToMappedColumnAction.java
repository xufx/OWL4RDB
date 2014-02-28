package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.OntTree;
import janus.database.DBColumn;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import java.awt.Component;
import java.awt.event.ActionEvent;

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
		
		OntEntity entity = ontTree.getSelectedOntEntity();
		
		DBColumn mappedColumn = null;
		
		if (entity.getType().equals(OntEntityTypes.COLUMN_CLASS))
			mappedColumn = Janus.mappingMetadata.getMappedColumnToClass(entity.getURI());
		else if (entity.getType().equals(OntEntityTypes.DATA_PROPERTY) 
				|| entity.getType().equals(OntEntityTypes.OBJECT_PROPERTY))
			mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(entity.getURI());
		
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
