package janus.application.dbscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.ShowMappedClassAction;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class DBTree extends JScrollPane {
	private JTree tree;
	private JPopupMenu popupMenu;
	private AbstractAction mappedClass;
	
	public DBTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getDBHierarchy());
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new DBTreeCellRenderer(new ImageIcon(ImageURIs.DB_CATALOG),
													  new ImageIcon(ImageURIs.DB_TABLE),
													  new ImageIcon(ImageURIs.DB_PRIMARY),
													  new ImageIcon(ImageURIs.DB_NON_PRIMARY)));
		
		tree.add(buildPopupMenu());
		
		setViewportView(tree);
	}
	
	private JPopupMenu buildPopupMenu() {
		popupMenu = new JPopupMenu();
		mappedClass = new ShowMappedClassAction("Mapped Class");
		popupMenu.add(mappedClass);
		
		return popupMenu;
	}
	
	private TreeNode getDBHierarchy() {
		// root node
		String catalog = Janus.cachedDBMetadata.getCatalog();
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DBTreeNode(catalog, DBTreeNodeTypes.CATALOG));
		// table nodes
		Set<String> tables = Janus.cachedDBMetadata.getTableNames();
		for(String table: tables) {	
			DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new DBTreeNode(table, DBTreeNodeTypes.TABLE));
			root.add(tableNode);
			
			Set<String> columns = Janus.cachedDBMetadata.getColumns(table);
			
			// primary key column nodes
			List<String> pks = Janus.cachedDBMetadata.getPrimaryKeys(table);
			for(String pk: pks)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(pk, DBTreeNodeTypes.PRIMARY)));
			
			columns.removeAll(pks);

			// non-key column nodes
			Set<String> nonKeys = Janus.cachedDBMetadata.getNonKeyColumns(table);
			for(String nonKey : nonKeys)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(nonKey, DBTreeNodeTypes.NON_KEY)));
			
			columns.removeAll(nonKeys);
			
			// key column (except primary) nodes
			for(String key: columns)
				tableNode.add(new DefaultMutableTreeNode(new DBTreeNode(key, DBTreeNodeTypes.KEY)));
		}

		return root;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	void showPopupMenu(int x, int y) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DBTreeNode dbNode = (DBTreeNode)node.getUserObject();
		DBTreeNodeTypes type = dbNode.getType();
		if (type.equals(DBTreeNodeTypes.CATALOG))
			mappedClass.setEnabled(false);
		else
			mappedClass.setEnabled(true);
		
		popupMenu.show(this, x, y);
	}
	
	public DBTreeNodeTypes getTypeOfSelectedNode() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		DBTreeNode dbNode = (DBTreeNode)node.getUserObject();

		return dbNode.getType();
	}
	
	TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}
	
	void setSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}
	
	boolean isPathSelected(TreePath path) {
		return tree.isPathSelected(path);
	}
	
	boolean isSelectionEmpty() {
		return tree.isSelectionEmpty();
	}
}

@SuppressWarnings("serial")
class DBTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon catalogIcon;
    private Icon tableIcon;
    private Icon primaryIcon;
    private Icon nonPrimaryIcon;

    DBTreeCellRenderer(Icon catalogIcon,
    		           Icon tableIcon,
    		           Icon primaryIcon,
    		           Icon nonPrimaryIcon) {
        this.catalogIcon = catalogIcon;
        this.tableIcon = tableIcon;
        this.primaryIcon = primaryIcon;
        this.nonPrimaryIcon = nonPrimaryIcon;
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
        else if(isPrimaryNode(value))
        	setIcon(primaryIcon);
        else if(isNonPrimaryNode(value))
        	setIcon(nonPrimaryIcon);

        return this;
    }

    private boolean isCatalogNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == DBTreeNodeTypes.CATALOG)
        	return true;
        return false;
    }
    
    private boolean isTableNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if(nodeObject.getType() == DBTreeNodeTypes.TABLE)
        	return true;
        return false;
    }
    
    private boolean isPrimaryNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == DBTreeNodeTypes.PRIMARY)
        	return true;
        return false;
    }
    
    private boolean isNonPrimaryNode(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        DBTreeNode nodeObject = (DBTreeNode)(node.getUserObject());
        if (nodeObject.getType() == DBTreeNodeTypes.NON_KEY
        		|| nodeObject.getType() == DBTreeNodeTypes.KEY)
        	return true;
        return false;
    }
}