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

public class AtomSubClassOfProcessor {
	
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI cURI1 = URI.create(args.get(0).getValue());
		URI cURI2 = URI.create(args.get(1).getValue());
		
		if (Janus.ontBridge.isEquivalentClass(cURI1, cURI2)) {
			System.out.println(atom.toString() + " is true.");
			return true;
		}
		
		boolean result = Janus.ontBridge.isSubClassOf(cURI1, cURI2);
		
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
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		String varName = null;
		URI cURI = null;
		
		if (arg1.getType().equals(QueryArgumentType.VAR)) {
			varName = arg1.toString();
			cURI = URI.create(arg2.getValue());
		}
		else {
			varName = arg2.toString();
			cURI = URI.create(arg1.getValue());
		}
		
		Set<URI> classes = new ConcurrentSkipListSet<URI>();
		
		if (Janus.ontBridge.containsClass(cURI)) {
			if (arg1.getType().equals(QueryArgumentType.VAR))
				classes = Janus.ontBridge.getAllSubClasses(cURI);
			else
				classes = Janus.ontBridge.getAllSuperClasses(cURI);
				
		} else {
			URI owlNothing = Janus.ontBridge.getOWLNothingURI();
			
			if (cURI.equals(owlNothing)) {
				if (arg1.getType().equals(QueryArgumentType.VAR))
					classes = Janus.ontBridge.getAllSubClasses(cURI);
				else
					classes = Janus.ontBridge.getAllSuperClasses(cURI);
			} else
				System.out.println("The class " + cURI + " in the " + atom.toString() + " is not asserted.");
		}
		
		URI[] URIs = classes.toArray(new URI[0]);
		
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
		//URI owlNothing = Janus.ontBridge.getOWLNothingURI();
		
		allClasses.add(owlThing);
		allClasses.addAll(Janus.ontBridge.getAllSubClasses(owlThing));

		for (URI cls: allClasses) {
			Set<URI> subClasses = Janus.ontBridge.getAllSubClasses(cls);
			for (URI subCls: subClasses) {
				//if (!cls.equals(subCls) 
				//		&& !subCls.equals(owlNothing)) {
					Vector<URI> row = new Vector<URI>();
					row.addElement(subCls);
					row.addElement(cls);
					rows.addElement(row);
				//}
			}
		}

		return new URIResultSet(rows, varNames);
	}

}
