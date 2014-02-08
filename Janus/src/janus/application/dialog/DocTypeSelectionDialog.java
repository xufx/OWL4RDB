package janus.application.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

@SuppressWarnings("serial")
public class DocTypeSelectionDialog extends JDialog {
	
	public static final String RDF_XML = "RDF/XML";
	public static final String FUNCTIONAL_SYNTAX = "Functional Syntax";
	
	private JRadioButton RDFXML;
	private JRadioButton functionalSyntax;
	
	private boolean NORMAL_EXIT = false;

	public DocTypeSelectionDialog(Window owner) {
		super(owner, "Choose Document Type", Dialog.ModalityType.DOCUMENT_MODAL);

		buildUI();
	}

	private void buildUI() {
		JPanel basePanel = new JPanel();
		setContentPane(basePanel);
		basePanel.setLayout(new BorderLayout());
		
		// centerPanel
		JPanel centerPanel = new JPanel();
		
		ButtonGroup bg = new ButtonGroup();
		
		RDFXML = new JRadioButton("RDF/XML", true);
		functionalSyntax = new JRadioButton("Functional Syntax");
		
		bg.add(RDFXML);
		bg.add(functionalSyntax);
		
		centerPanel.add(RDFXML);
		centerPanel.add(functionalSyntax);
		
		basePanel.add(centerPanel, BorderLayout.CENTER);
		
		// southPanel
		JPanel southPanel = new JPanel();
		
		// OK
		JButton OK = new JButton("OK");
		OK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NORMAL_EXIT = true;
				setVisible(false);
			}
		});
		southPanel.add(OK);

		// Cancel
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		southPanel.add(cancel);
		
		basePanel.add(southPanel, BorderLayout.SOUTH);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 300) >> 1, (screenSize.height- 100) >> 1, 300, 100);
		
		setResizable(false);
	}
	
	public String whichButtonIsSelected() {
		if (RDFXML.isSelected())
			return RDF_XML;
		else
			return FUNCTIONAL_SYNTAX;
	}
	
	public boolean isNormalExit() { return NORMAL_EXIT; }
	
	public void setVisible(boolean b) {
		if(b) NORMAL_EXIT = false;
		
		super.setVisible(b);
	}
}