package janus.application.description;

import janus.Janus;
import janus.application.description.DBDescrTreeNode.Type;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
class DBDescrTreeCellRenderer extends DefaultTreeCellRenderer {
	private Icon fieldIcon;
	private Icon keyIcon;
	private Icon dataTypeIcon;
	private Icon defaultValueIcon;
	private Icon nullValueIcon;
	private Icon lablIcon;
	private Icon checkedIcon;
	private Icon uncheckedIcon;
	
	DBDescrTreeCellRenderer(Icon fieldIcon, Icon keyIcon,
							 Icon dataTypeIcon, Icon defaultValueIcon, Icon nullValueIcon,
							 Icon lablIcon, Icon checkedIcon, Icon uncheckedIcon) {
		this.fieldIcon = fieldIcon;
		this.keyIcon = keyIcon;
		this.dataTypeIcon = dataTypeIcon;
		this.defaultValueIcon = defaultValueIcon;
		this.nullValueIcon = nullValueIcon;
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
    	
    	if(isColumnNode(value)) {
    		if(isPrimaryKeyNode(value))
        		setIcon(keyIcon);
        	else 
        		setIcon(fieldIcon);
    	} else if(isDataTypeNode(value))
    		setIcon(dataTypeIcon);
    	else if(isNotNullNode(value)) {
    		if(isNullAllowed(value))
    			setIcon(uncheckedIcon);
    		else
    			setIcon(checkedIcon);
    	} else if(isAutoIncrementNode(value)) {
    		if(isAutoIncrement(value))
    			setIcon(checkedIcon);
    		else
    			setIcon(uncheckedIcon);
    	} else if(isDefaultValueNode(value)) {
    		if(isNullValue(value))
    			setIcon(nullValueIcon);
    		else
    			setIcon(defaultValueIcon);
    	} else if(isLabelNode(value))
        	setIcon(lablIcon);
    	
        return this;
    }
    
    private boolean isNullValue(Object value) {
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        
        if(nodeObject.toString() == null) return true;
        
        return false;
    }
    
    private boolean isDataTypeNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.DATA_TYPE)
        	return true;
        return false;
    }
    
    private boolean isPrimaryKeyNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)node.getUserObject();
        
        String catalog = getCatalog(nodeObject);
        String table = getTable(nodeObject);
        String column = getColumn(nodeObject);
        
        return Janus.dbBridge.isPrimaryKey(catalog, table, column);
    }
    
    private String getCatalog(DBDescrTreeNode node) {
    	String fullName = node.toString();
        String[] names = fullName.split("[.]");
        
        return names[0].replaceAll("'", " ").trim();
    }
    
    private String getTable(DBDescrTreeNode node) {
    	String fullName = node.toString();
        String[] names = fullName.split("[.]");
        
        return names[1].replaceAll("'", " ").trim();
    }
    
    private String getColumn(DBDescrTreeNode node) {
    	String fullName = node.toString();
        String[] names = fullName.split("[.]");
        
        return names[2].replaceAll("'", " ").trim();
    }
    
    private boolean isColumnNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.COLUMN)
        	return true;
        return false;
    }
    
    private boolean isNotNullNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.NOT_NULL)
        	return true;
        return false;
    }
    
    private boolean isDefaultValueNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.DEFAULT_VALUE)
        	return true;
        return false;
    }
    
    private boolean isAutoIncrementNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.AUTO_INC)
        	return true;
        return false;
    }
    
    private boolean isAutoIncrement(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        DBDescrTreeNode rootObject = (DBDescrTreeNode)(root.getUserObject());
        
        String catalog = getCatalog(rootObject);
        String table = getTable(rootObject);
        String column = getColumn(rootObject);
        
        return Janus.dbBridge.isAutoIncrement(catalog, table, column);
    }
    
    private boolean isNullAllowed(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)node.getRoot();
        DBDescrTreeNode rootObject = (DBDescrTreeNode)(root.getUserObject());
        
        String catalog = getCatalog(rootObject);
        String table = getTable(rootObject);
        String column = getColumn(rootObject);
        
        return Janus.dbBridge.isNullable(catalog, table, column);
    }
    
    private boolean isLabelNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBDescrTreeNode nodeObject = (DBDescrTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.LABEL)
        	return true;
        return false;
    }
}
