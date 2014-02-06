package janus.application.description;

import java.net.URI;

public interface AttrDescribable {
	public void describeDBColumn(String catalog, String table, String column);
	
	public void describeOWLCls(URI cls);
	public void describeOWLObjProp(URI objProperty);
	public void describeOWLDataProp(URI dataProperty);
}
