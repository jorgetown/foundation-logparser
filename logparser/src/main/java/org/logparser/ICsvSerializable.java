package org.logparser;

/**
 * Specifies the protocol required of CSV serializers.
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc4180.txt">Common Format and MIME Type for CSV Files</a>
 * @author jorge.decastro
 */
public interface ICsvSerializable {
	public static final String NEWLINE = System.getProperty("line.separator");

	public String toCsvString();
}
