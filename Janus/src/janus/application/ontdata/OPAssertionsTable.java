package janus.application.ontdata;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import janus.ImageURIs;
import janus.Janus;
import janus.database.SQLResultSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class OPAssertionsTable extends JScrollPane {
	private JTable table;
	
	OPAssertionsTable(URI op) {
		table = new JTable(new OPAssertionsTableModel(op)) {
			
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        return getIndividual((String)getValueAt(rowIndex, colIndex)).toString();
			}
		};
		table.setDefaultRenderer(Object.class, new OPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setDragEnabled(true);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	void addOPAssertionsTableSelectionListener(ListSelectionListener x) {
		table.getSelectionModel().addListSelectionListener(x);
		table.getColumnModel().getSelectionModel().addListSelectionListener(x);
	}
	
	private URI getIndividual(String individualFragment) {
		return Janus.mappingMetadata.getIndividual(individualFragment);
	}
	
	URI getSelectedIndividual() {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		
		if (row >= 0 && col >= 0)
			return getIndividual(table.getValueAt(row, col).toString());
		
		return null;
	}
}

@SuppressWarnings("serial")
class OPAssertionsTableModel extends AbstractTableModel {
	
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	OPAssertionsTableModel(URI op) {
		String query = Janus.sqlGenerator.getQueryToGetOPAsserionsOfOP(op);
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Source Individual";
		else if (column == 1)
			return "Target Individual";
		
		return super.getColumnName(column);
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Record record = cache.get(rowIndex);

		if (record == null) {
			record = getRecord(rowIndex);
			cache.put(rowIndex, record);
		}

		return record.get(columnIndex);
	}

	private Record getRecord(int rowIndex) {
		return new Record(resultSet.getResultSetRowAt(rowIndex + 1));
	}

	private class Record {
		private List<String> record;
		
		Record(List<String> record) {
			this.record = record;
		}
		
		String get(int columnIndex) {
			return record.get(columnIndex);
		}
	}

	private class CachedRecord extends LinkedHashMap<Integer,Record> {
		private static final int MAX_ENTRIES = 100;

		@Override
		protected boolean removeEldestEntry(Entry<Integer, Record> eldest) {
			return size() > MAX_ENTRIES;
		}
	}
}

@SuppressWarnings("serial")
class OPAssertionsTableRenderer extends DefaultTableCellRenderer {

	private Icon individualIcon;
	
	OPAssertionsTableRenderer(Icon individualIcon) {
		this.individualIcon = individualIcon;
	}
	
	@Override
	protected void setValue(Object value) {
		super.setValue(value);
		setIcon(individualIcon);
	}
}