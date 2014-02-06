package janus.application.ontscheme;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ClsTreePopupTrigger extends MouseAdapter {
	private ClsTree tree;
	
	public ClsTreePopupTrigger(ClsTree tree) {
		this.tree = tree;
	}
	
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			int x = e.getX();
			int y = e.getY();
			
			if (tree.getSelectedClass() != null)
				tree.showPopupMenu(x, y);
		}
	} 
}
