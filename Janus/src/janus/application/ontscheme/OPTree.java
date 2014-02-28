package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedColumnAction;
import janus.application.actions.ShowObjectPropertyAssertionsAction;
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
public class OPTree extends  OntTree {
	private AbstractAction goToMappedColumn;
	private AbstractAction showObjectPropertyAssertions;
	
	protected TreeCellRenderer constructTreeCellRenderer() {
		return new OntPropertyTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP));
	}
	
	protected OntEntity constructRootNode() {
		return new OntEntity(Janus.ontBridge.getOWLTopObjectProperty(), OntEntityTypes.OWL_TOP_OBJECT_PROPERTY);
	}
	
	protected MutableTreeNode buildHierarchy(OntEntity entity) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
		
		Set<URI> children = Janus.ontBridge.getSubObjProps(entity.getURI());
		
		for(URI child : children)
			node.add(buildHierarchy(new OntEntity(child, OntEntityTypes.OBJECT_PROPERTY)));
			
		return node;
	}
	
	protected void addMenuItemsToPopupMenu() {
		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
		
		showObjectPropertyAssertions = new ShowObjectPropertyAssertionsAction();
		popupMenu.add(showObjectPropertyAssertions);
	}
	
	protected void determinePopupMenuItemOnOff(OntEntityTypes typeOfSelectedEntity) {
		// setting mapped column menu enabled/disabled
		if (typeOfSelectedEntity.equals(OntEntityTypes.OWL_TOP_OBJECT_PROPERTY)) {
			goToMappedColumn.setEnabled(false);
			showObjectPropertyAssertions.setEnabled(false);
		} else {
			goToMappedColumn.setEnabled(true);
			showObjectPropertyAssertions.setEnabled(true);
		}
	}
}