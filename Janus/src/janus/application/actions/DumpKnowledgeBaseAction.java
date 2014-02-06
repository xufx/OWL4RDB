package janus.application.actions;

import janus.Janus;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class DumpKnowledgeBaseAction extends AbstractAction {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Janus.ontMapper.dumpKB();
		
		JOptionPane.showMessageDialog((Component)e.getSource(), "The knowledge base dump has finished.");
	}

}
