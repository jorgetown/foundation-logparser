package org.logparser;

/**
 * Specifies the protocol required of JSON serializers.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc4627">The application/json Media Type for JavaScript Object Notation (JSON)</a>
 * @author jorge.decastro
 */
public interface IJsonSerializable {
	public String toJsonString();
}
