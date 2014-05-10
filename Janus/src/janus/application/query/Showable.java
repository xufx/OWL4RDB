package janus.application.query;

import janus.database.SQLResultSet;


public interface Showable {
	void showResult(QueryTypes submissionType);
	void showResult(SQLResultSet resultSet);
	void showResult(boolean result);
}
