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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
class SubDPAssertionsTable extends JScrollPane {
	private JTable table;
	
	SubDPAssertionsTable(URI individual) {
		table = new JTable(new SubDPAssertionsTableModel(individual)) {
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String value = getValueAt(rowIndex, colIndex).toString();
		        if (colIndex == 0)
		        	return getDataProperty(value).toString();
		        else if (colIndex == 1)
		        	return getToolTipTextForLiteral(value, getCellRect(rowIndex, colIndex, true).width);
		        
		        return super.getToolTipText();
			}
		};
		table.setDefaultRenderer(Object.class, 
				new SubDPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP), 
											  new ImageIcon(ImageURIs.ONT_LITERAL)));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		table.getTableHeader().setReorderingAllowed(false);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	private String getToolTipTextForLiteral(String literal, int width) {
		return "<html><p width=\"" + width + "\">" + literal + "</p></html>";
	}
	
	private URI getDataProperty(String dpFragment) {
		return Janus.mappingMetadata.getDataProperty(dpFragment);
	}
}

@SuppressWarnings("serial")
class SubDPAssertionsTableModel extends AbstractTableModel {

	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;

	SubDPAssertionsTableModel(URI individual) {
		String query = Janus.sqlGenerator.getQueryToGetDPAssertionsOfSourceIndividual(individual);
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
		
		cache = new CachedRecord();
	}
	
	@Override
	public String getColumnName(int column) {
		String columnName = super.getColumnName(column);
		
		if (column == 0)
			columnName = "Data Property";
		else if (column == 1)
			columnName = "Target Value";
		
		return columnName;
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
class SubDPAssertionsTableRenderer extends DefaultTableCellRenderer {

	private Icon dpIcon;
	private Icon litIcon;
	
	SubDPAssertionsTableRenderer(Icon dpIcon, Icon litIcon) {
		this.dpIcon = dpIcon;
		this.litIcon = litIcon;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (column == 0)
			setIcon(dpIcon);
		else if (column == 1)
			setIcon(litIcon);
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
}