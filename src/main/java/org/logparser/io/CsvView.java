package org.logparser.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.logparser.ICsvSerializable;

import com.google.common.base.Preconditions;

/**
 * Serializes data in CSV format.
 * 
 * @author jorge.decastro
 * 
 */
public class CsvView {
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	public CsvView() {
	}

	public void write(final String path, final String filename, ICsvSerializable csvSerializable) {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(filename);
		Preconditions.checkNotNull(csvSerializable);

		write(path, filename, csvSerializable.toCsvString());
	}

	private void write(final String path, final String filename, final String csvSerializable) {
		String filepath = String.format("%s%s%s.csv", path, FILE_SEPARATOR, filename);

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filepath));
			out.write(csvSerializable);
			out.close();
		} catch (IOException ioe) {
			throw new RuntimeException(String.format("Failed to write path %s", filepath), ioe);
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ioe) {
				throw new RuntimeException(String.format("Failed to properly close handler for %s", filepath), ioe);
			}
		}
	}
}
