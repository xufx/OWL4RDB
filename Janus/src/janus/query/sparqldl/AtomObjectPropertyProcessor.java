package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Set;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;

class AtomObjectPropertyProcessor {
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg = args.get(0);
		
		URI opURI = URI.create(arg.getValue());
		
		boolean result = Janus.ontBridge.containsObjectProperty(opURI);
		
		if (!result) {
			System.out.println(atom.toString() + " is false.");
			return false;
		} else {
			System.out.println(atom.toString() + " is true.");
			return true;
		}
	}
	
	static URIResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg = args.get(0);
		
		String varName = arg.getValue();
		
		Set<URI> objProps = Janus.ontBridge.getAllObjProps();
		
		URI[] URIs = objProps.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
}