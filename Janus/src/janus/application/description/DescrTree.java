package janus.application.description;

import janus.ImageURIs;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class DescrTree extends JScrollPane implements AttrDescribable {
	private DBDescrTreeCellRenderer dbRenderer;
	private ClsDescrTreeCellRenderer clsRenderer;
	private ObjPropDescrTreeCellRenderer objPropRenderer;
	private DataPropDescrTreeCellRenderer dataPropRenderer;
	
	public DescrTree() {
		dbRenderer = new DBDescrTreeCellRenderer(new ImageIcon(ImageURIs.DB_NON_PRIMARY),
					 							 new ImageIcon(ImageURIs.DB_PRIMARY),
					 							 new ImageIcon(ImageURIs.DESCR_DATA_TYPE), 
					 							 new ImageIcon(ImageURIs.DESCR_DEFAULT_VALUE),
					 							 new ImageIcon(ImageURIs.DESCR_NULL_VALUE),
					 							 new ImageIcon(ImageURIs.DESCR_LABLE),
					 							 new ImageIcon(ImageURIs.DESCR_CHECKED),
					 							 new ImageIcon(ImageURIs.DESCR_UNCHECKED));
		clsRenderer = new ClsDescrTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS),
												   new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS),
												   new ImageIcon(ImageURIs.ONT_ANON_CLS), 
												   new ImageIcon(ImageURIs.DESCR_LABLE),
												   new ImageIcon(ImageURIs.ONT_KEYS));
		objPropRenderer = new ObjPropDescrTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS),
														   new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS),
				   										   new ImageIcon(ImageURIs.ONT_ANON_CLS), 
				   										   new ImageIcon(ImageURIs.ONT_NAMED_OBJ_PROP),
				   										   new ImageIcon(ImageURIs.ONT_ANON_OBJ_PROP),
				   										   new ImageIcon(ImageURIs.DESCR_LABLE),
														   new ImageIcon(ImageURIs.DESCR_CHECKED),
														   new ImageIcon(ImageURIs.DESCR_UNCHECKED));
		dataPropRenderer = new DataPropDescrTreeCellRenderer(new ImageIcon(ImageURIs.ONT_NAMED_CLS),
				   											 new ImageIcon(ImageURIs.ONT_NAMED_EQUIVLNT_CLS),
				   											 new ImageIcon(ImageURIs.ONT_ANON_CLS), 
				   											 new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP),
				   											 new ImageIcon(ImageURIs.ONT_ANON_DATA_PROP),
				   											 new ImageIcon(ImageURIs.DESCR_DATA_TYPE),
				   											 new ImageIcon(ImageURIs.ONT_DATA_RANGE_BUT_DATA_TYPE),
				   											 new ImageIcon(ImageURIs.DESCR_LABLE),
															 new ImageIcon(ImageURIs.DESCR_CHECKED),
															 new ImageIcon(ImageURIs.DESCR_UNCHECKED));
	}
	
	public void describeOWLCls(URI cls) {
		JTree tree = new JTree(ClsDescrTreeBuilder.buildHierarchy(cls));
		tree.setCellRenderer(clsRenderer);
		expandAll(tree);
		
		setViewportView(tree);
	}
	
	public void describeOWLObjProp(URI objProperty) {
		JTree tree = new JTree(ObjPropDescrTreeBuilder.buildHierarchy(objProperty));
		tree.setCellRenderer(objPropRenderer);
		expandAll(tree);
		
		setViewportView(tree);
	}
	
	public void describeOWLDataProp(URI dataProperty) {
		JTree tree = new JTree(DataPropDescrTreeBuilder.buildHierarchy(dataProperty));
		tree.setCellRenderer(dataPropRenderer);
		expandAll(tree);
		
		setViewportView(tree);
	}
	
	public void describeDBColumn(String catalog, String table, String column) {
		JTree tree = new JTree(DBDescrTreeBuilder.buildHierarchy(catalog, table, column));
		tree.setCellRenderer(dbRenderer);
		expandAll(tree);
		
		setViewportView(tree);
	}
	
	private void expandAll(JTree tree) {
		TreeModel model = tree.getModel();
		Object root = model.getRoot();
		
		Object[] array = new Object[2];
		array[0] = root;
		
		int childCnt = model.getChildCount(root);
		
		for(int i = 0; i < childCnt; i++) {
			Object child = model.getChild(root, i);
			if(model.getChildCount(child) > 0) {
				array[1] = child;
				tree.expandPath(new TreePath(array));
			}
		}
	}
}