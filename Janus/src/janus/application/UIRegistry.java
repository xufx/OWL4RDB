package janus.application;

import janus.application.dbscheme.DBTree;
import janus.application.description.DescrTree;
import janus.application.ontscheme.ClsTree;
import janus.application.ontscheme.DPTree;
import janus.application.ontscheme.OPTree;
import janus.application.query.Showable;
import janus.application.query.Submittable;

import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class UIRegistry {
	private static JFrame frame;
	private static JTabbedPane queryPane;
	private static JScrollPane resultSetPane;
	private static JScrollPane dbSchemePane;
	private static JScrollPane descriptionPane;
	private static JScrollPane dataPropertyPane;
	private static JScrollPane objectPropertyPane;
	private static JScrollPane classPane;
	
	static void registerWindow(JFrame frame) {
		UIRegistry.frame = frame;
	}
	
	static void registerQueryPane(JTabbedPane queryPane) {
		UIRegistry.queryPane = queryPane;
	}
	
	static void registerResultSetPane(JScrollPane resultSetPane) {
		UIRegistry.resultSetPane = resultSetPane;
	}
	
	static void registerDBSchemePane(JScrollPane dbSchemePane) {
		UIRegistry.dbSchemePane = dbSchemePane;
	}
	
	static void registerDescriptionPane(JScrollPane descriptionPane) {
		UIRegistry.descriptionPane = descriptionPane;
	}
	
	static void registerDataPropertyPane(JScrollPane dataPropertyPane) {
		UIRegistry.dataPropertyPane = dataPropertyPane;
	}
	
	static void registerObjectPropertyPane(JScrollPane objectPropertyPane) {
		UIRegistry.objectPropertyPane = objectPropertyPane;
	}
	
	static void registerClassPane(JScrollPane classPane) {
		UIRegistry.classPane = classPane;
	}
	
	public static Window getDialogOwner() { return frame; }
	
	static JFrame getWindow() { return frame; }
	
	public static Submittable getSelectedSubmittable() {
		return (Submittable)queryPane.getSelectedComponent();
	}
	
	public static Showable getShowable() {
		return (Showable)resultSetPane;
	}
	
	static DPTree getDataPropTree() {
		return (DPTree)dataPropertyPane;
	}
	
	static OPTree findObjPropTree() {
		return (OPTree)objectPropertyPane;
	}
	
	static ClsTree findClsTreePane() {
		return (ClsTree)classPane;
	}
	
	static DBTree getDBTree() {
		return (DBTree)dbSchemePane;
	}
	
	static DescrTree getDescriptionTree() {
		return (DescrTree)descriptionPane;
	}
}