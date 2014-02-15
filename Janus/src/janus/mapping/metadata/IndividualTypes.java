package janus.mapping.metadata;

import janus.Janus;

public enum IndividualTypes {
	RECORD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "(([&]|([%][2][6]))" + "[k]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+)+"), 
	FIELD_INDIVIDUAL(Janus.ontBridge.getOntologyID().toString() + "#" + "[t]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[c]([=]|([%][3][D]))" + "[\\w[$]]+" + "([&]|([%][2][6]))" + "[v]([=]|([%][3][D]))" + "[\\p{Print}]+");
	
	private String value;
	
	private IndividualTypes(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}
}
