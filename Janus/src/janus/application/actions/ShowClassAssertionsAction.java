package janus.application.actions;

import janus.ImageURIs;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.ontdata.AssertionsPane;
import janus.application.ontscheme.ClsTree;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ShowClassAssertionsAction extends AbstractAction {
	private static final String NAME = "Show Class Assertions";
	
	public ShowClassAssertionsAction() {
		super(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClsTree tree = UIRegistry.getClsTree();
		
		URI cls = tree.getSelectedEntity();
		
		JTabbedPane displayPane = UIRegistry.getDisplayTab();
		displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.ASSERTIONS));
		
		JTabbedPane individualsPane = UIRegistry.getAssertionsTab();
		
		if (!alreadyExists(individualsPane, cls)) {
			JSplitPane newPane = new AssertionsPane(cls);
			individualsPane.addTab(cls.getFragment(), new ImageIcon(ImageURIs.ONT_NAMED_CLS),  newPane);
			individualsPane.setToolTipTextAt(individualsPane.indexOfComponent(newPane), cls.toString());
		}
		individualsPane.setSelectedIndex(indexOfTab(individualsPane, cls));
	}
	
	private boolean alreadyExists(JTabbedPane individualsPane, URI cls) {
		int tabCount = individualsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (individualsPane.getToolTipTextAt(i).equals(cls.toString()))
				return true;
		
		return false;
	}
	
	private int indexOfTab(JTabbedPane individualsPane, URI cls) {
		int index = -1;
		
		int tabCount = individualsPane.getTabCount();
		
		for (int i = 0; i < tabCount; i++)
			if (individualsPane.getToolTipTextAt(i).equals(cls.toString())) {
				index = i;
				break;
			}
		
		return index;
	}
}
