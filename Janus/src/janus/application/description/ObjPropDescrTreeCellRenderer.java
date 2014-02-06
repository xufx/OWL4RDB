package janus.application.description;

import janus.Janus;
import janus.application.description.ObjPropDescrTreeNode.Type;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class ObjPropDescrTreeCellRenderer extends DefaultTreeCellRenderer {
	private Icon namedClsIcon;
	private Icon equivlntClsIcon;
	private Icon anonClsIcon;
	private Icon namedObjPropIcon;
	private Icon anonObjPropIcon;
	private Icon lablIcon;
	private Icon checkedIcon;
	private Icon uncheckedIcon;
	
	ObjPropDescrTreeCellRenderer(Icon namedClsIcon, Icon equivlntClsIcon,
								 Icon anonClsIcon, Icon namedObjPropIcon, Icon anonObjPropIcon,
								 Icon lablIcon, Icon checkedIcon, Icon uncheckedIcon) {
		this.namedClsIcon = namedClsIcon;
		this.equivlntClsIcon = equivlntClsIcon;
		this.anonClsIcon = anonClsIcon;
		this.namedObjPropIcon = namedObjPropIcon;
		this.anonObjPropIcon = anonObjPropIcon;
		this.lablIcon = lablIcon;
		this.checkedIcon = checkedIcon;
		this.uncheckedIcon = uncheckedIcon;
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
    	
    	if(isNamedClsNode(value)) {
    		if(isEquivlntClsNode(value))
        		setIcon(equivlntClsIcon);
        	else 
        		setIcon(namedClsIcon);
    	} else if(isAnonClsNode(value))
    		setIcon(anonClsIcon);
    	else if(isNamedObjPropNode(value))
        	setIcon(namedObjPropIcon);
    	else if(isAnonObjPropNode(value))
        	setIcon(anonObjPropIcon);
    	else if(isLabelNode(value))
    		setIcon(lablIcon);
    	else if(isPropCharNode(value)) {
    		if(getPropChar(value).equals(PropChars.ASYMMETRIC.toString()))
    			setCheckBoxIcon(isRootAsymmetric(value));
    		else if(getPropChar(value).equals(PropChars.FUNCTIONAL.toString()))
    			setCheckBoxIcon(isRootFunctional(value));
    		else if(getPropChar(value).equals(PropChars.INVERSE_FUNCTIONAL.toString()))
    			setCheckBoxIcon(isRootInverseFunctional(value));
    		else if(getPropChar(value).equals(PropChars.IRREFLEXIVE.toString()))
    			setCheckBoxIcon(isRootIrreflexive(value));
    		else if(getPropChar(value).equals(PropChars.REFLEXIVE.toString()))
    			setCheckBoxIcon(isRootReflexive(value));
    		else if(getPropChar(value).equals(PropChars.SYMMETRIC.toString()))
    			setCheckBoxIcon(isRootSymmetric(value));
    		else if(getPropChar(value).equals(PropChars.TRANSITIVE.toString()))
    			setCheckBoxIcon(isRootTransitive(value));
    	}
    	
        return this;
    }
    
    private String getPropChar(Object value) {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        
        return nodeObject.toString();
    }
    
    private void setCheckBoxIcon(boolean value) {
    	if(value)
    		setIcon(checkedIcon); 
    	else
    		setIcon(uncheckedIcon);
    }
    
    private boolean isPropCharNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.PROP_CHAR)
        	return true;
        return false;
    }
    
    private boolean isRootTransitive(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isTransitive(rootObject.getURI());
    }
    
    private boolean isRootSymmetric(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isSymmetric(rootObject.getURI());
    }
    
    private boolean isRootReflexive(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isReflexive(rootObject.getURI());
    }
    
    private boolean isRootIrreflexive(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isIrreflexive(rootObject.getURI());
    }
    
    private boolean isRootInverseFunctional(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isInverseFunctional(rootObject.getURI());
    }
    
    private boolean isRootAsymmetric(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isAsymmetric(rootObject.getURI());
    }
    
    private boolean isRootFunctional(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        ObjPropDescrTreeNode rootObject = (ObjPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isFunctionalObjProp(rootObject.getURI());
    }
    
    private boolean isEquivlntClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)node.getUserObject();
        
        return Janus.ontBridge.hasEquivlntCls(nodeObject.getURI());
    }
    
    private boolean isNamedClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NAMED_CLS)
        	return true;
        return false;
    }
    
    private boolean isAnonClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.ANON_CLS)
        	return true;
        return false;
    }
    
    private boolean isNamedObjPropNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NAMED_OBJ_PROP)
        	return true;
        return false;
    }
    
    private boolean isAnonObjPropNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.ANON_OBJ_PROP)
        	return true;
        return false;
    }
    
    private boolean isLabelNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        ObjPropDescrTreeNode nodeObject = (ObjPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.LABEL)
        	return true;
        return false;
    }
}
