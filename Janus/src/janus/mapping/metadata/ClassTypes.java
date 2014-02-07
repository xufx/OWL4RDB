package janus.mapping.metadata;

import janus.Janus;

public enum ClassTypes {
	COLUMN_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+[\\.][\\w[$]]+"),
	OWL_THING(Janus.ontBridge.getOWLThingURI().toString()),
	TABLE_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+");
	
	private String value;
	
	private ClassTypes(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}
