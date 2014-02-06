package janus.application.description;

import janus.Janus;
import janus.application.description.DataPropDescrTreeNode.Type;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class DataPropDescrTreeCellRenderer extends DefaultTreeCellRenderer {
	private Icon namedClsIcon;
	private Icon equivlntClsIcon;
	private Icon anonClsIcon;
	private Icon namedDataPropIcon;
	private Icon anonDataPropIcon;
	private Icon dataTypeIcon;
	private Icon dataRangeButDataTypeIcon;
	private Icon lablIcon;
	private Icon checkedIcon;
	private Icon uncheckedIcon;
	
	DataPropDescrTreeCellRenderer(Icon namedClsIcon, Icon equivlntClsIcon,
			 					  Icon anonClsIcon, Icon namedDataPropIcon, Icon anonDataPropIcon,
			 					  Icon dataTypeIcon, Icon dataRangeButDataTypeIcon, Icon lablIcon,
			 					  Icon checkedIcon, Icon uncheckedIcon) {
		this.namedClsIcon = namedClsIcon;
		this.equivlntClsIcon = equivlntClsIcon;
		this.anonClsIcon = anonClsIcon;
		this.namedDataPropIcon = namedDataPropIcon;
		this.anonDataPropIcon = anonDataPropIcon;
		this.dataTypeIcon = dataTypeIcon;
		this.dataRangeButDataTypeIcon = dataRangeButDataTypeIcon;
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
    	} else if(isNamedClsNode(value))
    		setIcon(namedClsIcon);
    	else if(isAnonClsNode(value))
    		setIcon(anonClsIcon);
    	else if(isNamedObjPropNode(value))
        	setIcon(namedDataPropIcon);
    	else if(isAnonObjPropNode(value))
        	setIcon(anonDataPropIcon);
    	else if(isDataTypeNode(value))
        	setIcon(dataTypeIcon);
    	else if(isDataRangeButDataTypeNode(value))
    		setIcon(dataRangeButDataTypeIcon);
    	else if(isLabelNode(value))
        	setIcon(lablIcon);
    	else if(isPropCharNode(value))
    		if(getPropChar(value).equals(PropChars.FUNCTIONAL.toString()))
    			setCheckBoxIcon(isRootFunctional(value));
    	
        return this;
    }
    
    private boolean isDataRangeButDataTypeNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.DATA_RANGE_BUT_DATA_TYPE)
        	return true;
        return false;
    }
    
    private void setCheckBoxIcon(boolean value) {
    	if(value)
    		setIcon(checkedIcon); 
    	else
    		setIcon(uncheckedIcon);
    }
    
    private boolean isRootFunctional(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        DataPropDescrTreeNode rootObject = (DataPropDescrTreeNode)root.getUserObject();
        
        return Janus.ontBridge.isFunctionalDataProp(rootObject.getURI());
    }
    
    private String getPropChar(Object value) {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
    	DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        
        return nodeObject.toString();
    }
    
    private boolean isPropCharNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.PROP_CHAR)
        	return true;
        return false;
    }
    
    private boolean isDataTypeNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)node.getUserObject();
        if(nodeObject.getType() == Type.DATA_TYPE)
        	return true;
        
        return false;
    }
    
    private boolean isEquivlntClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)node.getUserObject();
        if(Janus.ontBridge.hasEquivlntCls(nodeObject.getURI()))
        	return true;
        
        return false;
    }
    
    private boolean isNamedClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NAMED_CLS)
        	return true;
        return false;
    }
    
    private boolean isAnonClsNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.ANON_CLS)
        	return true;
        return false;
    }
    
    private boolean isNamedObjPropNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NAMED_DATA_PROP)
        	return true;
        return false;
    }
    
    private boolean isAnonObjPropNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.ANON_DATA_PROP)
        	return true;
        return false;
    }
    
    private boolean isLabelNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DataPropDescrTreeNode nodeObject = (DataPropDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.LABEL)
        	return true;
        return false;
    }
}
