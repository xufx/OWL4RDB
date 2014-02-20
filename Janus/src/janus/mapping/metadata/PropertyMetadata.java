package janus.mapping.metadata;

import janus.database.Column;
import janus.ontology.OWLEntityTypes;

class PropertyMetadata implements Comparable<PropertyMetadata> {
	private String propertyName;

	private OWLEntityTypes propertyType;
	
	private String mappedTable;
	private String mappedColumn;

	private String domain;
	private String range;
	
	private String superProperty;

	PropertyMetadata(String propertyName, OWLEntityTypes propertyType, String mappedTable, String mappedColumn) {
		this.propertyName = propertyName;
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
	
	Column getMappedColumn() {
		return new Column(mappedTable, mappedColumn);
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
	
	String getPropertyName() {
		return propertyName;
	}
	
	OWLEntityTypes getPropertyType() {
		return propertyType;
	}

	@Override
	public int compareTo(PropertyMetadata o) {
		return propertyName.compareTo(o.getPropertyName());
	}
}
