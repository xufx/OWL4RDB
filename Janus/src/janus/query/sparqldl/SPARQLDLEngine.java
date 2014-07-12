package janus.query.sparqldl;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.derivo.sparqldlapi.QueryAtom;

public class SPARQLDLEngine {
	private QueryMetadata query;
	
	//private Hashtable<QueryAtom, SPARQLDLResultSet> atomResultSetPairs;
	
	private AtomProcessor atomProcessor;
	
	public SPARQLDLEngine(String queryString) {
		query = new QueryMetadata(queryString);
		atomProcessor = new AtomProcessor(query);
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
			if (!atomProcessor.executeR0BoxAtoms(R0BoxAtoms))
				return false;
		
		List<QueryAtom> T0BoxAtoms = query.getT0BoxAtoms();
		if (T0BoxAtoms.size() > 0)
			if (!atomProcessor.executeT0BoxAtoms(T0BoxAtoms))
				return false;
			
		List<QueryAtom> A0BoxAtoms = query.getA0BoxAtoms();
		if (A0BoxAtoms.size() > 0) {
			if (!atomProcessor.executeA0BoxAtomsWithoutPresenceCheck(A0BoxAtoms))
				return false;
			
			if (!atomProcessor.executeA0BoxAtomsWithPresenceCheck(A0BoxAtoms))
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
		
		query.identifyVariableType();
		
		//atomResultSetPairs = new Hashtable<QueryAtom, SPARQLDLResultSet>();
		
		List<Vertex> mergedVertices = new Vector<Vertex>();
		
		List<QueryAtom> T1BoxAtoms = query.getT1BoxAtoms();
		if (!T1BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, T1BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> T2BoxAtoms = query.getT2BoxAtoms();
		if (!T2BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, T2BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> R1BoxAtoms = query.getR1BoxAtoms();
		if (!R1BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, R1BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> R2BoxAtoms = query.getR2BoxAtoms();
		if (!R2BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, R2BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> A1BoxAtoms = query.getA1BoxAtoms();
		if (!A1BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, A1BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> A2BoxAtoms = query.getA2BoxAtoms();
		if (!A2BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, A2BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		List<QueryAtom> A3BoxAtoms = query.getA3BoxAtoms();
		if (!A3BoxAtoms.isEmpty()) {
			mergedVertices = executeAtoms(mergedVertices, A3BoxAtoms);
			
			if (mergedVertices == null)
				return new DefaultTableModel();
		}
		
		Vertex mergedVertex = null;
		for (Vertex vertexToBeMerged: mergedVertices) {System.out.println("INSIDE");
			if (mergedVertex == null)
				mergedVertex = new Vertex(vertexToBeMerged);
			else
				mergedVertex.joinResultSets(vertexToBeMerged);
		}
		if (mergedVertex == null) System.out.println("HERE");
		SPARQLDLResultSet resultSet = mergedVertex.getResultSet();
		if (query.getABoxAtoms().isEmpty())
			return (TableModel) resultSet;
		
		/*if (!executeT1BoxAtoms(query.getT1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeT2BoxAtoms(query.getT2BoxAtoms()))
			return new DefaultTableModel();
		
		mergeResultSets(query.getT1T2BoxAtoms());
		
		if (!executeR1BoxAtoms(query.getR1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeR2BoxAtoms(query.getR2BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeA1BoxAtoms(query.getA1BoxAtoms()))
			return new DefaultTableModel();
		
		if (!executeA2BoxAtoms(query.getA2BoxAtoms()))
			return new DefaultTableModel();
		
		executeA3BoxAtoms(query.getA3BoxAtoms());*/
		
		return null;
	}
	
	private List<Vertex> executeAtoms(List<Vertex> mergedVertices, List<QueryAtom> atoms) {
		List<Vertex> vertices = new Vector<Vertex>();
		//if (mergedVertices != null)
		vertices.addAll(mergedVertices);
		vertices.addAll(Vertex.buildListOfVertices(atoms));
		
		Graph graph = new Graph(vertices);
		
		List<List<Vertex>> components = graph.getComponents();
		
		List<Vertex> verticesToBeMerged = new Vector<Vertex>(components.size());
		
		for (List<Vertex> component: components) {
			
			Vertex vertexToBeMerged = null;
			
			for (Vertex vertex: component) {
				
				if (!vertex.isExecuted())
					atomProcessor.executeVertex(vertex);
				
				if (vertex.isResultEmptySet())
					return null;
				
				if (vertexToBeMerged == null)
					vertexToBeMerged = new Vertex(vertex);
				else
					vertexToBeMerged.joinResultSets(vertex);
			}
			
			verticesToBeMerged.add(vertexToBeMerged);
		}
		
		return verticesToBeMerged;
	}
	
	/*void putAtomResult(QueryAtom atom, SPARQLDLResultSet resultSet) {
		atomResultSetPairs.put(atom, resultSet);
	}*/
	/*
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
	*/
}
