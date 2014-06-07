package janus.query.sparqldl;

import java.util.List;
import java.util.Vector;
import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.QueryAtomGroup;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import de.derivo.sparqldlapi.types.QueryAtomType;
import de.derivo.sparqldlapi.types.QueryType;

class QueryMetadata {
	private Query queryObject;
	
	private QueryTypes queryType;
	
	private List<QueryAtom> A0BoxAtoms, A1BoxAtoms, A2BoxAtoms, A3BoxAtoms, T0BoxAtoms, T1BoxAtoms, T2BoxAtoms, R0BoxAtoms, R1BoxAtoms, R2BoxAtoms; // The number means the count of variables.
	
	QueryMetadata(String query) {
		queryObject = createQueryObject(query);
		
		seperateAtoms();
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
	
	private boolean isTBoxAtom(QueryAtom atom) {
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
	
	private boolean isRBoxAtom(QueryAtom atom) {
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
	
	private boolean isABoxAtom(QueryAtom atom) {
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
		
		ABoxAtoms.addAll(A1BoxAtoms);
		ABoxAtoms.addAll(A2BoxAtoms);
		ABoxAtoms.addAll(A3BoxAtoms);
		
		return ABoxAtoms;
	}
	
	List<QueryAtom> getTBoxAtoms() {
		List<QueryAtom> TBoxAtoms = new Vector<QueryAtom>();
		
		TBoxAtoms.addAll(T1BoxAtoms);
		TBoxAtoms.addAll(T2BoxAtoms);
		
		return TBoxAtoms;
	}
	
	List<QueryAtom> getRBoxAtoms() {
		List<QueryAtom> RBoxAtoms = new Vector<QueryAtom>();
		
		RBoxAtoms.addAll(R1BoxAtoms);
		RBoxAtoms.addAll(R2BoxAtoms);
		
		return RBoxAtoms;
	}
}
