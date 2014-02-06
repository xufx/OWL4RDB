package janus.mapping.metadata;

import janus.Janus;
import janus.mapping.OntMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class IndividualMetadata {
	static String getMappedIndividualFragment(String table, List<DBField> pkFields) {
		String rootTable = Janus.cachedDBMetadata.getRootTable(table);
		
		List<DBField> pkRootFields = new ArrayList<DBField>();
		
		for (DBField pkField: pkFields) {
			String srcColumn = pkField.getColumnName();
			
			String matchedColumn = Janus.cachedDBMetadata.getMatchedPKColumnAmongFamilyTables(table, srcColumn, rootTable);
			
			DBField pkRootField = new DBField(rootTable, matchedColumn, pkField.getValue());
			
			pkRootFields.add(pkRootField);
		} 
		
		String individualFragment = OntMapper.TABLE_NAME + OntMapper.IS + rootTable;
		
		for (DBField pkField: pkRootFields) {
			String column = pkField.getColumnName();
			String value = pkField.getValue();
			
			individualFragment = individualFragment 
								 + OntMapper.AND + OntMapper.PK_COLUMN_NAME + OntMapper.IS + column 
								 + OntMapper.AND + OntMapper.VALUE + OntMapper.IS + value;
		} 
		
		return individualFragment;
	}
	
	static URI getMappedIndividual(String table, List<DBField> pkFields) {
		URI uri = null; 
		
		try {
			uri = new URI(Janus.mappingMetadata.getOntologyID() + "#" + getMappedIndividualFragment(table, pkFields));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri;
	}
	
	static URI getMappedIndividual(String table, String column, String value) {
		URI uri = null; 
		
		try {
			uri = new URI(Janus.mappingMetadata.getOntologyID() + "#" + getMappedIndividualFragment(table, column, value));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return uri;
	}
	
	static String getMappedIndividualFragment(String table, String column, String value) {
		String rootColumn = Janus.cachedDBMetadata.getRootColumn(table, column);

		String[] tableDotColumn = rootColumn.split("\\.");
		String rootTableName = tableDotColumn[0];
		String rootColumnName = tableDotColumn[1]; 
		
		String individualFragment = OntMapper.TABLE_NAME + OntMapper.IS + rootTableName 
									+ OntMapper.AND + OntMapper.COLUMN_NAME + OntMapper.IS + rootColumnName 
									+ OntMapper.AND + OntMapper.VALUE + OntMapper.IS + value;
		
		return individualFragment;
	}
}
