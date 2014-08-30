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
import janus.mapping.OntEntityTypes;

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
	
	private SubDPAssertionsTable(final OntEntityTypes entityType) {
		table = new JTable() {
			@Override
			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int rowIndex = rowAtPoint(p);
		        int colIndex = columnAtPoint(p);
		        
		        String value = getValueAt(rowIndex, colIndex).toString();
		        
		        if (colIndex == 0)
	        		return  OntEntity.getURI(value).toString();
		        
		        if (entityType.equals(OntEntityTypes.RECORD_INDIVIDUAL) 
						|| entityType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
		        	if (colIndex == 1)
		        		return getToolTipTextForLiteral(value, getCellRect(rowIndex, colIndex, true).width);
		        } else if (entityType.equals(OntEntityTypes.TYPED_LITERAL)) {
		        	if (colIndex == 1)
		        		return  OntEntity.getURI(value).toString();
		        }
		        
		        return super.getToolTipText();
			}
		};
		table.setDefaultRenderer(Object.class, 
				new SubDPAssertionsTableRenderer(new ImageIcon(ImageURIs.ONT_INDIVIDUAL), 
												 new ImageIcon(ImageURIs.ONT_NAMED_DATA_PROP), 
												 new ImageIcon(ImageURIs.ONT_LITERAL),
												 entityType));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDragEnabled(true);
		table.getTableHeader().setReorderingAllowed(false);
		((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
		
		setViewportView(table);
	}
	
	SubDPAssertionsTable(URI individual) {
		this(Janus.mappingMetadata.getIndividualType(individual));
		
		table.setModel(new SubDPAssertionsTableModel(individual));
	}
	
	SubDPAssertionsTable(String literal) {
		this(OntEntityTypes.TYPED_LITERAL);
		
		table.setModel(new SubDPAssertionsTableModel(literal));
	}
	
	private String getToolTipTextForLiteral(String literal, int width) {
		return "<html><p width=\"" + width + "\">" + literal + "</p></html>";
	}
}

@SuppressWarnings("serial")
class SubDPAssertionsTableModel extends AbstractTableModel {
	private int columnCount;
	private int rowCount;
	private CachedRecord cache;
	
	private SQLResultSet resultSet;
	
	private SubDPAssertionsTableModel() {
		cache = new CachedRecord();
	}

	SubDPAssertionsTableModel(URI individual) {
		this();
		
		Janus.mappingMetadata.getIndividualType(individual);
		
		String query = Janus.sqlGenerator.getQueryToGetDPAssertionsOfSourceIndividual(individual, "'Data Property'", "'Target Value'");
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
	}
	
	SubDPAssertionsTableModel(String literal) {
		this();
		
		String query = Janus.sqlGenerator.getQueryToGetDPAssertionsOfTargetLiteral(literal, "'Data Property'", "'Source Individual'");
		
		resultSet = Janus.dbBridge.executeQuery(query);
		
		rowCount = resultSet.getResultSetRowCount();
		columnCount = resultSet.getResultSetColumnCount();
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
class SubDPAssertionsTableRenderer extends DefaultTableCellRenderer {
	private Icon indIcon;
	private Icon dpIcon;
	private Icon litIcon;
	
	private OntEntityTypes entityType;
	
	SubDPAssertionsTableRenderer(Icon indIcon, Icon dpIcon, Icon litIcon, OntEntityTypes entityType) {
		this.indIcon = indIcon;
		this.dpIcon = dpIcon;
		this.litIcon = litIcon;
		
		this.entityType = entityType;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (column == 0)
			setIcon(dpIcon);
		
		if (entityType.equals(OntEntityTypes.RECORD_INDIVIDUAL) 
				|| entityType.equals(OntEntityTypes.FIELD_INDIVIDUAL)) {
			if (column == 1)
				setIcon(litIcon);
		} else if (entityType.equals(OntEntityTypes.TYPED_LITERAL)) {
			if (column == 1)
				setIcon(indIcon);
		}
		
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
	}
}