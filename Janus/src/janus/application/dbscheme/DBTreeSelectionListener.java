package janus.application.dbscheme;

import janus.application.description.AttrDescribable;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

public class DBTreeSelectionListener implements TreeSelectionListener {
	private AttrDescribable announcer;
	
	public DBTreeSelectionListener(AttrDescribable announcer) {
		this.announcer = announcer;
	}

	public void valueChanged(TreeSelectionEvent e) {
		JTree tree = (JTree)e.getSource();
		
		if (tree.isSelectionEmpty())
			return;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		if(node.isLeaf()) {
			DBTreeNode columnNode = (DBTreeNode)node.getUserObject();
			String column = columnNode.toString();
			
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
			DBTreeNode tableNode = (DBTreeNode)parent.getUserObject();
			String table = tableNode.toString();
			
			DefaultMutableTreeNode grand = (DefaultMutableTreeNode)parent.getParent();
			DBTreeNode catalogNode = (DBTreeNode)grand.getUserObject();
			String catalog = catalogNode.toString();
			
			announcer.describeDBColumn(catalog, table, column);
		}
	}
}
