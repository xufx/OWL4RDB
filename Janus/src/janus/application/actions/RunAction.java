package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;
import janus.application.query.QueryTypes;
import janus.application.query.Showable;
import janus.application.query.Submittable;
import janus.query.sparqldl.QueryEngine;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class RunAction extends AbstractAction {
	private static final String NAME = "Run Query";
	
	public void actionPerformed(ActionEvent e) {
		Submittable submitter = UIRegistry.getSelectedQueryArea();
		
		QueryTypes selectedSubmissionType = submitter.getQueryType();
		switch(selectedSubmissionType) {
			case SPARQL_DL:
				submitSPARQLDL(submitter);
				break;
			case SQL:
				submitSQL(submitter);
				break;
		}

//		Showable displayer = UIRegistry.getQueryResultTable();
//		
//		displayer.showResult(selectedSubmissionType);
	}
	
	private void submitSPARQLDL(Submittable submitter) {
		String stmt = submitter.getQuery();
		
		//->using derivo
		//SPARQLDLQueryEngine queryAgent = new SPARQLDLQueryEngine(stmt);
		//queryAgent.executeQuery();
		//<-using derivo
		
		QueryEngine queryEngine = new QueryEngine(stmt);
		
		janus.query.sparqldl.QueryTypes queryType = queryEngine.getQueryType();
		
		if (queryType.equals(janus.query.sparqldl.QueryTypes.ASK)) {
			boolean result = queryEngine.executeAskQuery();
			
			Showable displayer = UIRegistry.getQueryResultTable();
			
			displayer.showResult(result);
		}
		
		//long start = System.currentTimeMillis();
		
		//long end = System.currentTimeMillis();
		
		//System.out.println( "(Through OWL View for RDB) 질의 처리 시간 : " + ( end - start));	
	}
	
	private void submitSQL(Submittable submitter) {
		String stmt = submitter.getQuery();
		Janus.dbBridge.executeQuery(stmt);
		
		Showable displayer = UIRegistry.getQueryResultTable();
		
		displayer.showResult(QueryTypes.SPARQL_DL);
	}
	
	public String getToolTipText() {
		return NAME;
	}
}
