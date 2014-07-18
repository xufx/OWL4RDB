package janus.query.sparqldl;

import java.util.List;
import java.util.Vector;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;

class Vertex {
	private List<QueryAtom> atoms = new Vector<QueryAtom>();
	private List<Vertex> adjacentVertices = new Vector<Vertex>();
	private SPARQLDLResultSet resultSet = null;
	
	Vertex(Vertex vertex) {
		atoms.addAll(vertex.atoms);
		this.resultSet = vertex.resultSet;
	}
	
	Vertex(QueryAtom atom) {
		atoms.add(atom);
	}
	
	void setResultSet(SPARQLDLResultSet resultSet) {
		this.resultSet = resultSet;
	}
	
	SPARQLDLResultSet getResultSet() {
		return resultSet;
	}
	
	boolean isResultEmptySet() {
		return resultSet.isEmptySet();
	}
	
	boolean isExecuted() {
		if (resultSet != null && atoms.size() > 1)
			return true;
		else
			return false;
	}
	
	QueryAtom getAtomAt(int index) {
		return atoms.get(index);
	}
	
	void addAdjacentVertex(Vertex v) {
		adjacentVertices.add(v);
	}
	
	int getEdgeCount() {
		return adjacentVertices.size();
	}
	
	Vertex getAdjacentVertexAt(int index) {
		return adjacentVertices.get(index);
	}
	
	void joinResultSets(Vertex vertex) {
		atoms.addAll(vertex.atoms);
		
		resultSet = resultSet.getNaturalJoinedResultSet(vertex.resultSet);
	}
	
	static List<Vertex> buildListOfVertices(List<QueryAtom> atoms) {
		List<Vertex> list = new Vector<Vertex>(atoms.size());
		
		for (QueryAtom atom: atoms) {
			Vertex vertex = new Vertex(atom);
			
			list.add(vertex);
		}
		
		return list;
	}
	
	List<String> getListOfVariables() {
		List<String> vars = new Vector<String>();
		
		for (QueryAtom atom: atoms) {
			List<QueryArgument> args = atom.getArguments();
			
			for (QueryArgument arg: args) {
				String aStringOfArg = arg.toString();
				if (arg.isVar() && !vars.contains(aStringOfArg))
					vars.add(aStringOfArg);
			}
		}
		
		return vars;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vertex) {
			Vertex arg = (Vertex)obj;
			List<QueryAtom> atomsOfArg = arg.atoms;
			if (atoms.size() == atomsOfArg.size() && atoms.containsAll(atomsOfArg))
				return true;
			else
				return false;
		} else
			return false;
	}
	
	
}