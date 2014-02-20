package janus.ontology;

import janus.Janus;
import janus.mapping.OntMapper;

public enum OWLEntityTypes {
	DATA_PROPERTY(Janus.ontBridge.getOntologyID().toString() + "#" + OntMapper.DP_PREFIX + "[\\w[$]]+[\\.][\\w[$]]+"),   
	OBJECT_PROPERTY(Janus.ontBridge.getOntologyID().toString() + "#" + OntMapper.OP_PREFIX + "[\\w[$]]+[\\.][\\w[$]]+"),
	
	COLUMN_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+[\\.][\\w[$]]+"),
	OWL_THING(Janus.ontBridge.getOWLThingURI().toString()),
	TABLE_CLASS(Janus.ontBridge.getOntologyID().toString() + "#" + "[\\w[$]]+"),
	
	RECORD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "(([&]|([%][2][6]))" + "[k]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+)+"), 
	FIELD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[c]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+");
	
	private String pattern;
	
	private OWLEntityTypes(String pattern) {
		this.pattern = pattern;
	}
	
	public String pattern() {
		return pattern;
	}
}
