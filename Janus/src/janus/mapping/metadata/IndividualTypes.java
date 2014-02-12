package janus.mapping.metadata;

import janus.Janus;

public enum IndividualTypes {
	RECORD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t][=]" + "[\\w[$]]+" + "([&]" + "[k][=]" + "[\\w[$]]+" + "[&]" + "[v][=]" + "[\\p{Graph}]+)+"), 
	FIELD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t][=]" + "[\\w[$]]+" + "[&]" + "[c][=]" + "[\\w[$]]+" + "[&]" + "[v][=]" + "[\\p{Graph}]+");
	
	private String value;
	
	private IndividualTypes(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}
