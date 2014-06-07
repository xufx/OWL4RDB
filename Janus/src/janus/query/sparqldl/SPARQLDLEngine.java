package janus.query.sparqldl;

import janus.Janus;
import janus.database.SQLResultSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;
import de.derivo.sparqldlapi.types.QueryArgumentType;
import de.derivo.sparqldlapi.types.QueryAtomType;

public class SPARQLDLEngine {
	private QueryMetadata query;
	
	private Map<String, Variable> variables;
	
	private Hashtable<QueryAtom, SPARQLDLResultSet> atomResultSetPairs;
	
	private List<QueryAtom> groupGT1;
	
	public SPARQLDLEngine(String queryString) {
		query = new QueryMetadata(queryString);
		
		variables = new Hashtable<String, Variable>();
	}
	
	public QueryTypes getQueryType() {
		return query.getQueryType();
	}
	
	public boolean executeAskQuery() {
		
		if (query.isEmptyQuery()) {
			System.err.println("The number of atoms is 0.");
			return true;
		}
		
		if (query.hasAnnotationAtoms()) {
			System.err.println("The ontology does't have annotation assertions.");
			return false;
		}
		
		if (query.hasInconsistency()) {
			return false;
		}
		
		query.optimize();
		
		List<QueryAtom> R0BoxAtoms = query.getR0BoxAtoms();
		if (R0BoxAtoms.size() > 0)
			if (!executeR0BoxAtoms(R0BoxAtoms))
				return false;
		
		List<QueryAtom> T0BoxAtoms = query.getT0BoxAtoms();
		if (T0BoxAtoms.size() > 0)
			if (!executeT0BoxAtoms(T0BoxAtoms))
				return false;
			
		List<QueryAtom> A0BoxAtoms = query.getA0BoxAtoms();
		if (A0BoxAtoms.size() > 0) {
			if (!executeA0BoxAtomsWithoutPresenceCheck(A0BoxAtoms))
				return false;
			
			if (!executeA0BoxAtomsWithPresenceCheck(A0BoxAtoms))
				return false;
		}
		
		return true;
	}
	
	public TableModel executeSelectQuery() {
		
		if (query.isEmptyQuery()) {
			System.err.println("The number of atoms is 0.");
			return new DefaultTableModel();
		}
		
		if (query.hasAnnotationAtoms()) {
			System.err.println("The ontology does't have annotation assertions.");
			return new DefaultTableModel();
		}
		
		if (query.hasInconsistency()) {
			return new DefaultTableModel();
		}
		
		query.optimize();
		
		identifyVariableType();
		
		atomResultSetPairs = new Hashtable<QueryAtom, SPARQLDLResultSet>();
		
		if (!executeT1BoxAtoms(query.getT1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeT2BoxAtoms(query.getT2BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeR1BoxAtoms(query.getR1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeR2BoxAtoms(query.getR2BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeA1BoxAtoms(query.getA1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeA2BoxAtoms(query.getA2BoxAtoms()))
			return new DefaultTableModel();
		
		executeA3BoxAtoms(query.getA3BoxAtoms());
		
		return null;
	}
	
	void putAtomResult(QueryAtom atom, SPARQLDLResultSet resultSet) {
		atomResultSetPairs.put(atom, resultSet);
	}
	
	private void identifyVariableType() {
		identifyVariableTypeFromRBoxAtoms();
		identifyVariableTypeFromTBoxAtoms();
		identifyVariableTypeFromABoxAtoms();
	}
	
	private void identifyVariableTypeFromABoxAtoms() {
		List<QueryAtom> ABoxAtoms = query.getABoxAtoms();
		
		for (QueryAtom atom: ABoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			int argIndex = 0;
			for (QueryArgument arg: args) {
				argIndex++;
				if (arg.isVar()) {
					
					String var = arg.toString();
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
		List<QueryAtom> TBoxAtoms = query.getTBoxAtoms();
		
		for (QueryAtom atom: TBoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.toString();
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
		List<QueryAtom> RBoxAtoms = query.getRBoxAtoms();
		
		for (QueryAtom atom: RBoxAtoms) {
			List<QueryArgument> args = atom.getArguments();
			for (QueryArgument arg: args)
				if (arg.isVar()) {
					
					String var = arg.toString();
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
	
	Variable getVariable(String name) {
		return variables.get(name);
	}
	
	/*private boolean execute() {
		boolean result = executeR0BoxAtoms();
		
		if (result)
			result = executeGroupR1();
		else
			return false;
		
		if (result)
			result = executeGroupT1();
		else
			return false;
		
		if (result)
			result = executeA1BoxAtoms();
		else
			return false;
		
		ABox2Processor abox2Processor = new ABox2Processor();
		ABox3Processor abox3Processor = new ABox3Processor();
		RBox2Processor rbox2Processor = new RBox2Processor();
		TBox2Processor tbox2Processor = new TBox2Processor();
		
		AtomGraph graph = new AtomGraph(groupGT1);
		
		for (QueryAtom atom: groupGT1) {
			List<QueryAtom> bfsPath = graph.getBFSPath(atom);
			
			for (QueryAtom vAtom: bfsPath) {
				if (isTBoxAtom(vAtom))
					tbox2Processor.execute(vAtom, variables);
				else if (isRBoxAtom(vAtom))
					rbox2Processor.execute(vAtom, variables);
				else {
					List<QueryArgument> args = vAtom.getArguments();
					int groupIndex = args.size();
					for (QueryArgument arg: args)
						if (!arg.isVar()) groupIndex--;
					
					if (groupIndex < 3)
						abox2Processor.execute(vAtom, variables);
					else
						abox3Processor.execute(vAtom, variables);
				}
			}
		}
		
		return true;
	}*/
	
	private boolean executeA1BoxAtoms(List<QueryAtom> A1BoxAtoms) {
		for (QueryAtom atom: A1BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				
				janus.query.sparqldl.SQLResultSet resultSet = AtomDifferentFromProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
				
			}
			
			else if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				
				janus.query.sparqldl.SQLResultSet resultSet = AtomSameAsProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.TYPE)) {
				
				janus.query.sparqldl.SQLResultSet resultSet = AtomTypeProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
				
			}
			
			else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				
				janus.query.sparqldl.SQLResultSet resultSet = AtomPropertyValueProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
				
			}
		}
		
		return true;
	}
	
	private boolean executeA2BoxAtoms(List<QueryAtom> A2BoxAtoms) {
		for (QueryAtom atom: A2BoxAtoms) {
			if (atom.getType().equals(QueryAtomType.DIFFERENT_FROM)) {
				
				putAtomResult(atom, AtomDifferentFromProcessor.execute2(atom));
			}
			
			else if (atom.getType().equals(QueryAtomType.SAME_AS)) {
				
				putAtomResult(atom, AtomSameAsProcessor.execute2(atom));
			}
			
			else if (atom.getType().equals(QueryAtomType.TYPE)) {
				
				putAtomResult(atom, AtomTypeProcessor.execute2(atom));
			}
			
			else if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
				
				janus.query.sparqldl.SQLResultSet resultSet = AtomPropertyValueProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
				
			}
		}
		
		return true;
	}
	
	private boolean executeA3BoxAtoms(List<QueryAtom> A3BoxAtoms) {
		
		for (QueryAtom atom: A3BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.PROPERTY_VALUE)) {
			
				putAtomResult(atom, AtomPropertyValueProcessor.execute3(atom));
				
			}
		}
		
		return true;
	}
	
	private boolean executeA0BoxAtomsWithoutPresenceCheck(List<QueryAtom> A0BoxAtoms) {
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
	
	private boolean executeA0BoxAtomsWithPresenceCheck(List<QueryAtom> A0BoxAtoms) {
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
	
	private boolean executeT0BoxAtoms(List<QueryAtom> T0BoxAtoms) {
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
	
	private boolean executeR0BoxAtoms(List<QueryAtom> R0BoxAtoms) {
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
	
	private boolean executeR1BoxAtoms(List<QueryAtom> R1BoxAtoms) {
		for (QueryAtom atom: R1BoxAtoms) {
			if (atom.getType().equals(QueryAtomType.TRANSITIVE)) {
				
				URIResultSet resultSet = AtomTransitiveProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.SYMMETRIC)) {
				
				URIResultSet resultSet = AtomSymmetricProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.INVERSE_FUNCTIONAL)) {
				
				URIResultSet resultSet = AtomInverseFunctionalProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.FUNCTIONAL)) {
				
				List<QueryArgument> args = atom.getArguments();
				
				String varName = args.get(0).toString();
				Variable variable = variables.get(varName);
				
				URIResultSet resultSet = AtomFunctionalProcessor.execute1(atom, variable);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.DATA_PROPERTY)) {
				
				URIResultSet resultSet = AtomDataPropertyProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.OBJECT_PROPERTY)) {
				
				URIResultSet resultSet = AtomObjectPropertyProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
				
				URIResultSet resultSet = AtomObjectPropertyProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName = null;
				
				for (QueryArgument arg: args)
					if (arg.getType().equals(QueryArgumentType.VAR)) {
						varName = arg.toString();
						break;
					}
				
				Variable variable = variables.get(varName);
				
				URIResultSet resultSet = AtomEquivalentPropertyProcessor.execute1(atom, variable);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
					
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
				List<QueryArgument> args = atom.getArguments();
				
				QueryArgument arg1 = args.get(0);
				QueryArgument arg2 = args.get(1);
				
				String varName = null;
				
				if (arg1.getType().equals(QueryArgumentType.VAR))
					varName = arg1.toString();
				else
					varName = arg2.toString();
				
				Variable variable = variables.get(varName);
				
				URIResultSet resultSet = AtomSubPropertyOfProcessor.execute1(atom, variable);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
		}
		
		return true;
	}
	
	private boolean executeR2BoxAtoms(List<QueryAtom> R2BoxAtoms) {
		for (QueryAtom atom: R2BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.INVERSE_OF)) {
				
				URIResultSet resultSet = AtomInverseOfProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_PROPERTY)) {
				List<QueryArgument> args = atom.getArguments();
				
				String varName1 = args.get(0).toString();
				String varName2 = args.get(1).toString();
				
				Variable var1 = variables.get(varName1);
				Variable var2 = variables.get(varName2);
				
				URIResultSet resultSet = AtomEquivalentPropertyProcessor.execute2(atom, var1, var2);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_PROPERTY_OF)) {
				
				List<QueryArgument> args = atom.getArguments();
				
				String varName1 = args.get(0).toString();
				String varName2 = args.get(1).toString();
				
				Variable var1 = variables.get(varName1);
				Variable var2 = variables.get(varName2);
				
				URIResultSet resultSet = AtomSubPropertyOfProcessor.execute2(atom, var1, var2);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
		}
		
		return true;
	}
	
	private boolean executeT1BoxAtoms(List<QueryAtom> T1BoxAtoms) {
		for (QueryAtom atom: T1BoxAtoms) {
			
			if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
				
				URIResultSet resultSet = AtomComplementOfProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
				
				URIResultSet resultSet = AtomDisjointWithProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
				
				URIResultSet resultSet = AtomEquivalentClassProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {
				
				URIResultSet resultSet = AtomSubClassOfProcessor.execute1(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
		}
		
		return true;
	}
	
	private boolean executeT2BoxAtoms(List<QueryAtom> T2BoxAtoms) {
		for (QueryAtom atom: T2BoxAtoms) {
			if (atom.getType().equals(QueryAtomType.COMPLEMENT_OF)) {
				
				URIResultSet resultSet = AtomComplementOfProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.DISJOINT_WITH)) {
				
				URIResultSet resultSet = AtomDisjointWithProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.EQUIVALENT_CLASS)) {
				
				URIResultSet resultSet = AtomEquivalentClassProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
			else if (atom.getType().equals(QueryAtomType.SUB_CLASS_OF)) {

				URIResultSet resultSet = AtomSubClassOfProcessor.execute2(atom);
				
				if (resultSet.isEmptySet())
					return false;
				
				putAtomResult(atom, resultSet);
			}
			
		}
		
		return true;
	}
	
}
