package org.logparser;

import net.jcip.annotations.Immutable;

/**
 * Application constants.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class Constants {
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String CSV_VALUE_SEPARATOR = ",";
	public static final String DEFAULT_OUTPUT_DIR = ".";

	private Constants() {

	}
}
