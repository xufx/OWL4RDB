package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import janus.application.actions.GoToMappedColumnAction;
import janus.application.actions.ShowDataPropertyAssertionsAction;
import janus.mapping.OntEntityTypes;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.AbstractAction;
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
public class DPTree extends JScrollPane implements OntTree {
	private JTree tree;
	private JPopupMenu popupMenu;
	private AbstractAction goToMappedColumn;
	private AbstractAction showDataPropertyAssertions;
	
	public DPTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getDataPropertyHierarchy(new OntTreeNode(Janus.ontBridge.getOWLTopDataProperty(), OntEntityTypes.OWL_TOP_DATA_PROPERTY)));
		
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new OntDataPropertyTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP)));
		
		tree.add(buildPopupMenu());
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		setViewportView(tree);
	}
	
	private JPopupMenu buildPopupMenu() {
		popupMenu = new JPopupMenu();

		goToMappedColumn = new GoToMappedColumnAction();
		popupMenu.add(goToMappedColumn);
		
		showDataPropertyAssertions = new ShowDataPropertyAssertionsAction();
		popupMenu.add(showDataPropertyAssertions);
		
		return popupMenu;
	}
	
	private MutableTreeNode getDataPropertyHierarchy(OntTreeNode entity) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(entity);
		
		Set<URI> children = Janus.ontBridge.getSubDataProps(entity.getURI());
		
		for(URI child : children)
			node.add(getDataPropertyHierarchy(new OntTreeNode(child, OntEntityTypes.DATA_PROPERTY)));
			
		return node;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
	}
	
	public void addTreePopupTrigger(MouseListener trigger) {
		tree.addMouseListener(trigger);
	}
	
	void showPopupMenu(int x, int y) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		OntTreeNode ontNode = (OntTreeNode)node.getUserObject();
		// setting pop up menu enabled/disabled
		if (ontNode.getType().equals(OntEntityTypes.OWL_TOP_DATA_PROPERTY)) {
			goToMappedColumn.setEnabled(false);
			showDataPropertyAssertions.setEnabled(false);
		}
		else {
			goToMappedColumn.setEnabled(true);
			showDataPropertyAssertions.setEnabled(true);
		}
		
		popupMenu.show(this, x, y);
	}
	
	public URI getSelectedEntity() {
		return getSelectedDataProperty();
	}
	
	private URI getSelectedDataProperty() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		
		return ((OntTreeNode)node.getUserObject()).getURI();
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
	
	public TreePath getTreePathOfEntity(URI entity) {
		return getTreePathOfDataProperty(entity);
	}
	
	private TreePath getTreePathOfDataProperty(URI dp) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
		
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = e.nextElement();
			OntTreeNode dpNode = (OntTreeNode)node.getUserObject();
	        if (dpNode.getURI().equals(dp))
	            return new TreePath(node.getPath());
	    }
		
		return null;
	}
}

@SuppressWarnings("serial")
class OntDataPropertyTreeCellRenderer extends DefaultTreeCellRenderer {
    private Icon dataPropIcon;

    OntDataPropertyTreeCellRenderer(Icon dataPropIcon) {
        this.dataPropIcon = dataPropIcon;
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
    	setIcon(dataPropIcon);
    	
        return this;
    }
    
    private String getToolTipText(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        OntTreeNode dpNode = (OntTreeNode)node.getUserObject();
        
        return dpNode.getToolTipText();
    }
}