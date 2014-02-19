package janus.mapping.metadata;

import janus.Janus;
import janus.database.DBField;
import janus.mapping.OntMapper;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class IndividualMetadata {
	
	static String getMappedRecordIndividualFragment(String table, List<DBField> pkFields) {
		String rootTable = Janus.cachedDBMetadata.getRootTable(table);
		
		List<DBField> pkRootFields = new Vector<DBField>();
		
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
	
	static URI getMappedRecordIndividual(String table, List<DBField> pkFields) {
		String fragment = getMappedRecordIndividualFragment(table, pkFields);
		
		try {
			return URI.create(getIndividualStringOfFragment(fragment));
		} catch (IllegalArgumentException e) {
			return getEncodedIndividual(fragment);
		}
	}
	
	static URI getMappedFieldIndividual(String table, String column, String value) {
		String fragment = getMappedFieldIndividualFragment(table, column, value);
		
		try {
			return URI.create(getIndividualStringOfFragment(fragment));
		} catch (IllegalArgumentException e) {
			return getEncodedIndividual(fragment);
		}
	}
	
	static String getMappedFieldIndividualFragment(String table, String column, String value) {
		String rootColumn = Janus.cachedDBMetadata.getRootTableDotColumn(table, column);

		String[] tableDotColumn = rootColumn.split("\\.");
		String rootTableName = tableDotColumn[0];
		String rootColumnName = tableDotColumn[1]; 
		
		String individualFragment = OntMapper.TABLE_NAME + OntMapper.IS + rootTableName 
									+ OntMapper.AND + OntMapper.COLUMN_NAME + OntMapper.IS + rootColumnName 
									+ OntMapper.AND + OntMapper.VALUE + OntMapper.IS + value;
		
		return individualFragment;
	}
	
	static String getMappedTableNameToRecordIndividual(URI individual) {
		String fragment = individual.getFragment();
		
		String[] tokens = fragment.split("&");
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME)) {
				return pair[1];
			}
		}
		
		return null;
	}
	
	static List<DBField> getMappedDBFieldsToRecordIndividual(URI individual) {
		List<DBField> fields = new Vector<DBField>();
		
		String tableName = null;
		List<String> columnNames = new Vector<String>();
		List<String> values = new Vector<String>();
		
		String fragment = getDecodedIndividualFragment(individual.getRawFragment());
		
		String[] tokens = fragment.split("&");
		
		try {
			if (tokens.length < 3 || ((tokens.length % 2) != 1))
				throw new IndividualParsingException();
		} catch (IndividualParsingException e) {
				e.printStackTrace();
		}
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME))
				tableName =  pair[1];
			if (pair[0].equals(OntMapper.PK_COLUMN_NAME))
				columnNames.add(pair[1]);
			if (pair[0].equals(OntMapper.VALUE))
				values.add(pair[1]);
		}
		
		for (int i = 0; i < (tokens.length)/2; i++)
			fields.add(new DBField(tableName, columnNames.get(i), values.get(i)));
		
		return fields;
	}
	
	static DBField getMappedDBFieldToFieldIndividual(URI individual) {
		String tableName = null;
		String columnName = null;
		String value = null;
		
		String fragment = getDecodedIndividualFragment(individual.getRawFragment());
		
		String[] tokens = fragment.split("&");
		
		try {
			if (tokens.length != 3)
				throw new IndividualParsingException();
		} catch (IndividualParsingException e) {
				e.printStackTrace();
		}
		
		for (String token: tokens) {
			String[] pair = token.split("=");
			if (pair[0].equals(OntMapper.TABLE_NAME))
				tableName =  pair[1];
			if (pair[0].equals(OntMapper.COLUMN_NAME))
				columnName =  pair[1];
			if (pair[0].equals(OntMapper.VALUE))
				value = pair[1];
		}
		
		return new DBField(tableName, columnName, value);
	}
	
	static IndividualTypes getIndividualType(URI individual) {
		if (Pattern.matches(IndividualTypes.FIELD_INDIVIDUAL.value(), individual.toString()))
			return IndividualTypes.FIELD_INDIVIDUAL;
		else if (Pattern.matches(IndividualTypes.RECORD_INDIVIDUAL.value(), individual.toString()))
			return IndividualTypes.RECORD_INDIVIDUAL;
		
		return null;
	}
	
	/*private static IndividualTypes getIndividualTypeWithDecodedFragment(URI individual) {
		
		String rawFragment = individual.getRawFragment();
		String decodedFragment = getDecodedIndividualFragment(rawFragment);
		String individualString = getIndividualStringOfFragment(decodedFragment);
			
		if (Pattern.matches(IndividualTypes.FIELD_INDIVIDUAL.value(), individualString))
			return IndividualTypes.FIELD_INDIVIDUAL;
		else if (Pattern.matches(IndividualTypes.RECORD_INDIVIDUAL.value(), individualString))
			return IndividualTypes.RECORD_INDIVIDUAL;
		
		return null;
	}*/
	
	static URI getIndividual(String individualFragment) {
		try {
			return  new URI(getIndividualStringOfFragment(individualFragment));
		} catch (URISyntaxException e) {
			return getEncodedIndividual(individualFragment);
		}
	}
	
	private static URI getEncodedIndividual(String individualFragment) {
		return URI.create(getIndividualStringOfFragment(getEncodedIndividualFragment(individualFragment)));
		
	}
	
	private static String getIndividualStringOfFragment(String fragment) {
		return Janus.ontBridge.getOntologyID() + "#" + fragment;
	}
	
	private static String getEncodedIndividualFragment(String fragment) {
		try {
			return URLEncoder.encode(fragment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static String getDecodedIndividualFragment(String encodedFragment) {
		try {
			return URLDecoder.decode(encodedFragment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}