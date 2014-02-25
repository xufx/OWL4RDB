package janus.mapping;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class DatatypeMap {
	private static Map<Integer, String> map = new Hashtable<Integer, String>(36);
	
	static {
		map.put(Integer.valueOf(java.sql.Types.BIGINT), "xsd:long");
		map.put(Integer.valueOf(java.sql.Types.BINARY), "xsd:base64Binary");
		//map.put(Integer.valueOf(java.sql.Types.BIT), "xsd:short");
		map.put(Integer.valueOf(java.sql.Types.BIT), "xsd:boolean"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.BLOB), "xsd:base64Binary");
		map.put(Integer.valueOf(java.sql.Types.BOOLEAN), "xsd:boolean");
		map.put(Integer.valueOf(java.sql.Types.CHAR), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.CLOB), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.DATALINK), "xsd:anyURI");
		map.put(Integer.valueOf(java.sql.Types.DATE), "xsd:dateTime"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.DECIMAL), "xsd:decimal");
		map.put(Integer.valueOf(java.sql.Types.DOUBLE), "xsd:double");
		//map.put(Integer.valueOf(java.sql.Types.DOUBLE), "xsd:decimal"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.FLOAT), "xsd:float");
		map.put(Integer.valueOf(java.sql.Types.INTEGER), "xsd:int");
		map.put(Integer.valueOf(java.sql.Types.LONGVARBINARY), "xsd:base64Binary");
		map.put(Integer.valueOf(java.sql.Types.LONGVARCHAR), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.NUMERIC), "xsd:decimal");
		//map.put(Integer.valueOf(java.sql.Types.NUMERIC), "xsd:integer"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.REAL), "xsd:float");
		//map.put(Integer.valueOf(java.sql.Types.REAL), "xsd:double"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.SMALLINT), "xsd:short");
		map.put(Integer.valueOf(java.sql.Types.TIME), "xsd:dateTime"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.TIMESTAMP), "xsd:dateTime");
		map.put(Integer.valueOf(java.sql.Types.TINYINT), "xsd:short");
		//map.put(Integer.valueOf(java.sql.Types.TINYINT), "xsd:byte"); //WebLogic
		map.put(Integer.valueOf(java.sql.Types.VARBINARY), "xsd:base64Binary");
		map.put(Integer.valueOf(java.sql.Types.VARCHAR), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.ARRAY), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.DISTINCT), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.JAVA_OBJECT), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.NULL), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.OTHER), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.REF), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.STRUCT), "xsd:string");
		map.put(Integer.valueOf(java.sql.Types.ROWID), "xsd:base64Binary");
		
		//map.put(Integer.valueOf(java.sql.Types.SQLXML), "xsd:anyType");
		//map.put(Integer.valueOf(java.sql.Types.LONGNVARCHAR), "xsd:string");
		//map.put(Integer.valueOf(java.sql.Types.NCHAR), "xsd:string");
		//map.put(Integer.valueOf(java.sql.Types.NCLOB), "xsd:string");
		//map.put(Integer.valueOf(java.sql.Types.NVARCHAR), "xsd:string");
	}
	
	public static String get(int key) {
		return map.get(Integer.valueOf(key));
	}
	
	public static Set<Integer> getMappedSQLTypes(String anXSD) {
		Set<Integer> mappedSQLTypes = new ConcurrentSkipListSet<Integer>();
		
		Set<Integer> keySet = map.keySet();
		
		for (Integer key: keySet)
			if (map.get(key).equals(anXSD))
				mappedSQLTypes.add(key);
		
		return mappedSQLTypes;
	}
}
