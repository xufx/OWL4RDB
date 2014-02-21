package janus.mapping;

import janus.Janus;

public enum OntEntityTypes {
	OWL_TOP_DATA_PROPERTY(Janus.ontBridge.getOWLTopDataProperty().toString()),
	DATA_PROPERTY(Janus.ontBridge.getOntologyID().toString() + "#" + OntMapper.DP_PREFIX + "[\\w[$]]+[\\.][\\w[$]]+"),   
	
	OWL_TOP_OBJECT_PROPERTY(Janus.ontBridge.getOWLTopObjectProperty().toString()),
	OBJECT_PROPERTY(Janus.ontBridge.getOntologyID().toString() + "#" + OntMapper.OP_PREFIX + "[\\w[$]]+[\\.][\\w[$]]+"),
	
	COLUMN_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+[\\.][\\w[$]]+"),
	OWL_THING_CLASS(Janus.ontBridge.getOWLThingURI().toString()),
	TABLE_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+"),
	
	RECORD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "(([&]|([%][2][6]))" + "[k]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+)+"), 
	FIELD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[c]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+");
	
	private String pattern;
	
	private OntEntityTypes(String pattern) {
		this.pattern = pattern;
	}
	
	public String pattern() {
		return pattern;
	}
}
