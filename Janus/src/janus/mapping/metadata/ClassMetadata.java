package janus.mapping.metadata;

import janus.database.Column;
import janus.mapping.ClassTypes;

class ClassMetadata implements Comparable<ClassMetadata> {
	private String className;

	private ClassTypes classType; // COLUMN_CLASS or TABLE_CLASS

	private String mappedTable;
	private String mappedColumn;
	
	private String superClassName;

	ClassMetadata(String className, String mappedTable, String mappedColumn) {
		this.className = className;
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;
		
		classType = ClassTypes.COLUMN_CLASS;
	}
	
	ClassMetadata(String className, String mappedTable) {
		this.className = className;
		this.mappedTable = mappedTable;
		
		classType = ClassTypes.TABLE_CLASS;
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
	
	ClassTypes getClassType() {
		return classType;
	}

	@Override
	public int compareTo(ClassMetadata o) {
		return className.compareTo(o.getClassName());
	}
}
