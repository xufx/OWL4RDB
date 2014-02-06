package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;
import janus.application.query.QueryTypes;
import janus.application.query.Showable;
import janus.application.query.Submittable;
import janus.sparqldl.SPARQLDLQueryEngine;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class RunAction extends AbstractAction {
	
	public void actionPerformed(ActionEvent e) {
		Submittable submitter = UIRegistry.getSelectedSubmittable();
		
		QueryTypes selectedSubmissionType = submitter.getQueryType();
		switch(selectedSubmissionType) {
			case SPARQL_DL:
				submitSPARQLDL(submitter);
				break;
			case SQL:
				submitSQL(submitter);
				break;
		}

		Showable displayer = UIRegistry.getShowable();
		
		displayer.showResult(selectedSubmissionType);
	}
	
	private void submitSPARQLDL(Submittable submitter) {
		String stmt = submitter.getQuery();
		
		SPARQLDLQueryEngine queryAgent = new SPARQLDLQueryEngine(stmt);
		
		//long start = System.currentTimeMillis();
		
		queryAgent.executeQuery();
		
		//long end = System.currentTimeMillis();
		
		//System.out.println( "(Through OWL View for RDB) 질의 처리 시간 : " + ( end - start));	
	}
	
	private void submitSQL(Submittable submitter) {
		String stmt = submitter.getQuery();
		Janus.dbBridge.executeQuery(stmt);
	}
}
