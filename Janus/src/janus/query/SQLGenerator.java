package janus.query;

import java.util.List;

public abstract class SQLGenerator {
	
	/* Type I-I: when a table has one column primary key. 
	 * SELECT aPK FROM table
	 */
	public abstract String getQueryToGetIndividualsOfSinglePKColumnClass(String aPK, String table);
	
	/* Type I-II: when a table has multiple column primary key.
	 * SELECT pk1, pk2, ... , pkn FROM table
	 */
	public abstract String getQueryToGetIndividualsOfTableClass(List<String> pk, String table);
	
	/* Type I-III: to select a non-nullable key column.
	 * SELECT DISTINCT keyColumn FROM table
	 */
	public abstract String getQueryToGetIndividualsOfNonNullableColumnClass(String keyColumn, String table);
	
	/* Type I-IV: to select a nullable key column.
	 * SELECT DISTINCT keyColumn FROM table WHERE keyColumn IS NOT NULL
	 */
	public abstract String getQueryToGetIndividualsOfNullableColumnClass(String keyColumn, String table);
	
	public String getUnionQuery(List<String> queries) {
		StringBuffer query = new StringBuffer();
		
		for (int i = 0; i < queries.size() - 1; i++)
			query.append("(" + queries.get(i) +") UNION ");
		
		query.append("(" + queries.get(queries.size() - 1) + ")");
		
		return query.toString();
	}
}