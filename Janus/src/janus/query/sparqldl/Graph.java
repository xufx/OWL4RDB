package janus.query.sparqldl;

import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Graph {
	private List<Vertex> heads;
	private boolean[] visited;
	private List<Vertex> bfsPath;
	private List<List<Vertex>> components;
	
	Graph(List<Vertex> vertices) {
		buildGraph(vertices);
		visited = new boolean[heads.size()];
		seperateGraph();
	}
	
	private void buildGraph(List<Vertex> vertices) {
		heads = vertices;
		
		for (Vertex head: heads) {
			List<String> vars = head.getListOfVariables();
			
			for (Vertex other: heads) {
				if (other.equals(head)) continue;
				
				List<String> otherVars = other.getListOfVariables();
				
				for (String var: otherVars)
					if (vars.contains(var)) {
						head.addAdjacentVertex(other);
						break;
					}
			}
		}
	}
	
	private void seperateGraph() {
		components = new Vector<List<Vertex>>();
			
		if (heads.isEmpty())
			return;
		
		Vertex startVertex;
		List<Vertex> tempHeads = new Vector<Vertex>(heads);
		List<Vertex> path = new Vector<Vertex>();
		
		do {
			tempHeads.removeAll(path);
			
			startVertex = tempHeads.get(0);
		
			path = getBFSPath(startVertex);
			
			components.add(path);
			
		} while (path.size() < tempHeads.size());
	}
	
	List<List<Vertex>> getComponents() {
		return components;
	}
	
	/*int getComponentCount() {
		return components.size();
	}*/
	
	private void initVisited() {
		for (int i = 0; i < visited.length; i++)
			visited[i] = false;
	}
	
	private List<Vertex> getBFSPath(Vertex startVertex) {
		initVisited();
		
		bfsPath = new Vector<Vertex>();
		
		Queue<Vertex> queue = new ConcurrentLinkedQueue<Vertex>();
		
		bfsPath.add(startVertex);
		
		int visitedIndex = heads.indexOf(startVertex);
		visited[visitedIndex] = true;
		
		int edgeCount = startVertex.getEdgeCount();
		for (int i = 0; i < edgeCount; i++)
			queue.add(startVertex.getAdjacentVertexAt(i));
		
		while (!queue.isEmpty()) {
			Vertex element = queue.poll();
			visitedIndex = heads.indexOf(element);
			
			bfsPath.add(element); 
			visited[visitedIndex] = true;
			
			edgeCount = element.getEdgeCount();
			for (int i = 0; i < edgeCount; i++) {
				Vertex adjacentVertexOfElement = element.getAdjacentVertexAt(i);
				visitedIndex = heads.indexOf(adjacentVertexOfElement);
				if (visited[visitedIndex] == false)
					queue.add(element.getAdjacentVertexAt(i));
			}
		}
		
		return bfsPath;
	}
}