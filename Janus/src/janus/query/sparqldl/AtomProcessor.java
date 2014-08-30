package janus.query.sparqldl;

import janus.Janus;
import janus.database.SQLResultSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class AtomProcessor {
	private QueryMetadata query;
	
	private enum AtomTypes {A_BOX_ATOM, R_BOX_ATOM, T_BOX_ATOM, KNOWN}
	
	AtomProcessor(QueryMetadata query) {
		this.query = query;
	}
	
	private int getVariableCount(QueryAtom atom) {
		int count = 0;
		
		List<QueryArgument> args = atom.getArguments();
		for (QueryArgument arg: args) {
			if (arg.isVar())
				count++;
		}
		
		return count;
	}
	
	private static AtomTypes getAtomType(QueryAtom atom) {
		if (QueryMetadata.isABoxAtom(atom))
			return AtomTypes.A_BOX_ATOM;
		
		if (QueryMetadata.isRBoxAtom(atom))
			return AtomTypes.R_BOX_ATOM;
		
		if (QueryMetadata.isTBoxAtom(atom))
			return AtomTypes.T_BOX_ATOM;
		
		return AtomTypes.KNOWN;
	}
	
	void executeVertex(Vertex vertex) {
		QueryAtom atom = vertex.getAtomAt(0);
		
		int varCount = getVariableCount(atom);
		
		AtomTypes atomType = getAtomType(atom);
		
		if (atomType == AtomTypes.A_BOX_ATOM) {
			if (varCount == 1)
				vertex.setResultSet(executeA1BoxAtom(atom));
			else if (varCount == 2)
				vertex.setResultSet(executeA2BoxAtom(atom));
			else if (varCount == 3)
				vertex.setResultSet(executeA3BoxAtom(atom));
		} else if (atomType == AtomTypes.R_BOX_ATOM) {
			if (varCount == 1)
				vertex.setResultSet(executeR1BoxAtom(atom));
			else if (varCount == 2)
				vertex.setResultSet(executeR2BoxAtom(atom));
		} else if (atomType == AtomTypes.T_BOX_ATOM) {
			if (varCount == 1)
				vertex.setResultSet(executeT1BoxAtom(atom));
			else if (varCount == 2)
				vertex.setResultSet(executeT2BoxAtom(atom));
		}
	}
	
	private janus.query.sparqldl.SQLResultSet executeA1BoxAtom(QueryAtom atom) {
		if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM))
			return AtomDifferentFromProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.SAME_AS))
			return AtomSameAsProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.TYPE))
			return AtomTypeProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE))
			return AtomPropertyValueProcessor.execute1(atom);

		return null;
	}
	
	private janus.query.sparqldl.SQLResultSet executeA2BoxAtom(QueryAtom atom) {
		if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM))
			return AtomDifferentFromProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.SAME_AS))
			return AtomSameAsProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.TYPE))
			return AtomTypeProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE))
			return AtomPropertyValueProcessor.execute2(atom);

		return null;
	}
	
	private janus.query.sparqldl.SQLResultSet executeA3BoxAtom(QueryAtom atom) {
		if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE))	
				return AtomPropertyValueProcessor.execute3(atom);
		
		return null;
	}
	
	boolean executeA0BoxAtomsWithoutPresenceCheck(List<QueryAtom> A0BoxAtoms) {
		for (QueryAtom atom: A0BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				
				if (AtomDifferentFromProcessor.execute0WithoutPresenceCheck(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				
				if (AtomSameAsProcessor.execute0WithoutPresenceCheck(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.TYPE)) {
				
				if (AtomTypeProcessor.execute0WithoutPresenceCheck(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				
				if (AtomPropertyValueProcessor.execute0WithoutPresenceCheck(atom))
					continue;
				else
					return false;
			}
		}
		
		return true;
	}
	
	boolean executeA0BoxAtomsWithPresenceCheck(List<QueryAtom> A0BoxAtoms) {
		Set<String> queries = new ConcurrentSkipListSet<String>();
		
		for (QueryAtom atom: A0BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				
				queries.addAll(AtomDifferentFromProcessor.execute0WithPresenceCheck(atom));
			
			}
			
			else if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				
				queries.add(AtomSameAsProcessor.execute0WithPresenceCheck(atom));
					
			}
			
			else if (atom.getType().equals(QueryAtomType.TYPE)) {
				
				queries.add(AtomTypeProcessor.execute0WithPresenceCheck(atom));
				
			}
			
			else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				
				queries.addAll(AtomPropertyValueProcessor.execute0WithPresenceCheck(atom));
		
			}
		}
		
		String query = Janus.sqlGenerator.getQueryToCheckPresence(queries);
		
		SQLResultSet resultSet = Janus.dbBridge.executeQuery(query);
		
		List<String> record = resultSet.getResultSetRowAt(1);
		
		return Boolean.parseBoolean(record.get(0));
	}
	
	boolean executeT0BoxAtoms(List<QueryAtom> T0BoxAtoms) {
		for (QueryAtom atom: T0BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
				
				if (AtomComplementOfProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
				
				if (AtomDisjointWithProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
				
				if (AtomEquivalentClassProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
				
				if (AtomSubClassOfProcessor.execute0(atom))
					continue;
				else
					return false;
			}
		}
		
		return true;
	}
	
	boolean executeR0BoxAtoms(List<QueryAtom> R0BoxAtoms) {
		for (QueryAtom atom: R0BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.TRANSITIVE)) {
				
				if (AtomTransitiveProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.SYMMETRIC)) {
				
				if (AtomSymmetricProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL)) {
				
				if (AtomInverseFunctionalProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.FUNCTIONAL)) {
				
				if (AtomFunctionalProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.DATA_PROPERTY)) {
				
				if (AtomDataPropertyProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY)) {
				
				if (AtomObjectPropertyProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
				
				if (AtomInverseOfProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
				
				if (AtomEquivalentPropertyProcessor.execute0(atom))
					continue;
				else
					return false;
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
				
				if (AtomSubPropertyOfProcessor.execute0(atom))
					continue;
				else
					return false;
			}
		}
		
		return true;
	}
	
	private URIResultSet executeR1BoxAtom(QueryAtom atom) {
		
		if (atom.getType().equals(QueryAtomType.TRANSITIVE))
			return AtomTransitiveProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.SYMMETRIC))
			return AtomSymmetricProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL))
			return AtomInverseFunctionalProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.FUNCTIONAL)) {
			List<QueryArgument> args = atom.getArguments();

			String varName = args.get(0).getValue();
			Variable variable = query.getVariable(varName);

			return AtomFunctionalProcessor.execute1(atom, variable);
		}
		
		else if (atom.getType().equals(QueryAtomType.DATA_PROPERTY))
			return AtomDataPropertyProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY))
			return AtomObjectPropertyProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.INVERSE_OF))
			return AtomObjectPropertyProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
			List<QueryArgument> args = atom.getArguments();

			String varName = null;

			for (QueryArgument arg: args)
				if (arg.getType().equals(QueryArgumentType.VAR)) {
					varName = arg.getValue();
					break;
				}

			Variable variable = query.getVariable(varName);

			return AtomEquivalentPropertyProcessor.execute1(atom, variable);
		}

		else if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
			List<QueryArgument> args = atom.getArguments();

			QueryArgument arg1 = args.get(0);
			QueryArgument arg2 = args.get(1);

			String varName = null;

			if (arg1.getType().equals(QueryArgumentType.VAR))
				varName = arg1.getValue();
			else
				varName = arg2.getValue();

			Variable variable = query.getVariable(varName);

			return AtomSubPropertyOfProcessor.execute1(atom, variable);
		}

		return null;
	}
	
	private URIResultSet executeR2BoxAtom(QueryAtom atom) {
		if (atom.getType().equals(QueryAtomType.INVERSE_OF))
			return AtomInverseOfProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).getValue();
			String varName2 = args.get(1).getValue();

			Variable var1 = query.getVariable(varName1);
			Variable var2 = query.getVariable(varName2);

			return AtomEquivalentPropertyProcessor.execute2(atom, var1, var2);
		}

		else if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {

			List<QueryArgument> args = atom.getArguments();

			String varName1 = args.get(0).getValue();
			String varName2 = args.get(1).getValue();

			Variable var1 = query.getVariable(varName1);
			Variable var2 = query.getVariable(varName2);

			return AtomSubPropertyOfProcessor.execute2(atom, var1, var2);
		}

		return null;
	}
	
	private URIResultSet executeT1BoxAtom(QueryAtom atom) {
		
		if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF))
			return AtomComplementOfProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.DISJOINT_WITH))
			return AtomDisjointWithProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS))
			return AtomEquivalentClassProcessor.execute1(atom);

		else if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF))
			return AtomSubClassOfProcessor.execute1(atom);
		
		return null;
	}
	
	private URIResultSet executeT2BoxAtom(QueryAtom atom) {
		
		if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF))
			return AtomComplementOfProcessor.execute2(atom);
		
		else if (atom.getType().equals(QueryAtomType.DISJOINT_WITH))
			return AtomDisjointWithProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS))
			return AtomEquivalentClassProcessor.execute2(atom);

		else if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF))
			return AtomSubClassOfProcessor.execute2(atom);
			
		return null;
	}
}