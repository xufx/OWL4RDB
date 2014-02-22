package janus.application;

import janus.Janus;
import janus.application.dbscheme.DBTree;
import janus.application.dbscheme.DBTreePopupTrigger;
import janus.application.dbscheme.DBTreeSelectionListener;
import janus.application.description.AttrDescribable;
import janus.application.ontscheme.OntTreePopupTrigger;
import janus.application.ontscheme.ClsTreeSelectionListener;
import janus.application.ontscheme.DPTreeSelectionListener;
import janus.application.ontscheme.OPTreeSelectionListener;
import janus.application.ontscheme.OntTree;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

class EventHandlerBuilder {
	
	static void buildHandlers() {
		buildWindowHandler();
		
		buildSchemeTreesHandlers();
	}
	
	private static void buildWindowHandler() {
		JFrame window = UIRegistry.getWindow();
		
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Janus.dbBridge.disconnect();
				Janus.ontBridge.disposeReasoner();
			}
		});
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static void buildSchemeTreesHandlers() {
		connectDBTreeSelectionListener();
		connectDBTreePopupTrigger();
		connectClsTreeSelectionListener();
		connectClsTreePopupTrigger();
		connectOPTreeSelectionListener();
		connectOPTreePopupTrigger();
		connectDPTreeSelectionListener();
		connectDPTreePopupTrigger();
	}
	
	private static void connectDBTreeSelectionListener() {
		DBTree tree = UIRegistry.getDBTree();
		AttrDescribable announcer = UIRegistry.getDescriptionTree();
		DBTreeSelectionListener listener = new DBTreeSelectionListener(announcer);
		tree.addTreeSelectionListener(listener);
	}
	
	private static void connectDBTreePopupTrigger() {
		DBTree tree = UIRegistry.getDBTree();
		DBTreePopupTrigger trigger = new DBTreePopupTrigger(tree);
		tree.addTreePopupTrigger(trigger);
	}
	
	private static void connectDPTreeSelectionListener() {
		OntTree tree = UIRegistry.getDPTree();
		AttrDescribable announcer = UIRegistry.getDescriptionTree();
		DPTreeSelectionListener listener = new DPTreeSelectionListener(announcer);
		tree.addTreeSelectionListener(listener);
	}
	
	private static void connectOPTreeSelectionListener() {
		OntTree tree = UIRegistry.getOPTree();
		AttrDescribable announcer = UIRegistry.getDescriptionTree();
		OPTreeSelectionListener listener = new OPTreeSelectionListener(announcer);
		tree.addTreeSelectionListener(listener);
	}
	
	private static void connectClsTreeSelectionListener() {
		OntTree tree = UIRegistry.getClsTree();
		AttrDescribable announcer = UIRegistry.getDescriptionTree();
		ClsTreeSelectionListener listener = new ClsTreeSelectionListener(announcer);
		tree.addTreeSelectionListener(listener);
	}
	
	private static void connectClsTreePopupTrigger() {
		OntTree tree = UIRegistry.getClsTree();
		OntTreePopupTrigger trigger = new OntTreePopupTrigger(tree);
		tree.addTreePopupTrigger(trigger);
	}
	
	private static void connectOPTreePopupTrigger() {
		OntTree tree = UIRegistry.getOPTree();
		OntTreePopupTrigger trigger = new OntTreePopupTrigger(tree);
		tree.addTreePopupTrigger(trigger);
	}
	
	private static void connectDPTreePopupTrigger() {
		OntTree tree = UIRegistry.getDPTree();
		OntTreePopupTrigger trigger = new OntTreePopupTrigger(tree);
		tree.addTreePopupTrigger(trigger);
	}
}
