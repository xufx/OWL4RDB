package janus.application.ontscheme;

import janus.mapping.OntEntity;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class OntPropertyTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon propertyIcon;

    OntPropertyTreeCellRenderer(Icon propertyIcon) {
        this.propertyIcon = propertyIcon;
    }

    public Component getTreeCellRendererComponent(JTree tree,
							                      Object value,
							                      boolean sel,
							                      boolean expanded,
							                      boolean leaf,
							                      int row,
							                      boolean hasFocus) {
    	// except root node
    	if(!((DefaultMutableTreeNode)value).isRoot())
    		setToolTipText(getToolTipText(value));
    	
    	super.getTreeCellRendererComponent(tree, value, sel,
                        				   expanded, leaf, row,
                                           hasFocus);
    	setIcon(propertyIcon);
    	
        return this;
    }
    
    private String getToolTipText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntEntity propertyNode = (OntEntity)node.getUserObject();
        
        return propertyNode.getToolTipText();
    }
}