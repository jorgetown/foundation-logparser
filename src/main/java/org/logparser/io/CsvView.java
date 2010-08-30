package org.logparser.io;

import static org.logparser.Constants.FILE_SEPARATOR;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.logparser.ICsvSerializable;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * Serializes data in CSV format.
 * 
 * @author jorge.decastro
 * 
 */
public class CsvView {
	private static final Logger LOGGER = Logger.getLogger(CsvView.class.getName());

	public CsvView() {
	}

	public <T> void write(final String path, final String filename, ICsvSerializable<T> csvSerializable) {
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
			LOGGER.warn(String.format("IO error writing to path '%s'", filepath), ioe);
		} finally {
			Closeables.closeQuietly(out);
		}
	}
}
