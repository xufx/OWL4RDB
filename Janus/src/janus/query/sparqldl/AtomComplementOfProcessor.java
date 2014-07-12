package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;

class AtomComplementOfProcessor {
	
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI cURI1 = URI.create(args.get(0).getValue());
		URI cURI2 = URI.create(args.get(1).getValue());
		
		URI owlThing = Janus.ontBridge.getOWLThingURI();
		URI owlNothing = Janus.ontBridge.getOWLNothingURI();
		
		if ((cURI1.equals(owlThing) && cURI2.equals(owlNothing)) || (cURI1.equals(owlNothing) && cURI2.equals(owlThing))) {
			System.out.println(atom.toString() + " is true.");
			return true;
		} else {
			System.out.println(atom.toString() + " is false.");
			return false;
		}
	}
	
	static URIResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		String varName = null;
		URI cURI = null;
		
		if (arg1.getType().equals(QueryArgumentType.VAR)) {
			varName = arg1.getValue();
			cURI = URI.create(arg2.getValue());
		}
		else {
			varName = arg2.getValue();
			cURI = URI.create(arg1.getValue());
		}
		
		URI owlThing = Janus.ontBridge.getOWLThingURI();
		URI owlNothing = Janus.ontBridge.getOWLNothingURI();
		
		Set<URI> complementClasses = new ConcurrentSkipListSet<URI>();
		
		if (cURI.equals(owlThing))
			complementClasses.add(owlNothing);
		else if (cURI.equals(owlNothing))
			complementClasses.add(owlThing);
		else if (!Janus.ontBridge.containsClass(cURI))
			System.out.println("The class " + cURI + " in the " + atom.toString() + " is not asserted.");
		
		URI[] URIs = complementClasses.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
	
	static URIResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		String varName1 = args.get(0).getValue();
		String varName2 = args.get(1).getValue();
		
		URI owlThing = Janus.ontBridge.getOWLThingURI();
		URI owlNothing = Janus.ontBridge.getOWLNothingURI();
		
		URI[][] aPairOfcomplementClass = { {owlThing, owlNothing}, {owlNothing, owlThing} };
		String[] varNames = {varName1, varName2};
		
		return new URIResultSet(aPairOfcomplementClass, varNames); 
	}

}
