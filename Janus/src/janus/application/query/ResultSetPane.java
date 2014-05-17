package janus.application.query;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ResultSetPane extends JScrollPane implements Showable {
	
	public void showResult(boolean result) {
		
		String[] columnNames = {""};

		String[][] rowData = {{String.valueOf(result)}};

		JTable table = new JTable(rowData, columnNames);

		setViewportView(table);
	}
	
	public void showResult(TableModel resultSet) {
		JTable table = new JTable(resultSet);

		setViewportView(table);
	}
}
