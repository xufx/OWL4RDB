package janus.mapping;

import janus.Janus;

public enum ClassTypes {
	COLUMN_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+[\\.][\\w[$]]+"),
	OWL_THING(Janus.ontBridge.getOWLThingURI().toString()),
	TABLE_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+");
	
	private String pattern;
	
	private ClassTypes(String pattern) {
		this.pattern = pattern;
	}
	
	public String pattern() {
		return pattern;
	}
}
