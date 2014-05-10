package janus.application.query;

import java.util.List;
import java.util.Vector;

import janus.Janus;
import janus.database.SQLResultSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class ResultSetPane extends JScrollPane implements Showable {
	
	public void showResult(QueryTypes submissionType) {
		if (submissionType.equals(QueryTypes.SQL)) {
			int columnCount = Janus.dbBridge.getResultSetColumnCount();

			Vector<String> columnNames = new Vector<String>(columnCount);
			for(int column = 1; column <= columnCount; column++)
				columnNames.addElement(Janus.dbBridge.getResultSetColumnLabel(column));

			Vector<List<String>> rowData = new Vector<List<String>>();
			int rowCount = Janus.dbBridge.getResultSetRowCount();
			for(int row = 1; row <= rowCount;row++)
				rowData.addElement(Janus.dbBridge.getResultSetRowAt(row));

			JTable table = new JTable(rowData, columnNames);

			setViewportView(table);
		}
	}
	
	public void showResult(boolean result) {
		
		String[] columnNames = {""};

		String[][] rowData = {{String.valueOf(result)}};

		JTable table = new JTable(rowData, columnNames);

		setViewportView(table);
	}
	
	public void showResult(SQLResultSet resultSet) {}
}
