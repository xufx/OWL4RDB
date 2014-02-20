package janus.application.ontdata;

import janus.TabNames;

import java.net.URI;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings("serial")
public class AssertionsPane extends JSplitPane implements ListSelectionListener {
	private URI cls;
	
	private ClsAssertionsTable clsAssertionsTable;
	private JTabbedPane types;
	private JTabbedPane opAssertions;
	private JTabbedPane dpAssertions;
	
	public AssertionsPane(URI cls) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		
		this.cls = cls;
		
		setResizeWeight(0.5);
		setOneTouchExpandable(true);
		
		buildUI();
	}
	
	private void buildUI() {
		JTabbedPane members = new JTabbedPane();
		members.addTab(TabNames.CLASS_ASSERTIONS, buildMembersPane());
		
		setLeftComponent(members);
		
		JSplitPane rightSP = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		rightSP.setResizeWeight(0.5);
		rightSP.setOneTouchExpandable(true);
		
		types = new JTabbedPane();
		types.addTab(TabNames.TYPES, null);
		
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
		clsAssertionsTable = new ClsAssertionsTable(cls);
		clsAssertionsTable.addMembersTableSelectionListener(this);
		
		return clsAssertionsTable;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		types.setComponentAt(types.indexOfTab(TabNames.TYPES), new TypesTable(clsAssertionsTable.getSelectedMember()));
		opAssertions.setComponentAt(opAssertions.indexOfTab(TabNames.OBJECT_PROPERTY_ASSERTIONS), new OPAssertionsTable(clsAssertionsTable.getSelectedMember()));
		dpAssertions.setComponentAt(dpAssertions.indexOfTab(TabNames.DATA_PROPERTY_ASSERTIONS), new DPAssertionsTable(clsAssertionsTable.getSelectedMember()));
	}
}