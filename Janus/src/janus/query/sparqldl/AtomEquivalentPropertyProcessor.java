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

class AtomEquivalentPropertyProcessor {
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI pURI1 = URI.create(args.get(0).getValue());
		URI pURI2 = URI.create(args.get(1).getValue());
		
		boolean result = false;
		
		if (Janus.ontBridge.containsDataProperty(pURI1))
			result = Janus.ontBridge.isEquivalentDataPropertyOf(pURI1, pURI2);
		else 
			result = Janus.ontBridge.isEquivalentObjectPropertyOf(pURI1, pURI2);
			
		if (!result) {
			System.out.println(atom.toString() + " is false.");
			return false;
		} else {
			System.out.println(atom.toString() + " is true.");
			return true;
		}
	}
	
	static URIResultSet execute1(QueryAtom atom, Variable var) {
		List<QueryArgument> args = atom.getArguments();
		
		String varName = null;
		URI pURI = null;
		
		for (QueryArgument arg: args)
			if (arg.getType().equals(QueryArgumentType.VAR))
				varName = arg.toString();
			else
				pURI = URI.create(arg.getValue());
		
		Set<URI> equivalentProps = new ConcurrentSkipListSet<URI>();
		
		if (Janus.ontBridge.containsObjectProperty(pURI)) {
			
			var.setType(VariableTypes.OBJECT_PROPERTIES);
			
			equivalentProps = Janus.ontBridge.getEquivalentObjectProperties(pURI);
		
		} else if (Janus.ontBridge.containsDataProperty(pURI)) {
			
			var.setType(VariableTypes.DATA_PROPERTIES);
			
			equivalentProps = Janus.ontBridge.getEquivalentDataProperties(pURI);
			
		}
		
		URI[] URIs = equivalentProps.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
	
	static URIResultSet execute2(QueryAtom atom, Variable var1, Variable var2) {
		List<QueryArgument> args = atom.getArguments();
		
		Vector<String> varNames = new Vector<String>(2);
		
		varNames.addElement(args.get(0).toString());
		varNames.addElement(args.get(1).toString());
		
		if (!var1.getType().equals(var2.getType()))
			return new URIResultSet();
		
		VariableTypes varType = var1.getType();
		
		Vector<Vector<URI>> rows = new Vector<Vector<URI>>();
				
		if (varType.equals(VariableTypes.PROPERTIES)
				|| varType.equals(VariableTypes.OBJECT_PROPERTIES)) {
			
			Set<URI> allOPs = Janus.ontBridge.getAllObjProps();
			
			URI owlBottomOP = Janus.ontBridge.getOWLBottomObjectProperty();
			allOPs.remove(owlBottomOP);

			for (URI op: allOPs) {
				Set<URI> equivalentOPs = Janus.ontBridge.getEquivalentObjectProperties(op);
				for (URI anEqui: equivalentOPs) {
					Vector<URI> row = new Vector<URI>();
					row.addElement(op);
					row.addElement(anEqui);
					rows.addElement(row);
				}
			}
		}
		
		if (varType.equals(VariableTypes.PROPERTIES)
				|| varType.equals(VariableTypes.DATA_PROPERTIES)) {
			
			Set<URI> allDPs = Janus.ontBridge.getAllDataProps();
			
			URI owlBottomDP = Janus.ontBridge.getOWLBottomDataProperty();
			allDPs.remove(owlBottomDP);

			for (URI dp: allDPs) {
				Set<URI> equivalentDPs = Janus.ontBridge.getEquivalentDataProperties(dp);
				for (URI anEqui: equivalentDPs) {
					Vector<URI> row = new Vector<URI>();
					row.addElement(dp);
					row.addElement(anEqui);
					rows.addElement(row);
				}
			}
		}
		
		return new URIResultSet(rows, varNames);
	}
}
