package org.logparser;

/**
 * Specifies the protocol required to serialize and deserialize CSV formatted text.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc4180.txt">Common Format and MIME Type for CSV Files</a>
 * @author jorge.decastro
 */
public interface ICsvSerializable<T> {
	public String toCsvString();

	public T fromCsvString(String csvString);
}
