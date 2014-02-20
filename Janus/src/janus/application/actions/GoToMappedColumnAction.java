package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dbscheme.DBTree;
import janus.application.ontscheme.ClsTree;
import janus.application.ontscheme.DPTree;
import janus.application.ontscheme.OPTree;
import janus.database.Column;

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
		Component source = getSource(e);
		
		Column mappedColumn = null;
		
		if (source instanceof ClsTree)
			mappedColumn = Janus.mappingMetadata.getMappedColumnToClass(((ClsTree)source).getSelectedClass());
		else if (source instanceof OPTree)
			mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(((OPTree)source).getSelectedObjectProperty());
		else if (source instanceof DPTree)
			mappedColumn = Janus.mappingMetadata.getMappedColumnToProperty(((DPTree)source).getSelectedDataProperty());
		
		DBTree dbTree = UIRegistry.getDBTree();
		
		TreePath path = dbTree.getTreePathOfColumn(mappedColumn);
		
		if (path != null) {
			JTabbedPane schemeTab = UIRegistry.getschemeTab();
			schemeTab.setSelectedIndex(schemeTab.indexOfTab(TabNames.DATABASE));
			
			dbTree.setSelectionPath(path);
		}
	}
	
	private Component getSource(ActionEvent e) {
		return ((JPopupMenu)((Component)e.getSource()).getParent()).getInvoker();
	}
}
