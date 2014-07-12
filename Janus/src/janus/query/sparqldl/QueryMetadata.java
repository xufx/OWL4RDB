package janus.query.sparqldl;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.QueryAtomGroup;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import de.derivo.sparqldlapi.types.QueryAtomType;
import de.derivo.sparqldlapi.types.QueryType;

class QueryMetadata {
	private String queryString;
	
	private Query queryObject;
	
	private QueryTypes queryType;
	
	private List<QueryAtom> A0BoxAtoms, A1BoxAtoms, A2BoxAtoms, A3BoxAtoms, T0BoxAtoms, T1BoxAtoms, T2BoxAtoms, R0BoxAtoms, R1BoxAtoms, R2BoxAtoms; // The number means the count of variables.
	
	private Map<String, Variable> variables;
	
	QueryMetadata(String query) {
		queryString = query;
		
		queryObject = createQueryObject(query);
		
		seperateAtoms();
		
		variables = new Hashtable<String, Variable>();
	}
	
	List<String> getResultVariables() {
		List<String> resultVars = new Vector<String>();
		
		int beginIndex = queryString.indexOf("SELECT") + 7;
		int endIndex = queryString.indexOf("WHERE") - 1;
		
		if (beginIndex < 0 || endIndex < 0 || endIndex - beginIndex <= 0)
			return resultVars;
		
		do {
			beginIndex = queryString.indexOf("?", beginIndex);
			int nextVarIndex = queryString.indexOf("?", beginIndex + 1);
			
			if (nextVarIndex < endIndex) {
				resultVars.add(queryString.substring(beginIndex + 1, nextVarIndex).trim());
				beginIndex = nextVarIndex;
			} else {
				resultVars.add(queryString.substring(beginIndex + 1, endIndex).trim());
				break;
			}
				
		} while(true);
		
		return resultVars;
	}
	
	Variable getVariable(String variable) {
		return variables.get(variable);
	}
	
	private Query createQueryObject(String query) {
		de.derivo.sparqldlapi.Query queryObject = null;
		
		try {
			queryObject = de.derivo.sparqldlapi.Query.create(query);
		} catch (QueryParserException e) {
			e.printStackTrace();
		}
		
		return queryObject;
	}
	
	QueryTypes getQueryType() {
		if (queryType != null)
			return queryType;
		
		if (queryObject.getType().equals(QueryType.ASK))
			queryType = QueryTypes.ASK;
		else if (queryObject.getType().equals(QueryType.SELECT))
			queryType = QueryTypes.SELECT;
		else
			queryType = QueryTypes.NOT_SUPPORTED_TYPE;
		
		return queryType;
	}
	
	boolean isEmptyQuery() {
		return queryObject.isEmpty();
	}
	
	boolean hasAnnotationAtoms() {
		List<QueryAtomGroup> groups = queryObject.getAtomGroups();
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms)
				if (atom.getType().equals(QueryAtomType.ANNOTATION))
					return true;
		}
		
		return false;
	}
	
	static boolean isTBoxAtom(QueryAtom atom) {
		QueryAtomType type = atom.getType();
		if (type.equals(QueryAtomType.COMPLEMENT_OF)
				|| type.equals(QueryAtomType.DISJOINT_WITH)
				|| type.equals(QueryAtomType.EQUIVALENT_CLASS)
				|| type.equals(QueryAtomType.SUB_CLASS_OF))
			return true;
		
		return false;
	}
	
	// returns TBox atoms which have no variable. 
	List<QueryAtom> getT0BoxAtoms() {
		return T0BoxAtoms;
	}
	
	List<QueryAtom> getT1BoxAtoms() {
		return T1BoxAtoms;
	}
	
	List<QueryAtom> getT2BoxAtoms() {
		return T2BoxAtoms;
	}
	
	List<QueryAtom> getT1T2BoxAtoms() {
		List<QueryAtom> T1T2BoxAtoms = new Vector<QueryAtom>(T1BoxAtoms);
		T1T2BoxAtoms.addAll(T2BoxAtoms);
		
		return T1T2BoxAtoms;
	}
	
	static boolean isRBoxAtom(QueryAtom atom) {
		QueryAtomType type = atom.getType();
		if (type.equals(QueryAtomType.DATA_PROPERTY)
				|| type.equals(QueryAtomType.FUNCTIONAL)
				|| type.equals(QueryAtomType.INVERSE_FUNCTIONAL)
				|| type.equals(QueryAtomType.OBJECT_PROPERTY)
				|| type.equals(QueryAtomType.SYMMETRIC)
				|| type.equals(QueryAtomType.TRANSITIVE)
				|| type.equals(QueryAtomType.INVERSE_OF)
				|| type.equals(QueryAtomType.EQUIVALENT_PROPERTY)
				|| type.equals(QueryAtomType.SUB_PROPERTY_OF))
			return true;
		
		return false;
	}
	
	// returns RBox atoms which have no variable. 
	List<QueryAtom> getR0BoxAtoms() {
		return R0BoxAtoms;
	}
	
	List<QueryAtom> getR1BoxAtoms() {
		return R1BoxAtoms;
	}
	
	List<QueryAtom> getR2BoxAtoms() {
		return R2BoxAtoms;
	}
	
	List<QueryAtom> getA1BoxAtoms() {
		return A1BoxAtoms;
	}
	
	List<QueryAtom> getA2BoxAtoms() {
		return A2BoxAtoms;
	}
	
	List<QueryAtom> getA3BoxAtoms() {
		return A3BoxAtoms;
	}
	
	static boolean isABoxAtom(QueryAtom atom) {
		QueryAtomType type = atom.getType();
		if (type.equals(QueryAtomType.DIFFERENT_FROM)
				|| type.equals(QueryAtomType.PROPERTY_VALUE)
				|| type.equals(QueryAtomType.SAME_AS)
				|| type.equals(QueryAtomType.TYPE))
			return true;
		
		return false;
	}
	
	// returns ABox atoms which have no variable. 
	List<QueryAtom> getA0BoxAtoms() {
		return A0BoxAtoms;
	}
	
	boolean hasABoxAtoms() {
		List<QueryAtomGroup> groups = queryObject.getAtomGroups();
		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms)
				if (isABoxAtom(atom))
					return true;
		}
		
		return false;
	}
	
	List<QueryAtom> getGT0Atoms() {
		List<QueryAtom> allAtoms = new Vector<QueryAtom>();
		
		allAtoms.addAll(T1BoxAtoms);
		allAtoms.addAll(R1BoxAtoms);
		allAtoms.addAll(T2BoxAtoms);
		allAtoms.addAll(R2BoxAtoms);
		allAtoms.addAll(A1BoxAtoms);
		allAtoms.addAll(A2BoxAtoms);
		allAtoms.addAll(A3BoxAtoms);
				
		return allAtoms;
	}
	
	private void seperateAtoms() {
		
		A0BoxAtoms = new Vector<QueryAtom>();
		A1BoxAtoms = new Vector<QueryAtom>();
		A2BoxAtoms = new Vector<QueryAtom>();
		A3BoxAtoms = new Vector<QueryAtom>();
		T0BoxAtoms = new Vector<QueryAtom>();
		T1BoxAtoms = new Vector<QueryAtom>();
		T2BoxAtoms = new Vector<QueryAtom>();
		R0BoxAtoms = new Vector<QueryAtom>();
		R1BoxAtoms = new Vector<QueryAtom>();
		R2BoxAtoms = new Vector<QueryAtom>();
		
		List<QueryAtomGroup> groups = queryObject.getAtomGroups();

		for (QueryAtomGroup group : groups) {
			List<QueryAtom> atoms = group.getAtoms();
			for (QueryAtom atom : atoms) {
				List<QueryArgument> args = atom.getArguments();
				int varCount = args.size();
				for (QueryArgument arg: args)
					if (!arg.isVar()) varCount--;

				if (isTBoxAtom(atom)) {
					if (varCount == 0)
						T0BoxAtoms.add(atom);
					else if (varCount == 1)
						T1BoxAtoms.add(atom);
					else if (varCount == 2)
						T2BoxAtoms.add(atom);
				} else if (isRBoxAtom(atom)) {
					if (varCount == 0)
						R0BoxAtoms.add(atom);
					else if (varCount == 1)
						R1BoxAtoms.add(atom);
					else if (varCount == 2)
						R2BoxAtoms.add(atom);
				} else if (isABoxAtom(atom)) {
					if (varCount == 0)
						A0BoxAtoms.add(atom);
					else if (varCount == 1)
						A1BoxAtoms.add(atom);
					else if (varCount == 2)
						A2BoxAtoms.add(atom);
					else if (varCount == 3)
						A3BoxAtoms.add(atom);
				}
			}
		}
	}
	
	List<QueryAtom> getABoxAtoms() {
		List<QueryAtom> ABoxAtoms = new Vector<QueryAtom>();
		
		ABoxAtoms.addAll(A0BoxAtoms);
		ABoxAtoms.addAll(A1BoxAtoms);
		ABoxAtoms.addAll(A2BoxAtoms);
		ABoxAtoms.addAll(A3BoxAtoms);
		
		return ABoxAtoms;
	}
	
	List<QueryAtom> getTBoxAtoms() {
		List<QueryAtom> TBoxAtoms = new Vector<QueryAtom>();
		
		TBoxAtoms.addAll(T0BoxAtoms);
		TBoxAtoms.addAll(T1BoxAtoms);
		TBoxAtoms.addAll(T2BoxAtoms);
		
		return TBoxAtoms;
	}
	
	List<QueryAtom> getRBoxAtoms() {
		List<QueryAtom> RBoxAtoms = new Vector<QueryAtom>();
		
		RBoxAtoms.addAll(R0BoxAtoms);
		RBoxAtoms.addAll(R1BoxAtoms);
		RBoxAtoms.addAll(R2BoxAtoms);
		
		return RBoxAtoms;
	}
	
	boolean hasInconsistency() {
		List<QueryAtom> ABoxAtoms = getABoxAtoms();
		
		for (QueryAtom atom : ABoxAtoms) {

			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {

				List<QueryArgument> args = atom.getArguments();

				for (QueryAtom atom2 : ABoxAtoms) {

					if (atom2.getType().equals(QueryAtomType.SAME_AS)) {

						List<QueryArgument> args2 = atom2.getArguments();

						if (args.containsAll(args2)) {
							System.err.println("Catched Inconsistency...");
							return true;
						}
					}
				}
			}
		}
		
		List<QueryAtom> TBoxAtoms = getTBoxAtoms();
		
		for (QueryAtom atom : TBoxAtoms) {

			if (atom.getType().equals(QueryAtomType.DISJOINT_WITH) || atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {

				List<QueryArgument> args = atom.getArguments();

				for (QueryAtom atom2 : TBoxAtoms) {

					if (atom2.getType().equals(QueryAtomType.SUB_CLASS_OF) || atom2.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {

						List<QueryArgument> args2 = atom2.getArguments();

						if (args.containsAll(args2)) {
							System.err.println("Catched Inconsistency...");
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	void optimize() {
		List<QueryAtom> TBoxAtoms = getTBoxAtoms();
		
		for (QueryAtom atom : TBoxAtoms) {

			if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {

				List<QueryArgument> args = atom.getArguments();

				for (QueryAtom atom2 : TBoxAtoms) {

					if (atom2.getType().equals(QueryAtomType.SUB_CLASS_OF)) {

						List<QueryArgument> args2 = atom2.getArguments();

						if (args.containsAll(args2)) {
							System.out.println("Optimize...");

							T0BoxAtoms.remove(atom2);
							T1BoxAtoms.remove(atom2);
							T2BoxAtoms.remove(atom2);
						}
					}
				}
			}
		}
	}
	
	void identifyVariableType() {
		identifyVariableTypeFromRBoxAtoms();
		identifyVariableTypeFromTBoxAtoms();
		identifyVariableTypeFromABoxAtoms();
	}
	
	private void identifyVariableTypeFromABoxAtoms() {
		List<QueryAtom> ABoxAtoms = getABoxAtoms();
		
		for (QueryAtom atom: ABoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			int argIndex = 0;
			for (QueryArgument arg: args) {
				argIndex++;
				if (arg.isVar()) {
					
					String var = arg.getValue();
					Variable variable;
					
					if (variables.containsKey(var))
						variable = variables.get(var);
					else
						variable = new Variable(var);
					
					if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM) || atom.getType().equals(QueryAtomType.SAME_AS))
						variable.setType(VariableTypes.INDIVIDUALS);
					else if (atom.getType().equals(QueryAtomType.TYPE)) {
						if (argIndex == 1)
							variable.setType(VariableTypes.INDIVIDUALS);
						else
							variable.setType(VariableTypes.CLASSES);
					}
					else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
						if (argIndex == 1)
							variable.setType(VariableTypes.INDIVIDUALS);
						else if(argIndex == 2)
							variable.setType(VariableTypes.PROPERTIES);
						else if(argIndex == 3)
							variable.setType(VariableTypes.INDIVIDUALS_OR_LITERALS);
					}
					
					variables.put(var, variable);
				}
			}
		}
	}
	
	private void identifyVariableTypeFromTBoxAtoms() {
		List<QueryAtom> TBoxAtoms = getTBoxAtoms();
		
		for (QueryAtom atom: TBoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.getValue();
					Variable variable;
					
					if (variables.containsKey(var))
						variable = variables.get(var);
					else
						variable = new Variable(var);
					
					variable.setType(VariableTypes.CLASSES);
					
					variables.put(var, variable);
				}
		}
	}
	
	private void identifyVariableTypeFromRBoxAtoms() {
		List<QueryAtom> RBoxAtoms = getRBoxAtoms();
		
		for (QueryAtom atom: RBoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.getValue();
					Variable variable;
					
					if (variables.containsKey(var))
						variable = variables.get(var);
					else
						variable = new Variable(var);
					
					if (atom.getType().equals(QueryAtomType.DATA_PROPERTY))
						variable.setType(VariableTypes.DATA_PROPERTIES);
					else if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY) || atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL) || atom.getType().equals(QueryAtomType.SYMMETRIC) || atom.getType().equals(QueryAtomType.TRANSITIVE) || atom.getType().equals(QueryAtomType.INVERSE_OF))
						variable.setType(VariableTypes.OBJECT_PROPERTIES);
					else if (atom.getType().equals(QueryAtomType.FUNCTIONAL) || atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF) || atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY))
						variable.setType(VariableTypes.PROPERTIES);
					
					variables.put(var, variable);
				}
		}
	}
}
