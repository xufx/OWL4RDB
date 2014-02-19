package janus.application.dbscheme;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.TreePath;

public class DBTreePopupTrigger extends MouseAdapter {
	private DBTree tree;
	
	public DBTreePopupTrigger(DBTree tree) {
		this.tree = tree;
	}
	
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int x = e.getX();
			int y = e.getY();
			
			TreePath path = tree.getPathForLocation(x, y);
			if (path != null) {
				if (!tree.isPathSelected(path))
					tree.setSelectionPath(path);
				
				tree.showPopupMenu(x, y);
			} else {
				if (!tree.isSelectionEmpty())
					tree.showPopupMenu(x, y);
			}
		}
	} 
}
