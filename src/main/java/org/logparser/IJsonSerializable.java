package org.logparser;

/**
 * Specifies the protocol required to serialize and deserialize JSON content.
 * 
 * @see <a href="http://tools.ietf.org/html/rfc4627">The application/json Media Type for JavaScript Object Notation (JSON)</a>
 * @author jorge.decastro
 */
public interface IJsonSerializable<T> {
	public String toJsonString();

	public T fromJsonString(String jsonString);
}
