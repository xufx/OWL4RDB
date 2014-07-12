package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;
import janus.application.query.QueryTypes;
import janus.application.query.Showable;
import janus.application.query.Submittable;
import janus.database.SQLResultSetTableModel;
import janus.query.sparqldl.SPARQLDLEngine;
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
		Janus.ontBridge.executeQuery(stmt);
		//<-using derivo
		
		SPARQLDLEngine queryEngine = new SPARQLDLEngine(stmt);
		
		janus.query.sparqldl.QueryTypes queryType = queryEngine.getQueryType();
		
		Showable displayer = UIRegistry.getQueryResultTable();
		
		if (queryType.equals(janus.query.sparqldl.QueryTypes.ASK))
			displayer.showResult(queryEngine.executeAskQuery());
		else if (queryType.equals(janus.query.sparqldl.QueryTypes.SELECT))
			displayer.showResult(queryEngine.executeSelectQuery());
		
		//long start = System.currentTimeMillis();
		
		//long end = System.currentTimeMillis();
		
		//System.out.println( "(Through OWL View for RDB) 질의 처리 시간 : " + ( end - start));	
	}
	
	private void submitSQL(Submittable submitter) {
		String stmt = submitter.getQuery();
		
		Showable displayer = UIRegistry.getQueryResultTable();
		
		displayer.showResult(new SQLResultSetTableModel(Janus.dbBridge.executeQuery(stmt)));
	}
	
	public String getToolTipText() {
		return NAME;
	}
}
