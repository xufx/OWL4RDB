package janus.sparqldl;

import janus.Janus;

import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

class Variable {
	private String varName;

	private VariableTypes varType;
	
	private Set<URI> URIs; // properties, classes
	private boolean URIsVirginity;
	private boolean hasURIsBecomeTheEmptySetAgain;
	
	private Set<IndividualSet> individualSets; // individuals
	private boolean individualsVirginity;
	private boolean hasIndividualsBecomeTheEmptySetAgain;
	
	private Set<LiteralSet> literalSets; // literals
	private boolean literalsVirginity;
	private boolean hasLiteralsBecomeTheEmptySetAgain;
	
	Variable(String varName) {
		this.varName = varName;
		varType = VariableTypes.UNKNOWN;
		URIs = new ConcurrentSkipListSet<URI>();
		individualSets = new ConcurrentSkipListSet<IndividualSet>();
		literalSets = new ConcurrentSkipListSet<LiteralSet>();
		
		URIsVirginity = true;
		individualsVirginity = true;
		literalsVirginity = true;
		
		hasURIsBecomeTheEmptySetAgain = false;
		hasIndividualsBecomeTheEmptySetAgain = false;
		hasLiteralsBecomeTheEmptySetAgain = false;
	}
	
	String getVarName() {
		return varName;
	}
	
	VariableTypes getVarType() {
		return varType;
	}
	
	void makeFinished() {
		URIs = new ConcurrentSkipListSet<URI>();
		individualSets = new ConcurrentSkipListSet<IndividualSet>();
		literalSets = new ConcurrentSkipListSet<LiteralSet>();
		
		URIsVirginity = false;
		individualsVirginity = false;
		literalsVirginity = false;
		
		hasURIsBecomeTheEmptySetAgain = true;
		hasIndividualsBecomeTheEmptySetAgain = true;
		hasLiteralsBecomeTheEmptySetAgain = true;
	}
	
	boolean hasURIsFinished() {
		if (URIsVirginity == false && hasURIsBecomeTheEmptySetAgain == true)
			return true;
		else
			return false;
	}
	
	boolean hasIndividualsFinished() {
		if (individualsVirginity == false && hasIndividualsBecomeTheEmptySetAgain == true)
			return true;
		else
			return false;
	}
	
	boolean hasLiteralsFinished() {
		if (literalsVirginity == false && hasLiteralsBecomeTheEmptySetAgain == true)
			return true;
		else
			return false;
	}
	
	Set<URI> getURISet() {
		return URIs;
	}
	
	Set<IndividualSet> getIndividualSets() {
		return individualSets;
	}
	
	Set<LiteralSet> getLiteralSets() {
		return literalSets;
	}
	
	Set<URI> getGroupRoots(Set<URI> classes) {
		Set<URI> groupRoots = new ConcurrentSkipListSet<URI>();
		groupRoots.addAll(classes);
		
		for (URI cls: classes) {
			for (URI cls2: classes) {
				if (Janus.ontBridge.isSubClassOf(cls2, cls) && !Janus.ontBridge.isEquivalentClass(cls2, cls))
					groupRoots.remove(cls2);
			}
		}
		
		return groupRoots;
	}
	
	void printResultSet() {
		for (IndividualSet individualSet: individualSets)
			individualSet.printIndividual(varName);
		
		int literalSetsSize = literalSets.size();
		if (literalSetsSize > 0) {
			
			String query = "";
			
			if (literalSetsSize == 1) {
				for (LiteralSet literalSet: literalSets)
					query = query + literalSet.generateQuery();
			} else {
				for (LiteralSet literalSet: literalSets)
					query = query + "(" + literalSet.generateQuery() + ") UNION ";
				query = query.substring(0, query.lastIndexOf("UNION"));
			}
			
			System.out.println(varName + ": " + query);
			
			Janus.dbBridge.executeQuery(query);
			
			int rowCount = Janus.dbBridge.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
				String cellData = rowData.get(0);
				
				System.out.println(varName + ": " + cellData);
			}
			
		}
	}
	
	void printResultSet(PrintWriter writer) {
		for (IndividualSet individualSet: individualSets)
			individualSet.printIndividual(writer, varName);
		
		int literalSetsSize = literalSets.size();
		if (literalSetsSize > 0) {
			
			String query = "";
			
			if (literalSetsSize == 1) {
				for (LiteralSet literalSet: literalSets)
					query = query + literalSet.generateQuery();
			} else {
				for (LiteralSet literalSet: literalSets)
					query = query + "(" + literalSet.generateQuery() + ") UNION ";
				query = query.substring(0, query.lastIndexOf("UNION"));
			}
			
			System.out.println(varName + ": " + query);
			
			Janus.dbBridge.executeQuery(query);
			
			int rowCount = Janus.dbBridge.getResultSetRowCount();
			for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
				List<String> rowData = Janus.dbBridge.getResultSetRowAt(rowIndex);
				String cellData = rowData.get(0);
				
				writer.println(varName + ": " + cellData);
			}
			
		}
	}
	
	void intersectIndividualSet(Set<IndividualSet> collection) {
		if (individualsVirginity) {
			individualSets.addAll(collection);
			individualsVirginity = false;
		} else {
			individualSets.retainAll(collection);
			collection.retainAll(individualSets);
			
			for (IndividualSet individualSet: individualSets)
				for (IndividualSet other: collection)
					if (individualSet.getClassURI().equals(other.getClassURI())) {
						individualSet.addAllSelectSet(other.getSelectSet());
						individualSet.addAllFromSet(other.getFromSet());
						individualSet.addAllJoinWhereSet(other.getJoinWhereSet());
						individualSet.addAllValueWhereSet(other.getValueWhereSet());

						break;
					}
		}
		
		if (individualSets.size() < 1)
			hasIndividualsBecomeTheEmptySetAgain = true;
	}
	
	void intersectLiteralSet(Set<LiteralSet> collection) {
		if (literalsVirginity) {
			literalSets.addAll(collection);
			literalsVirginity = false;
		} else {
			literalSets.retainAll(collection);
			collection.retainAll(literalSets);
			
			for (LiteralSet literalSet: literalSets)
				for (LiteralSet other: collection)
					if (literalSet.getClassURI().equals(other.getClassURI())) {
						literalSet.addAllFromSet(other.getFromSet());
						literalSet.addAllJoinWhereSet(other.getJoinWhereSet());
						literalSet.addAllValueWhereSet(other.getValueWhereSet());

						break;
					}
		}
		
		if (literalSets.size() < 1)
			hasLiteralsBecomeTheEmptySetAgain = true;
	}
	
	void intersectURIs(Set<URI> collection) {
		if (URIsVirginity) {
			URIs.addAll(collection);
			URIsVirginity = false;
		} else
			URIs.retainAll(collection);
		
		if (URIs.size() < 1)
			hasURIsBecomeTheEmptySetAgain = true;
	}

	void setVarType(VariableTypes varType) {
		if (this.varType.equals(varType) 
				|| varType.equals(VariableTypes.PROPERTIES) && (this.varType.equals(VariableTypes.DATA_PROPERTIES) || this.varType.equals(VariableTypes.OBJECT_PROPERTIES))
				|| varType.equals(VariableTypes.INDIVIDUALS_OR_LITERALS) && (this.varType.equals(VariableTypes.INDIVIDUALS) || this.varType.equals(VariableTypes.LITERALS)))
			return;
		
		if (this.varType.equals(VariableTypes.UNKNOWN) 
				|| (this.varType.equals(VariableTypes.PROPERTIES) && (varType.equals(VariableTypes.DATA_PROPERTIES) || varType.equals(VariableTypes.OBJECT_PROPERTIES)))
				|| (this.varType.equals(VariableTypes.INDIVIDUALS_OR_LITERALS) && (varType.equals(VariableTypes.INDIVIDUALS) || varType.equals(VariableTypes.LITERALS)))) {
			this.varType = varType;
			return;
		} 
		
		try {
			throw new Exception();
		} catch (Exception e) {
			System.out.println("The use of the variable" + varName + " is wrong.");
			makeFinished();
			e.printStackTrace();
		}
	}
}
