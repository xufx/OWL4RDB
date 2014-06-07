package janus.application.ontdata;

import java.awt.Component;
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
class DPAssertionsTable extends JScrollPane {
	private JTable table;
	
	DPAssertionsTable(URI dp) {
		table = new JTable(new DPAssertionsTableModel(dp)) {
			
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String value = getValueAt(rowIndex, colIndex).toString();
		        if (colIndex == 0)
		        	return OntEntity.getURI(value).toString();
		        else if (colIndex == 1)
		        	return getToolTipTextForLiteral(value, getCellRect(rowIndex, colIndex, true).width);
		        
		        return super.getToolTipText();
			}
		};
		table.setDefaultRenderer(Object.class, new DPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL),
																		     new ImageIcon(ImageURIs.ONT_LITERAL)));
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setDragEnabled(true);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	void addDPAssertionsTableSelectionListener(ListSelectionListener x) {
		table.getSelectionModel().addListSelectionListener(x);
		table.getColumnModel().getSelectionModel().addListSelectionListener(x);
	}
	
	private String getToolTipTextForLiteral(String literal, int width) {
		return "<html><p width=\"" + width + "\">" + literal + "</p></html>";
	}
	
	URI getSelectedIndividual() {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		
		if (row >= 0 && col == 0)
			return OntEntity.getURI(table.getValueAt(row, col).toString());
		
		return null;
	}
	
	String getSelectedLiteral() {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		
		if (row >= 0 && col == 1)
			return table.getValueAt(row, col).toString();
		
		return null;
	}
}

@SuppressWarnings("serial")
class DPAssertionsTableModel extends AbstractTableModel {
	
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	DPAssertionsTableModel(URI dp) {
		String query = Janus.sqlGenerator.getQueryToGetDPAsserionsOfDP(dp, "'Source Individual'", "'Target Value'");
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		int index = column + 1;
		
		if (column == 0 || column == 1)
			return resultSet.getColumnName(index);
		
		return super.getColumnName(index);
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
class DPAssertionsTableRenderer extends DefaultTableCellRenderer {

	private Icon individualIcon;
	private Icon litIcon;
	
	DPAssertionsTableRenderer(Icon individualIcon, Icon litIcon) {
		this.individualIcon = individualIcon;
		this.litIcon = litIcon;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (column == 0)
			setIcon(individualIcon);
		else if (column == 1)
			setIcon(litIcon);
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
}