package org.logparser.io;

import java.io.File;
import java.util.List;

import org.logparser.ILogFilter;

/**
 * Performs some desired pre-processing on a list of log files, before these are
 * consumed by {@link ILogFilter} implementations.
 * 
 * @author jorge.decastro
 * 
 */
public interface IPreProcessor {
	public List<File> apply(List<File> logFiles);
}
