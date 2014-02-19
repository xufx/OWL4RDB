package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.ShowMembersAction;

import java.net.URI;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class OPTree extends JScrollPane {
	private JTree tree;
	private JPopupMenu popupMenu;
	
	public OPTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree  = new JTree(getObjPropertyHierarchy(Janus.ontBridge.getOWLTopObjectProperty()));
		
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new OntObjPropertyTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP)));
		
		tree.add(buildPopupMenu());
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		setViewportView(tree);
	}
	
	private JPopupMenu buildPopupMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.add(new ShowMembersAction("Members"));
		
		return popupMenu;
	}
	
	private MutableTreeNode getObjPropertyHierarchy(URI opURI) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OntTreeNode(opURI));
		
		Set<URI> children = Janus.ontBridge.getSubObjProps(opURI);
		
		for(URI child : children)
			node.add(getObjPropertyHierarchy(child));
			
		return node;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	void showPopupMenu(int x, int y) {
		popupMenu.show(this, x, y);
	}
	
	TreePath getPathForLocation(int x, int y) {
		return tree.getPathForLocation(x, y);
	}
	
	public void setSelectionPath(TreePath path) {
		tree.setSelectionPath(path);
	}
	
	boolean isPathSelected(TreePath path) {
		return tree.isPathSelected(path);
	}
	
	boolean isSelectionEmpty() {
		return tree.isSelectionEmpty();
	}
	
	public TreePath getMatchedNode(URI op) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			OntTreeNode opNode = (OntTreeNode)node.getUserObject();
	        if (opNode.getURI().equals(op))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
}

@SuppressWarnings("serial")
class OntObjPropertyTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon objPropIcon;

    OntObjPropertyTreeCellRenderer(Icon objPropIcon) {
        this.objPropIcon = objPropIcon;
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
    	setIcon(objPropIcon);
    	
        return this;
    }
    
    private String getToolTipText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode opNode = (OntTreeNode)node.getUserObject();
        
        return opNode.getToolTipText();
    }
}