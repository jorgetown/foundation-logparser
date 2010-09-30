package org.logparser.io;

import java.io.File;
import java.util.List;

/**
 * {@link IPreProcessor} implementation that always returns the same value that
 * was used as its argument.
 * 
 * @author jorge.decastro
 * 
 */
public final class IdentityPreProcessor implements IPreProcessor {

	public List<File> apply(final List<File> logFiles) {
		return logFiles;
	}

}
