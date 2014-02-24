package janus.mapping.metadata;

import janus.database.DBColumn;
import janus.mapping.OntEntityTypes;

class PropertyMetadata implements Comparable<PropertyMetadata> {
	private String propertyFragment;

	private OntEntityTypes propertyType;
	
	private String mappedTable;
	private String mappedColumn;

	private String domain;
	private String range;
	
	private String superProperty;

	PropertyMetadata(String propertyFragment, OntEntityTypes propertyType, String mappedTable, String mappedColumn) {
		this.propertyFragment = propertyFragment;
		this.mappedTable = mappedTable;
		this.mappedColumn = mappedColumn;
		
		this.propertyType = propertyType;
	}
	
	String getMappedTable() {
		return mappedTable;
	}
	
	String getMappedColumnName() {
		return mappedColumn;
	}
	
	DBColumn getMappedColumn() {
		return new DBColumn(mappedTable, mappedColumn);
	}
	
	String getDomain() {
		return domain;
	}

	void setDomain(String domain) {
		this.domain = domain;
	}
	
	String getRange() {
		return range;
	}

	void setRange(String range) {
		this.range = range;
	}
	
	String getSuperProperty() {
		return superProperty;
	}

	void setSuperProperty(String superProperty) {
		this.superProperty = superProperty;
	}
	
	String getPropertyFragment() {
		return propertyFragment;
	}
	
	OntEntityTypes getPropertyType() {
		return propertyType;
	}

	@Override
	public int compareTo(PropertyMetadata o) {
		return propertyFragment.compareTo(o.getPropertyFragment());
	}
}
