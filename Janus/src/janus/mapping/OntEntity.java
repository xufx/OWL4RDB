package janus.mapping;

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
}
