package janus.mapping.metadata;

import janus.Janus;
import janus.mapping.DatatypeMap;

class LiteralMetadata {
	static String getMappedLiteral(String table, String column, String value) {
		int sqlDataType = Janus.cachedDBMetadata.getDataType(table, column);
		String xmlSchemaDataType = DatatypeMap.get(sqlDataType);
		String literal = "\"" + value + "\"^^" + xmlSchemaDataType;
		
		return literal;
	}
	
	static String getDatatype(String typedLiteral) {
		return typedLiteral.substring(typedLiteral.lastIndexOf("^^")+2);
	}
	
	static String getLexicalValue(String typedLiteral) {
		return typedLiteral.substring(typedLiteral.indexOf("\"")+1, typedLiteral.lastIndexOf("\"^^" + getDatatype(typedLiteral)));
	}
}
