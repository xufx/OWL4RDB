package janus.application.query;

import janus.Janus;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class QueryArea extends JScrollPane implements Submittable {
	private QueryTypes submissionType;
	private JTextArea text;
	
	public QueryArea(QueryTypes submissionType) {
		this.submissionType = submissionType;
		buildUI();
	}
	
	private void buildUI() {
		text = new JTextArea();
		
		initText();
		
		setViewportView(text);
	}
	
	private void initText() {
		if (submissionType.equals(QueryTypes.SPARQL_DL)) {
			String prefix = "PREFIX : <" +  Janus.ontBridge.getOntologyID() + "#>\n\n";
			String select = "SELECT [DISTINCT] [space-separated list of variables]\n[WHERE] { [comma-separated list of atoms] }\n\n";
			String ask = "ASK { [comma-separated list of atoms] }";
			
			text.setText(prefix + select + prefix + ask);
		}
	}
	
	public String getQuery() {
		return text.getSelectedText();
	}
	
	public QueryTypes getQueryType() {
		return submissionType;
	}
}