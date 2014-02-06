package janus.application.description;

import janus.Janus;
import janus.application.description.ClsDescrTreeNode.Type;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class ClsDescrTreeCellRenderer extends DefaultTreeCellRenderer {
	private Icon namedClsIcon;
	private Icon equivlntClsIcon;
	private Icon anonClsIcon;
	private Icon lablIcon;
	private Icon keysIcon;
	
	ClsDescrTreeCellRenderer(Icon namedClsIcon, Icon equivlntClsIcon,
							 Icon anonClsIcon, Icon lablIcon, Icon keysIcon) {
		this.namedClsIcon = namedClsIcon;
		this.equivlntClsIcon = equivlntClsIcon;
		this.anonClsIcon = anonClsIcon;
		this.lablIcon = lablIcon;
		this.keysIcon = keysIcon;
	}

    public Component getTreeCellRendererComponent(JTree tree,
							                      Object value,
							                      boolean sel,
							                      boolean expanded,
							                      boolean leaf,
							                      int row,
							                      boolean hasFocus) {
    	
    	super.getTreeCellRendererComponent(tree, value, sel,
                        				   expanded, leaf, row,
                                           hasFocus);
    	
    	if (isNamedClsNode(value)) {
    		if (isEquivlntClsNode(value))
        		setIcon(equivlntClsIcon);
        	else 
        		setIcon(namedClsIcon);
    	} else if (isAnonClsNode(value))
    		setIcon(anonClsIcon);
    	else if (isLabelNode(value))
        	setIcon(lablIcon);
    	else if (isKeysNode(value))
    		setIcon(keysIcon);
    	
        return this;
    }
    
    private boolean isEquivlntClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ClsDescrTreeNode nodeObject = (ClsDescrTreeNode)node.getUserObject();
        
        return Janus.ontBridge.hasEquivlntCls(nodeObject.getURI());
    }
    
    private boolean isNamedClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ClsDescrTreeNode nodeObject = (ClsDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NAMED_CLS)
        	return true;
        return false;
    }
    
    private boolean isAnonClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ClsDescrTreeNode nodeObject = (ClsDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.ANON_CLS)
        	return true;
        return false;
    }
    
    private boolean isLabelNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ClsDescrTreeNode nodeObject = (ClsDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.LABEL)
        	return true;
        return false;
    }
    
    private boolean isKeysNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ClsDescrTreeNode nodeObject = (ClsDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.KEYS)
        	return true;
        return false;
    }
}
