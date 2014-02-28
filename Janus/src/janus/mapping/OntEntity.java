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
		return OntEntity.getCURIE(uri);
	}
	
	public static String getCURIE(URI uri) {
		String s = uri.toString();
		
		String namespace = s.substring(0, s.indexOf(OntMapper.NUMBER_SIGN));
		
		if (!namespace.equals(Janus.ontologyURI)) {
			String prefix = PrefixMap.getPrefix(URI.create(namespace));
			
			return prefix + OntMapper.COLON + uri.getFragment();
		}
		
		return OntMapper.COLON + uri.getFragment();
	}
	
	public String getToolTipText() { 
		return uri.toString(); 
	}
	
	@Override
	public boolean equals(Object obj) {
		return uri.equals(obj);
	}
	
	public static URI getURI(String aCURIE) {
		String[] tokens = aCURIE.split(OntMapper.COLON);
		
		String prefix = tokens[0];
		String reference = tokens[1];
		
		if (prefix.equals(PrefixMap.getPrefix(Janus.ontBridge.getOntologyID())))
			return URI.create(Janus.ontologyURI + OntMapper.NUMBER_SIGN + aCURIE.substring(1));
		
		return URI.create(PrefixMap.getURI(prefix) + OntMapper.NUMBER_SIGN + reference);
	}
}
