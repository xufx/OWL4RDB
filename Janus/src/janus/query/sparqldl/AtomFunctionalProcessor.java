package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;

class AtomFunctionalProcessor {
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg = args.get(0);
			
		URI pURI = URI.create(arg.getValue());
			
		boolean result = false;
			
		if (Janus.ontBridge.containsDataProperty(pURI))
			result = Janus.ontBridge.isFunctionalDataProp(pURI);
		else
			result = Janus.ontBridge.isFunctionalObjProp(pURI);
			
		if (!result) {
			System.out.println(atom.toString() + " is false.");
			return false;
		} else {
			System.out.println(atom.toString() + " is true.");
			return true;
		}
	}
	
	static URIResultSet execute1(QueryAtom atom, Variable var) {
		Set<URI> functionalProps = new ConcurrentSkipListSet<URI>();
		
		if (var.getType().equals(VariableTypes.OBJECT_PROPERTIES))
			functionalProps = Janus.ontBridge.getAllFunctionalObjProps();
		else if (var.getType().equals(VariableTypes.DATA_PROPERTIES))
			functionalProps = Janus.ontBridge.getAllFunctionalDataProps();
		else if (var.getType().equals(VariableTypes.PROPERTIES)) {
			functionalProps.addAll(Janus.ontBridge.getAllFunctionalObjProps());
			functionalProps.addAll(Janus.ontBridge.getAllFunctionalDataProps());
		}
		
		URI[] URIs = functionalProps.toArray(new URI[0]);
		
		return new URIResultSet(var.getName(), URIs);
	}
}
