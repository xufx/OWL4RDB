package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class ShowAtomsAction extends AbstractAction {
	private static final String NAME = "Show Atoms";
	
	private Set<String> SPARQLDLAtoms;
	
	public void actionPerformed(ActionEvent e) {
		if(SPARQLDLAtoms == null) {
			SPARQLDLAtoms = Janus.ontBridge.getSPARQLDLAtoms();
			
			addArguments();
		}
		
		AbstractButton btn = (AbstractButton)e.getSource();
		
		buildDialog(btn);
		
		btn.setEnabled(false);
	}
	
	private void addArguments() {
		Set<String> atomsAddedArguments = new ConcurrentSkipListSet<String>();
		
		for (String atom : SPARQLDLAtoms) {
			String atomAddedArguments = atom;
			if (atom.equals("Annotation"))
				atomAddedArguments = atom + "(b1, r, b2)";
			else if (atom.equals("ComplementOf") || atom.equals("DisjointWith")
					|| atom.equals("EquivalentClass")
					|| atom.equals("SubClassOf"))
				atomAddedArguments = atom + "(C1, C2)";
			else if (atom.equals("DataProperty") || atom.equals("Functional")
					|| atom.equals("InverseFunctional")
					|| atom.equals("ObjectProperty")
					|| atom.equals("Symmetric") || atom.equals("Transitive"))
				atomAddedArguments = atom + "(p)";
			else if (atom.equals("DifferentFrom") || atom.equals("SameAs"))
				atomAddedArguments = atom + "(a1, a2)";
			else if (atom.equals("EquivalentProperty")
					|| atom.equals("SubPropertyOf")
					|| atom.equals("InverseOf"))
				atomAddedArguments = atom + "(p1, p2)";
			else if (atom.equals("PropertyValue"))
				atomAddedArguments = atom + "(a, p, d)";
			else if (atom.equals("Type"))
				atomAddedArguments = atom + "(a, C)";
			else
				atomAddedArguments = null;

			if (atomAddedArguments != null)
				atomsAddedArguments.add(atomAddedArguments);
		}
		
		SPARQLDLAtoms = atomsAddedArguments;
	}
	
	private void buildDialog(final AbstractButton btn) {
		Vector<String> v = new Vector<String>(SPARQLDLAtoms.size());
		for(String atom : SPARQLDLAtoms)
			v.addElement(atom);
		
		JList<String> list = new JList<String>(v);
		list.setDragEnabled(true);
		
		JScrollPane sp = new JScrollPane(list);
		
		JDialog dialog = new JDialog(UIRegistry.getDialogOwner(), "SPARQL-DL Atoms");
		
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				btn.setEnabled(true);
			}
		});
		
		dialog.add(sp);
		dialog.setAlwaysOnTop(true);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = screenSize.width / 6;
		int h = screenSize.height / 3;
		dialog.setBounds((screenSize.width - w) >> 1, (screenSize.height - h) >> 1, w, h);
		
		dialog.setVisible(true);
	}
	
	public String getToolTipText() {
		return NAME;
	}
}
