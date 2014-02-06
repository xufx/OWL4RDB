package janus.application.dbscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.dbscheme.DBTreeNode.Type;
import java.awt.Component;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public class DBTree extends JScrollPane {
	private JTree tree;
	
	public DBTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getDBHierarchy());
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new DBTreeCellRenderer(new ImageIcon(ImageURIs.DB_CATALOG),
													  new ImageIcon(ImageURIs.DB_TABLE),
													  new ImageIcon(ImageURIs.DB_KEY),
													  new ImageIcon(ImageURIs.DB_FIELD)));
		setViewportView(tree);
	}
	
	private TreeNode getDBHierarchy() {
		// root node
		String catalog = Janus.dbBridge.getConnectedCatalog();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DBTreeNode(catalog, Type.CATALOG));
		// table nodes
		Set<String> tables = Janus.dbBridge.getTables(catalog);
		for(String table : tables) {
			DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new DBTreeNode(table, Type.TABLE));
			root.add(tableNode);
			// primary key field nodes
			Set<String> pks = Janus.dbBridge.getPrimaryKeys(catalog, table);
			for(String pk : pks)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(pk, Type.KEY)));
			// field nodes
			Set<String> fields = Janus.dbBridge.getNonPKColumns(catalog, table);
			for(String field : fields)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(field, Type.FIELD)));
		}

		return root;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
}

@SuppressWarnings("serial")
class DBTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon catalogIcon;
    private Icon tableIcon;
    private Icon keyIcon;
    private Icon fieldIcon;

    DBTreeCellRenderer(Icon catalogIcon,
    		           Icon tableIcon,
    		           Icon keyIcon,
    		           Icon fieldIcon) {
        this.catalogIcon = catalogIcon;
        this.tableIcon = tableIcon;
        this.keyIcon = keyIcon;
        this.fieldIcon = fieldIcon;
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
        
    	if(isCatalogNode(value))
        	setIcon(catalogIcon);
        else if(isTableNode(value))
        	setIcon(tableIcon);
        else if(isKeyNode(value))
        	setIcon(keyIcon);
        else if(isFieldNode(value))
        	setIcon(fieldIcon);

        return this;
    }

    private boolean isCatalogNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.CATALOG)
        	return true;
        return false;
    }
    
    private boolean isTableNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == Type.TABLE)
        	return true;
        return false;
    }
    
    private boolean isKeyNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == Type.KEY)
        	return true;
        return false;
    }
    
    private boolean isFieldNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == Type.FIELD)
        	return true;
        return false;
    }
}