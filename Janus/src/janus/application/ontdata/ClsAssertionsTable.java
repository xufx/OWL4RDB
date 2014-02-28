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
import janus.mapping.OntEntity;

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
class ClsAssertionsTable extends JScrollPane {
	private JTable table;
	
	ClsAssertionsTable(URI cls) {
		table = new JTable(new ClsAssertionsTableModel(cls)) {

			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String curie = getValueAt(rowIndex, colIndex).toString();
		        
		        return OntEntity.getURI(curie).toString();
			}
		};
		table.setDefaultRenderer(Object.class, new ClsAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL)));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	void addClsAssertionsTableSelectionListener(ListSelectionListener x) {
		table.getSelectionModel().addListSelectionListener(x);
	}
	
	URI getSelectedIndividual() {
		String curie = table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString();
        
        return OntEntity.getURI(curie);
	}
}

@SuppressWarnings("serial")
class ClsAssertionsTableModel extends AbstractTableModel {
	
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	ClsAssertionsTableModel(URI cls) {
		String query = Janus.sqlGenerator.getQueryToGetIndividualsOfClass(cls);
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Individual";
		
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
class ClsAssertionsTableRenderer extends DefaultTableCellRenderer {

	private Icon individualIcon;
	
	ClsAssertionsTableRenderer(Icon individualIcon) {
		this.individualIcon = individualIcon;
	}
	
	@Override
	protected void setValue(Object value) {
		super.setValue(value);
		setIcon(individualIcon);
	}
}
