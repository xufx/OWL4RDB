package janus.application.ontdata;

import janus.TabNames;

import java.net.URI;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class IndividualPane extends JSplitPane {
	private URI cls;
	
	private JTabbedPane types;
	private JTabbedPane opAssertions;
	private JTabbedPane dpAssertions;
	
	public IndividualPane(URI cls) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		
		this.cls = cls;
		
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI();
	}
	
	private void buildUI() {
		JTabbedPane members = new JTabbedPane();
		members.addTab(TabNames.MEMBERS, buildMembersPane());
		
		setLeftComponent(members);
		
		JSplitPane rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightSP.setResizeWeight(0.5);
		rightSP.setOneTouchExpandable(true);
		
		types = new JTabbedPane();
		types.addTab(TabNames.TYPES, new TypeList());
		
		rightSP.setTopComponent(types);
		
		JSplitPane rightBottomSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightBottomSP.setResizeWeight(0.5);
		rightBottomSP.setOneTouchExpandable(true);
		
		opAssertions = new JTabbedPane();
		opAssertions.addTab(TabNames.OBJECT_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setTopComponent(opAssertions);
		
		dpAssertions = new JTabbedPane();
		dpAssertions.addTab(TabNames.DATA_PROPERTY_ASSERTIONS, null);
		
		rightBottomSP.setBottomComponent(dpAssertions);
		
		rightSP.setBottomComponent(rightBottomSP);
		
		setRightComponent(rightSP);
	}
	
	private JScrollPane buildMembersPane() {
		MembersTable table = new MembersTable(cls);
		table.addMembersTableSelectionListener(new MembersTableSelectionListener(table.getSelectionSource()));
		
		return table;
	}
}

class MembersTableSelectionListener implements ListSelectionListener {
	private JTable table;
	
	MembersTableSelectionListener(JTable table) {
		this.table = table;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		System.out.println(table.getValueAt(table.getSelectedRow(), table.getSelectedColumn()));
		
	}
	
}

