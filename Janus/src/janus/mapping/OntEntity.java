package janus.mapping;

import janus.Janus;

import java.net.URI;

public class OntEntity {
	private URI uri;
	private OntEntityTypes type;
	
	public OntEntity(URI uri, OntEntityTypes type) {
		this.uri = uri;
		this.type = type;
	}
	
	public URI getURI() { return uri; }
	
	public OntEntityTypes getType() { return type; }
	
	public String toString() {
		return getAbbreviatedIRI();
	}
	
	private String getAbbreviatedIRI() {
		String s = uri.toString();
		
		String namespaceIRI = s.substring(0, s.indexOf("#"));
		
		if (!namespaceIRI.equals(Janus.ontologyIRI)) {
			String prefix = PrefixMap.getPrefixName(URI.create(namespaceIRI));
			
			return prefix + ":" + uri.getFragment();
		}
		
		return uri.getFragment();
	}
	
	public String getToolTipText() { 
		return uri.toString(); 
	}
}
