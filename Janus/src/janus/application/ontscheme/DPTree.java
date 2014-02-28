package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedColumnAction;
import janus.application.actions.ShowDataPropertyAssertionsAction;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

@SuppressWarnings("serial")
public class DPTree extends OntTree {
	private AbstractAction goToMappedColumn;
	private AbstractAction showDataPropertyAssertions;
	
	protected TreeCellRenderer constructTreeCellRenderer() {
		return new OntPropertyTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP));
	}
	
	protected OntEntity constructRootNode() {
		return new OntEntity(Janus.ontBridge.getOWLTopDataProperty(), OntEntityTypes.OWL_TOP_DATA_PROPERTY);
	}
	
	protected void addMenuItemsToPopupMenu() {
		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
		
		showDataPropertyAssertions = new ShowDataPropertyAssertionsAction();
		popupMenu.add(showDataPropertyAssertions);
	}
	
	protected MutableTreeNode buildHierarchy(OntEntity entity) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
		
		Set<URI> children = Janus.ontBridge.getSubDataProps(entity.getURI());
		
		for(URI child : children)
			node.add(buildHierarchy(new OntEntity(child, OntEntityTypes.DATA_PROPERTY)));
			
		return node;
	}
	
	protected void determinePopupMenuItemOnOff(OntEntityTypes typeOfSelectedEntity) {
		// setting pop up menu enabled/disabled
		if (typeOfSelectedEntity.equals(OntEntityTypes.OWL_TOP_DATA_PROPERTY)) {
			goToMappedColumn.setEnabled(false);
			showDataPropertyAssertions.setEnabled(false);
		}
		else {
			goToMappedColumn.setEnabled(true);
			showDataPropertyAssertions.setEnabled(true);
		}
	}
}