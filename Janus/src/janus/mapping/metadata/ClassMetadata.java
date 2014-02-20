package janus.mapping.metadata;

import janus.database.Column;
import janus.ontology.OWLEntityTypes;

class ClassMetadata implements Comparable<ClassMetadata> {
	private String className;

	private OWLEntityTypes classType; // COLUMN_CLASS or TABLE_CLASS

	private String mappedTable;
	private String mappedColumn;
	
	private String superClassName;

	ClassMetadata(String className, String mappedTable, String mappedColumn) {
		this.className = className;
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;
		
		classType = OWLEntityTypes.COLUMN_CLASS;
	}
	
	ClassMetadata(String className, String mappedTable) {
		this.className = className;
		this.mappedTable = mappedTable;
		
		classType = OWLEntityTypes.TABLE_CLASS;
	}
	
	void setSuperClassName(String superClassName) {
		this.superClassName = superClassName;
	}
	
	String getSuperClassName() {
		return superClassName;
	}
	
	String getClassName() {
		return className;
	}
	
	String getMappedTableName() {
		return mappedTable;
	}
	
	String getMappedColumnName() {
		return mappedColumn;
	}
	
	Column getMappedColumn() {
		return new Column(mappedTable, mappedColumn);
	}
	
	OWLEntityTypes getClassType() {
		return classType;
	}

	@Override
	public int compareTo(ClassMetadata o) {
		return className.compareTo(o.getClassName());
	}
}
