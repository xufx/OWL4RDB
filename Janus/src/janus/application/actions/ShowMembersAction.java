package janus.application.actions;

import janus.Janus;
import janus.application.UIRegistry;
import janus.application.ontscheme.ClsTree;
import janus.mapping.metadata.ClassTypes;
import janus.query.SQLGenerator;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ShowMembersAction extends AbstractAction {
	
	public ShowMembersAction(String name) {
		super(name);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClsTree tree = UIRegistry.getClsTreePane();
		
		String query = getQuery(tree.getSelectedClass());System.out.println(query);
	}
	
	private String getQuery(URI cls) {
		String query = null;
		
		if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.TABLE_CLASS))
			query = getQueryToGetIndividualsOfTableClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.COLUMN_CLASS))
			query = getQueryToGetIndividualsOfColumnClass(cls);
		else if (Janus.mappingMetadata.getClassType(cls).equals(ClassTypes.OWL_THING)) {
			
			List<String> queries = new Vector<String>();
			
			Set<URI> clses = Janus.ontBridge.getSubClses(cls);
			for (URI aCls: clses) {
				if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.TABLE_CLASS))
					queries.add(getQueryToGetIndividualsOfTableClass(aCls));
				else if (Janus.mappingMetadata.getClassType(aCls).equals(ClassTypes.COLUMN_CLASS))
					queries.add(getQueryToGetIndividualsOfColumnClass(aCls));
			}
			
			query = Janus.sqlGenerator.getUnionQuery(queries);
		}
		
		return query;
	}
	
	private String getQueryToGetIndividualsOfTableClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameOfClass(cls);
		List<String> pk = Janus.cachedDBMetadata.getPrimaryKeys(table);
		
		return Janus.sqlGenerator.getQueryToGetIndividualsOfTableClass(pk, table);
	}
	
	private String getQueryToGetIndividualsOfColumnClass(URI cls) {
		String table = Janus.mappingMetadata.getMappedTableNameOfClass(cls);
		String column = Janus.mappingMetadata.getMappedColumnNameOfClass(cls);
		
		if (Janus.cachedDBMetadata.isPrimaryKeySingleColumn(table))
			return Janus.sqlGenerator.getQueryI_I(column, table);
		else if (Janus.cachedDBMetadata.isNotNull(table, column))
			return Janus.sqlGenerator.getQueryI_III(column, table);
		else
			return Janus.sqlGenerator.getQueryI_IV(column, table);
	}
}
