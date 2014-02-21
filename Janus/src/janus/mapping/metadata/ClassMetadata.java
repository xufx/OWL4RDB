package janus.mapping.metadata;

import janus.database.DBColumn;
import janus.mapping.OntEntityTypes;

class ClassMetadata implements Comparable<ClassMetadata> {
	private String className;

	private OntEntityTypes classType; // COLUMN_CLASS or TABLE_CLASS

	private String mappedTable;
	private String mappedColumn;
	
	private String superClassName;

	ClassMetadata(String className, String mappedTable, String mappedColumn) {
		this.className = className;
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;
		
		classType = OntEntityTypes.COLUMN_CLASS;
	}
	
	ClassMetadata(String className, String mappedTable) {
		this.className = className;
		this.mappedTable = mappedTable;
		
		classType = OntEntityTypes.TABLE_CLASS;
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
	
	DBColumn getMappedColumn() {
		return new DBColumn(mappedTable, mappedColumn);
	}
	
	OntEntityTypes getClassType() {
		return classType;
	}

	@Override
	public int compareTo(ClassMetadata o) {
		return className.compareTo(o.getClassName());
	}
}
