package janus.application.ontscheme;

import janus.application.actions.GoToMappedColumnAction;
import janus.mapping.OntEntity;
import janus.mapping.OntEntityTypes;

import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public abstract class OntTree extends JScrollPane {
	protected JTree tree;
	protected JPopupMenu popupMenu;
	
	protected AbstractAction goToMappedColumn;
	
	protected OntTree() {
		buildUI();
	}
	
	public URI getSelectedURI() {
		return getSelectedTreeNode().getURI();
	}
	
	public OntEntity getSelectedEntity() {
		OntTreeNode node = getSelectedTreeNode();
		
		return new OntEntity(node.getURI(), node.getType());
	}
	
	public TreePath getTreePathOfEntity(URI entity) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			OntTreeNode entityNode = (OntTreeNode)node.getUserObject();
	        if (entityNode.getURI().equals(entity))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
	
	public void setSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	protected abstract MutableTreeNode buildHierarchy(OntTreeNode entity);
	
	private void buildUI() {
		tree  = new JTree(buildHierarchy(constructRootNode()));
		
		tree.setCellRenderer(constructTreeCellRenderer());
		
		tree.setDragEnabled(true);
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		popupMenu = new JPopupMenu();
		
		addMenuItemsToPopupMenu();
		
		tree.add(popupMenu);
		
		setViewportView(tree);
	}
	
	protected abstract TreeCellRenderer constructTreeCellRenderer();
	
	protected abstract OntTreeNode constructRootNode();
	
	void showPopupMenu(int x, int y) {
		OntTreeNode node = getSelectedTreeNode();
		OntEntityTypes type = node.getType();
		
		determinePopupMenuItemOnOff(type);
		
		popupMenu.show(this, x, y);
	}
	
	protected abstract void determinePopupMenuItemOnOff(OntEntityTypes typeOfSelectedEntity);
	
	boolean isSelectionEmpty() {
		return tree.isSelectionEmpty();
	}
	
	boolean isPathSelected(TreePath path) {
		return tree.isPathSelected(path);
	}
	
	TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}
	
	protected void addMenuItemsToPopupMenu() {
		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
	}
	
	private OntTreeNode getSelectedTreeNode() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		return (OntTreeNode)node.getUserObject();
	}
}
