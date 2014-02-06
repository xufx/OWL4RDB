package janus.application.description;

enum PropChars {
	FUNCTIONAL("Functional"),
	INVERSE_FUNCTIONAL("Inverse functional"),
	TRANSITIVE("Transitive"),
	SYMMETRIC("Symmetric"),
	ASYMMETRIC("Asymmetric"),
	REFLEXIVE("Reflexive"),
	IRREFLEXIVE("Irreflexive");
	
	private final String charName;
	
	private PropChars(String charName) {
		this.charName = charName;
	}
	
	public String toString() { return charName; }
}
