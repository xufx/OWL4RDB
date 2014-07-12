package janus.query.sparqldl;

import janus.Janus;
import janus.database.SQLResultSetTableModel;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import de.derivo.sparqldlapi.QueryAtom;

public class SPARQLDLEngine {
	private QueryMetadata query;
	
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
		for (Vertex vertexToBeMerged: mergedVertices) {
			if (mergedVertex == null)
				mergedVertex = new Vertex(vertexToBeMerged);
			else
				mergedVertex.joinResultSets(vertexToBeMerged);
		}
		
		SPARQLDLResultSet resultSet = mergedVertex.getResultSet();
		if (query.getABoxAtoms().isEmpty())
			return (TableModel) resultSet;
		else
			return new SQLResultSetTableModel(Janus.dbBridge.executeQuery(resultSet.getQuery()));
	}
	
	private List<Vertex> executeAtoms(List<Vertex> mergedVertices, List<QueryAtom> atoms) {
		List<Vertex> vertices = new Vector<Vertex>();
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
}
