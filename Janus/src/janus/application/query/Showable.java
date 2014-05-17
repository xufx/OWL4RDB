package janus.application.query;

import javax.swing.table.TableModel;


public interface Showable {
	void showResult(TableModel resultSet);
	void showResult(boolean result);
}
