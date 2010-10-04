package org.logparser.io;

import static org.logparser.Constants.FILE_SEPARATOR;
import static org.logparser.Constants.LINE_SEPARATOR;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.logparser.ICsvSerializable;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;

/**
 * Serializes data in CSV format. Data must be {@link ICsvSerializable}.
 * 
 * @author jorge.decastro
 * 
 */
public class CsvView {
	private static final Logger LOGGER = Logger.getLogger(CsvView.class.getName());
	private static final String DEFAULT_CSV_FILENAME = "log-analysis.csv";
	private final String path;
	private final String filename;
	private final List<ICsvSerializable<?>> submitted;

	public CsvView(final String path) {
		this(path, DEFAULT_CSV_FILENAME);
	}

	private CsvView(final String path, final String filename) {
		this.path = Preconditions.checkNotNull(path, "'path' argument cannot be null.");
		this.filename = Preconditions.checkNotNull(filename, "'filename' argument cannot be null.");
		this.submitted = new ArrayList<ICsvSerializable<?>>();
	}

	public void submit(final ICsvSerializable<?> toSerialize) {
		if (toSerialize != null) {
			submitted.add(toSerialize);
		}
	}

	public void write() {
		write(path, filename, submitted);
	}

	public void write(final ICsvSerializable<?>... csvSerializables) {
		Preconditions.checkNotNull(csvSerializables, "'csvSerializables' cannot be null.");

		write(path, filename, Arrays.asList(csvSerializables));
	}

	private void write(final String path, final String filename, final List<ICsvSerializable<?>> csvSerializables) {
		String filepath = String.format("%s%s%s", path, FILE_SEPARATOR, filename);

		LOGGER.info(String.format("Writing CSV file '%s'", filepath));

		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(filepath));
			for (ICsvSerializable<?> csvSerializable : csvSerializables) {
				out.write(csvSerializable.toCsvString());
				out.write(LINE_SEPARATOR);
			}
			out.close();
		} catch (IOException ioe) {
			LOGGER.warn(String.format("IO error writing to path '%s'", filepath), ioe);
		} finally {
			Closeables.closeQuietly(out);
		}
	}
}
