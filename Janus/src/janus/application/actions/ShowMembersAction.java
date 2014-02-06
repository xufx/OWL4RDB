package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;
import janus.application.ontscheme.ClsTree;
import janus.mapping.metadata.ClassTypes;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ShowMembersAction extends AbstractAction {
	
	public ShowMembersAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClsTree tree = UIRegistry.getClsTreePane();
		
		getQuery(tree.getSelectedClass());
	}
	
	private String getQuery(URI cls) {
		if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.TABLE_CLASS))
			;
		else
			;
		
		return null;
	}
}
