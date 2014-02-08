package janus.application.actions;

import janus.Janus;
import janus.TabNames;
import janus.application.UIRegistry;
import janus.application.dialog.DocTypeSelectionDialog;
import janus.ontology.RenderingType;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ShowDocumentAction extends AbstractAction {

	@Override
	public void actionPerformed(ActionEvent e) {
		DocTypeSelectionDialog dialog = new DocTypeSelectionDialog(UIRegistry.getDialogOwner());
		
		dialog.setVisible(true);
		
		if(dialog.isNormalExit()) {
			
			JTabbedPane documentPane = UIRegistry.getDocumentPane();
			
			if (dialog.whichButtonIsSelected().equals(DocTypeSelectionDialog.RDF_XML)) {
				documentPane.addTab(TabNames.RDF_XML, buildOWLRenderingPane(RenderingType.RDF_XML));
			} else {
				//tp.addTab(TabNames.FUNCTIONAL_SYNTAX, buildOWLRenderingPane(RenderingType.FUNCTIONAL_SYNTAX));
			}
		}
	}
	
	private JScrollPane buildOWLRenderingPane(RenderingType renderingType) {
		JScrollPane sp = new JScrollPane();
		sp.setViewportView(new JTextArea(Janus.ontBridge.dumpTBoxOntology(renderingType)));
		
		return sp;
	}
}
