package janus.sparqldl;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryAtom;

public class AtomGraph {
	private List<Vertex> heads = new Vector<Vertex>();
	private boolean[] visited;
	private List<QueryAtom> bfsPath;
	private List<QueryAtom> groupGT1;
	private int startAtomIndex;
	
	AtomGraph(List<QueryAtom> groupGT1) {
		
		this.groupGT1 = groupGT1;
		
		visited = new boolean[groupGT1.size()];
		
		bfsPath = new Vector<QueryAtom>();
		
		for (QueryAtom atom: groupGT1) {
			Vertex node = new Vertex(atom);
			
			heads.add(node);
		}
		
		for (Vertex head: heads) {
			QueryAtom headAtom = head.getAtom();
			
			List<QueryArgument> args = headAtom.getArguments();
			
			List<String> vars = new Vector<String>();
			
			for (QueryArgument arg: args)
				if (arg.isVar())
					vars.add(arg.toString());
			
			Vertex currentLast = head;
			
			for (Vertex other: heads) {
				QueryAtom otherAtom = other.getAtom();
				
				if (otherAtom.equals(headAtom)) continue;
				
				List<QueryArgument> otherArgs = otherAtom.getArguments();
				
				for (QueryArgument arg: otherArgs)
					if (arg.isVar())
						if (vars.contains(arg.toString())) {
							Vertex node = new Vertex(otherAtom);
							
							currentLast.setNext(node);
							currentLast = node;
							
							break;
						}
			}
		}
	}
	
	private void resetVisited() {
		for (int i = 0; i < visited.length; i++)
			visited[i] = false;
	}
	
	private Vertex getHeadVertex(QueryAtom atom) {
		for (Vertex vertex: heads) {
			if (vertex.getAtom().equals(atom))
				return vertex;
		}
		
		return null;
	}
	
	List<QueryAtom> getBFSPath(QueryAtom startAtom) {
		resetVisited();
		
		bfsPath = new Vector<QueryAtom>();
		
		Queue queue = new Queue();
//System.out.println("startAtom: "  + startAtom);		
		bfsPath.add(startAtom);//System.out.println("add to path : " + startAtom);
		
		int visitedIndex = groupGT1.indexOf(startAtom);
		startAtomIndex = visitedIndex;
		visited[visitedIndex] = true;
		
		Vertex head = getHeadVertex(startAtom);
		Vertex next = head.getNext();
		while (next != null) {
			queue.add(next);
			next = next.getNext();
		}
		
		while (!queue.isEmpty()) {
			Vertex first = queue.getFirst();
			QueryAtom firstAtom = first.getAtom();
			
			visitedIndex = groupGT1.indexOf(firstAtom);
			
			if (visitedIndex > startAtomIndex)
				continue;
			
			if (visited[visitedIndex] == false) {
				bfsPath.add(firstAtom); 
				visited[visitedIndex] = true;
			} else
				continue;
			
			head = getHeadVertex(firstAtom);
			next = head.getNext();
			while (next != null) {
				queue.add(next);
				next = next.getNext();
			}
		}
		
		return bfsPath;
	}
}

class Vertex {
	private QueryAtom atom;
	private Vertex next;
	
	Vertex(QueryAtom atom) {
		this.atom = atom;
		next = null;
	}
	
	QueryAtom getAtom() {
		return atom;
	}

	Vertex getNext() {
		return next;
	}
	
	void setNext(Vertex nextNode) {
		next = nextNode;
	}
	
}

class Queue {
	ArrayList<Vertex> list = new ArrayList<Vertex>();

	public Queue() {}

	void add(Vertex o) {
		list.add(o);
	}

	boolean isEmpty() {
		return list.isEmpty();
	}
	
	Vertex getFirst() {
		Vertex first = list.get(0);//System.out.println("first: " + first.getAtom().toString());
		list.remove(0);
		return first;
	}
}

