package janus.query.rewriter;

import janus.database.DBMSTypes;

public class SQLGeneratorFactory {
	public static SQLGenerator getSQLGenerator(DBMSTypes DBMS_TYPE) {
		SQLGenerator generator = null;
		
		if(DBMS_TYPE.equals(DBMSTypes.MARIADB))
			generator = MariaDBSQLGenerator.getInstance();
		
		return generator;
	}
}