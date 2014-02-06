package janus.mapping.metadata;

class ClassMetadata implements Comparable<ClassMetadata> {
	private String className;

	private ClassTypes classType; // COLUMN_CLASS or TABLE_CLASS

	private String mappedTable;
	private String mappedColumn;

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
	
	public void setSuperClassName(String superClassName) {
	}
	
	public String getClassName() {
		return className;
	}
	
	public String getMappedTableName() {
		return mappedTable;
	}
	
	public String getMappedColumnName() {
		return mappedColumn;
	}
	
	public ClassTypes getClassType() {
		return classType;
	}

	@Override
	public int compareTo(ClassMetadata o) {
		return className.compareTo(o.getClassName());
	}
}
