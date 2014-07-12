package janus.query.sparqldl;

import java.util.Vector;

interface SPARQLDLResultSet {
	boolean isEmptySet();
	
	SPARQLDLResultSet getNaturalJoinedResultSet(SPARQLDLResultSet arg);
	
	String getQuery();
	
	Vector<String> getColumnNames();
}
