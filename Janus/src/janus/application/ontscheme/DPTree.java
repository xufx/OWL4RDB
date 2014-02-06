package janus.application.ontscheme;

import janus.ImageURIs;
import janus.Janus;
import java.awt.Component;
import java.net.URI;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

@SuppressWarnings("serial")
public class DPTree extends JScrollPane {
	private JTree tree;
	
	public DPTree() {
		buildUI();
	}
	
	private void buildUI() {
		tree = new JTree(getDataPropertyHierarchy(Janus.ontBridge.getOWLTopDataProperty()));
		
		tree.setDragEnabled(true);
		
		tree.setCellRenderer(new OntDataPropertyTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP)));
		
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		setViewportView(tree);
	}
	
	private MutableTreeNode getDataPropertyHierarchy(URI dpURI) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(new OntTreeNode(dpURI));
		
		Set<URI> children = Janus.ontBridge.getSubDataProps(dpURI);
		
		for(URI child : children)
			node.add(getDataPropertyHierarchy(child));
			
		return node;
	}
	
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		tree.addTreeSelectionListener(listener);
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