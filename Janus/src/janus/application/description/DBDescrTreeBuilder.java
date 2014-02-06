package janus.application.description;

import janus.Janus;
import janus.application.description.DBDescrTreeNode.Type;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

class DBDescrTreeBuilder {
	static TreeNode buildHierarchy(String catalog, String table, String column) {
		// root node: to be visible
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DBDescrTreeNode("'" + catalog + "'.'" + table + "'.'" + column + "'", Type.COLUMN));
		
		// Data Type
		root.add(getDataTypeSubtree(catalog, table, column));
		
		// Not Null
		root.add(getNotNullSubtree(catalog, table, column));
		
		// Auto Increment
		root.add(getAutoIncrementSubtree(catalog, table, column));
		
		// Default Value
		root.add(getDefaultValueSubtree(catalog, table, column));
		
		return root;
	}
	
	private static MutableTreeNode getDefaultValueSubtree(String catalog, String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Default Value", Type.LABEL));
		
		node.add(new DefaultMutableTreeNode(new DBDescrTreeNode(Janus.dbBridge.getColumnDefaultValue(catalog, table, column), Type.DEFAULT_VALUE)));
		
		return node;
	}
	
	private static MutableTreeNode getDataTypeSubtree(String catalog, String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Data Type", Type.LABEL));
		
		String type = Janus.dbBridge.getColumnTypeName(catalog, table, column);
		
		node.add(new DefaultMutableTreeNode(new DBDescrTreeNode(type, Type.DATA_TYPE)));
		
		return node;
	}
	
	private static MutableTreeNode getNotNullSubtree(String catalog, String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Not Null", Type.NOT_NULL));
		
		return node;
	}
	
	private static MutableTreeNode getAutoIncrementSubtree(String catalog, String table, String column) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new DBDescrTreeNode("Auto Increment", Type.AUTO_INC));
		
		return node;
	}
}