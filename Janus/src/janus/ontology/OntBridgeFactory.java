package janus.ontology;

import java.io.File;

public class OntBridgeFactory {
	public static OntBridge getOntBridge(String ontURI, ReasonerType reasonerType) {
		OntBridge ontBridge = OWLAPIBridge.getInstance(ontURI, reasonerType);
		
		return ontBridge;
	}
	
	public static OntBridge getOntBridge(File ontFile, ReasonerType reasonerType) {
		OntBridge ontBridge = OWLAPIBridge.getInstance(ontFile, reasonerType);
		
		return ontBridge;
	}
}
