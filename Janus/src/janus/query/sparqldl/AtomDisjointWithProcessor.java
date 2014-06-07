package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;

class AtomDisjointWithProcessor {
	
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI cURI1 = URI.create(args.get(0).getValue());
		URI cURI2 = URI.create(args.get(1).getValue());
		
		boolean result = Janus.ontBridge.areDisjointWith(cURI1, cURI2);
		
		if (result) {
			System.out.println(atom.toString() + " is true.");
			return true;
		} else {
			System.out.println(atom.toString() + " is false.");
			return false;
		}
	}
	
	static URIResultSet execute1(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		String varName = null;
		URI cURI = null;
		
		for (QueryArgument arg: args)
			if (arg.getType().equals(QueryArgumentType.VAR))
				varName = arg.toString();
			else
				cURI = URI.create(arg.getValue());
		
		Set<URI> disjointClasses = new ConcurrentSkipListSet<URI>();
		
		if (Janus.ontBridge.containsClass(cURI))
			disjointClasses = Janus.ontBridge.getAllDisjointClasses(cURI);
		else {
			URI owlNothing = Janus.ontBridge.getOWLNothingURI();
			
			if (cURI.equals(owlNothing))
				disjointClasses = Janus.ontBridge.getAllDisjointClasses(cURI);
			else
				System.out.println("The class " + cURI + " in the " + atom.toString() + " is not asserted.");
		}
		
		URI[] URIs = disjointClasses.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
	
	static URIResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		Vector<String> varNames = new Vector<String>(2);
		
		varNames.addElement(args.get(0).toString());
		varNames.addElement(args.get(1).toString());
		
		Vector<Vector<URI>> rows = new Vector<Vector<URI>>();
		
		Set<URI> allClasses = new ConcurrentSkipListSet<URI>();

		URI owlThing = Janus.ontBridge.getOWLThingURI();
		URI owlNothing = Janus.ontBridge.getOWLNothingURI();
		
		allClasses.add(owlThing);
		allClasses.addAll(Janus.ontBridge.getAllSubClasses(owlThing));
		allClasses.remove(owlNothing);

		for (URI cls: allClasses) {
			Set<URI> disjointClasses = Janus.ontBridge.getAllDisjointClasses(cls);
			for (URI disjointCls: disjointClasses) {
				Vector<URI> row = new Vector<URI>();
				row.addElement(cls);
				row.addElement(disjointCls);
				rows.addElement(row);
			}
		}
		
		return new URIResultSet(rows, varNames);
	}

}
