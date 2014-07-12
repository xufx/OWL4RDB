package janus.query.sparqldl;

import janus.Janus;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;

class AtomInverseOfProcessor {
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI opURI1 = URI.create(args.get(0).getValue());
		URI opURI2 = URI.create(args.get(1).getValue());
		
		boolean result = Janus.ontBridge.isInverseObjectPropertyOf(opURI1, opURI2);
		
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
		
		String varName = null;
		URI opURI = null;
		
		for (QueryArgument arg: args)
			if (arg.getType().equals(QueryArgumentType.VAR))
				varName = arg.getValue();
			else
				opURI = URI.create(arg.getValue());
		
		Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(opURI);
		
		URI[] URIs = inverseObjProps.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
	
	static URIResultSet execute2(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		Vector<String> varNames = new Vector<String>(2);
		
		varNames.addElement(args.get(0).getValue());
		varNames.addElement(args.get(1).getValue());
		
		Vector<Vector<URI>> rows = new Vector<Vector<URI>>();
				
		Set<URI> allObjProperties = Janus.ontBridge.getAllObjProps();

		for (URI op: allObjProperties) {
			Set<URI> inverseObjProps = Janus.ontBridge.getInverseObjectProperties(op);
			for (URI anInverse: inverseObjProps) {
				Vector<URI> row = new Vector<URI>();
				row.addElement(anInverse);
				row.addElement(op);
				rows.addElement(row);
			}
		}

		return new URIResultSet(rows, varNames);
	}
}
