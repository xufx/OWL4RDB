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
	private static final String NAME = "Show Document";

	@Override
	public void actionPerformed(ActionEvent e) {
		DocTypeSelectionDialog dialog = new DocTypeSelectionDialog(UIRegistry.getDialogOwner());
		
		dialog.setVisible(true);
		
		if(dialog.isNormalExit()) {
			
			JTabbedPane displayPane = UIRegistry.getDisplayTab();
			displayPane.setSelectedIndex(displayPane.indexOfTab(TabNames.DOCUMENT));
			
			JTabbedPane documentPane = UIRegistry.getDocumentTab();
			
			if (dialog.whichButtonIsSelected().equals(DocTypeSelectionDialog.RDF_XML)) {
				
				if (documentPane.indexOfTab(TabNames.RDF_XML) == -1)
					documentPane.addTab(TabNames.RDF_XML, buildOWLRenderingPane(RenderingType.RDF_XML));
				
				documentPane.setSelectedIndex(documentPane.indexOfTab(TabNames.RDF_XML));
				
			} else {
				
				if (documentPane.indexOfTab(TabNames.FUNCTIONAL_SYNTAX) == -1)
					documentPane.addTab(TabNames.FUNCTIONAL_SYNTAX, buildOWLRenderingPane(RenderingType.FUNCTIONAL_SYNTAX));
				
				documentPane.setSelectedIndex(documentPane.indexOfTab(TabNames.FUNCTIONAL_SYNTAX));
			}
		}
	}
	
	private JScrollPane buildOWLRenderingPane(RenderingType renderingType) {
		JScrollPane sp = new JScrollPane();
		JTextArea ta = new JTextArea(Janus.ontBridge.dumpTBoxOntology(renderingType));
		ta.setEditable(false);
		sp.setViewportView(ta);
		
		return sp;
	}
	
	public String getToolTipText() {
		return NAME;
	}
}
