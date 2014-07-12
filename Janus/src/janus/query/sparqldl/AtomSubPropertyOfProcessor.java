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

class AtomSubPropertyOfProcessor {
	static boolean execute0(QueryAtom atom) {
		List<QueryArgument> args = atom.getArguments();
		
		URI pURI1 = URI.create(args.get(0).getValue());
		URI pURI2 = URI.create(args.get(1).getValue());
		
		boolean result = false;
		
		if (Janus.ontBridge.containsDataProperty(pURI1))
			result = Janus.ontBridge.isSubDataPropertyOf(pURI1, pURI2);
		else 
			result = Janus.ontBridge.isSubObjectPropertyOf(pURI1, pURI2);
			
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
		
		QueryArgument arg1 = args.get(0);
		QueryArgument arg2 = args.get(1);
		
		String varName = null;
		URI pURI = null;
		
		if (arg1.getType().equals(QueryArgumentType.VAR)) {
			varName = arg1.getValue();
			pURI = URI.create(arg2.getValue());
		}
		else {
			varName = arg2.getValue();
			pURI = URI.create(arg1.getValue());
		}
		
		Set<URI> properties = new ConcurrentSkipListSet<URI>();
		
		if (Janus.ontBridge.containsObjectProperty(pURI)) {
			var.setType(VariableTypes.OBJECT_PROPERTIES);
			
			if (arg1.getType().equals(QueryArgumentType.VAR))
				properties = Janus.ontBridge.getAllSubObjProps(pURI);
			else
				properties = Janus.ontBridge.getAllSuperObjProps(pURI);
		}
		
		else if (Janus.ontBridge.containsDataProperty(pURI)) {
			var.setType(VariableTypes.DATA_PROPERTIES);
			
			if (arg1.getType().equals(QueryArgumentType.VAR))
				properties = Janus.ontBridge.getAllSubDataProps(pURI);
			else
				properties = Janus.ontBridge.getAllSuperDataProps(pURI);
		}
		
		URI[] URIs = properties.toArray(new URI[0]);
		
		return new URIResultSet(varName, URIs);
	}
	
	static URIResultSet execute2(QueryAtom atom, Variable var1, Variable var2) {
		List<QueryArgument> args = atom.getArguments();
		
		Vector<String> varNames = new Vector<String>(2);
		
		varNames.addElement(args.get(0).getValue());
		varNames.addElement(args.get(1).getValue());
		
		if (!var1.getType().equals(var2.getType()))
			return new URIResultSet();
		
		VariableTypes varType = var1.getType();
		
		Vector<Vector<URI>> rows = new Vector<Vector<URI>>();
				
		if (varType.equals(VariableTypes.PROPERTIES)
				|| varType.equals(VariableTypes.OBJECT_PROPERTIES)) {
			
			Set<URI> allOPs = Janus.ontBridge.getAllObjProps();

			for (URI op: allOPs) {
				Set<URI> subOPs = Janus.ontBridge.getAllSubObjProps(op);
				for (URI aSub: subOPs) {
					Vector<URI> row = new Vector<URI>();
					row.addElement(aSub);
					row.addElement(op);
					rows.addElement(row);
				}
			}
		}
		
		if (varType.equals(VariableTypes.PROPERTIES)
				|| varType.equals(VariableTypes.DATA_PROPERTIES)) {
			
			Set<URI> allDPs = Janus.ontBridge.getAllDataProps();

			for (URI dp: allDPs) {
				Set<URI> subDPs = Janus.ontBridge.getAllSubDataProps(dp);
				for (URI aSub: subDPs) {
					Vector<URI> row = new Vector<URI>();
					row.addElement(aSub);
					row.addElement(dp);
					rows.addElement(row);
				}
			}
		}
		
		return new URIResultSet(rows, varNames);
	}
}
