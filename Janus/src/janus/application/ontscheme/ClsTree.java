package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedColumnAction;
import janus.application.actions.GoToMappedTableAction;
import janus.application.actions.ShowClassAssertionsAction;
import janus.mapping.OntEntityTypes;

import java.net.URI;
import java.awt.Component;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

@SuppressWarnings("serial")
public class ClsTree extends OntTree {
	private AbstractAction goToMappedColumn;
	private AbstractAction goToMappedTable;
	private AbstractAction showClassAssertions;
	
	protected TreeCellRenderer constructTreeCellRenderer() {
		return new OntClassTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS), 
				new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS));
	}
	
	protected OntTreeNode constructRootNode() {
		return new OntTreeNode(Janus.ontBridge.getOWLThingURI(), OntEntityTypes.OWL_THING_CLASS);
	}
	
	protected void addMenuItemsToPopupMenu() {
		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
		
		goToMappedTable = new GoToMappedTableAction();
		popupMenu.add(goToMappedTable);
		
		showClassAssertions = new ShowClassAssertionsAction();
		popupMenu.add(showClassAssertions);
	}
	
	protected MutableTreeNode buildHierarchy(OntTreeNode entity) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
		
		Set<URI> children = Janus.ontBridge.getSubClses(entity.getURI());
		for(URI child : children)
			node.add(buildHierarchy(new OntTreeNode(child, Janus.mappingMetadata.getClassType(child))));
		
		return node;
	}
	
	protected void determinePopupMenuItemOnOff(OntEntityTypes typeOfSelectedEntity) {
		// setting mapped table menu enabled/disabled
		if (typeOfSelectedEntity.equals(OntEntityTypes.OWL_THING_CLASS) 
				|| typeOfSelectedEntity.equals(OntEntityTypes.COLUMN_CLASS))
			goToMappedTable.setEnabled(false);
		else
			goToMappedTable.setEnabled(true);
		// setting mapped column menu enabled/disabled
		if (typeOfSelectedEntity.equals(OntEntityTypes.OWL_THING_CLASS) 
				|| typeOfSelectedEntity.equals(OntEntityTypes.TABLE_CLASS))
			goToMappedColumn.setEnabled(false);
		else
			goToMappedColumn.setEnabled(true);
	}
}

@SuppressWarnings("serial")
class OntClassTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon clsIcon;
    private Icon equivlntClsIcon;

    OntClassTreeCellRenderer(Icon clsIcon, Icon equivlntClsIcon) {
        this.clsIcon = clsIcon;
        this.equivlntClsIcon = equivlntClsIcon;
    }

    public Component getTreeCellRendererComponent(JTree tree,
							                      Object value,
							                      boolean sel,
							                      boolean expanded,
							                      boolean leaf,
							                      int row,
							                      boolean hasFocus) {
    	setToolTipText(getToolTipText(value));
    	
    	super.getTreeCellRendererComponent(tree, value, sel,
                        				   expanded, leaf, row,
                                           hasFocus);
    	
    	if(isEquivlntClsNode(value))
        	setIcon(equivlntClsIcon);
    	else
    		setIcon(clsIcon);
       
        return this;
    }
    
    private String getToolTipText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode clsNode = (OntTreeNode)node.getUserObject();
        
        return clsNode.getToolTipText();
    }

    private boolean isEquivlntClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode clsNode = (OntTreeNode)node.getUserObject();
        
        return Janus.ontBridge.hasEquivlntCls(clsNode.getURI());
    }
}