package org.logparser.config;

import java.lang.reflect.Constructor;

import net.jcip.annotations.Immutable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.logparser.io.IPreProcessor;
import org.logparser.io.LogFiles;

import com.google.common.base.Strings;

/**
 * Responsible for providing bespoke instances of {@link LogFiles}.
 * 
 * @author jorge.decastro
 * 
 */
@Immutable
public final class LogFilesProvider {
	private static final Logger LOGGER = Logger.getLogger(LogFilesProvider.class);
	private final String filenamePattern;
	private final String[] inputDirs;
	private final String outputDir;
	private final PreProcessorProvider preProcessorProvider;

	@JsonCreator
	public LogFilesProvider(
			@JsonProperty("filenamePattern") final String filenamePattern,
			@JsonProperty("inputDirs") final String[] inputDirs,
			@JsonProperty("outputDir") final String outputDir,
			@JsonProperty("preprocessor") final PreProcessorProvider preProcessorProvider) {

		this.filenamePattern = filenamePattern;
		this.inputDirs = inputDirs;
		this.outputDir = outputDir;
		this.preProcessorProvider = preProcessorProvider;
	}

	public String getFilenamePattern() {
		return filenamePattern;
	}

	public String[] getInputDirs() {
		return inputDirs;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public PreProcessorProvider getPreProcessorProvider() {
		return preProcessorProvider;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public static class PreProcessorProvider {
		private final String type;
		private final String filenamePattern;
		private final String outputDir;

		@JsonCreator
		public PreProcessorProvider(
				@JsonProperty("type") final String type, 
				@JsonProperty("filenamePattern") final String filenamePattern,
				@JsonProperty("outputDir") final String outputDir) {

			this.type = type;
			this.filenamePattern = filenamePattern;
			this.outputDir = outputDir;
		}

		public String getType() {
			return type;
		}

		public String getFilenamePattern() {
			return filenamePattern;
		}

		public String getOutputDir() {
			return outputDir;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this);
		}
	}

	public LogFiles build() {
		LogFiles.Builder logFilesBuilder = new LogFiles.Builder();
		if (!Strings.isNullOrEmpty(filenamePattern)) {
			logFilesBuilder.filenamePattern(filenamePattern);
		}
		if (inputDirs != null) {
			logFilesBuilder.inputDirs(inputDirs);
		}
		if (!Strings.isNullOrEmpty(outputDir)) {
			logFilesBuilder.outputDir(outputDir);
		}
		if (preProcessorProvider != null) {
			String type = preProcessorProvider.type;
			if (Strings.isNullOrEmpty(type)) {
				throw new IllegalArgumentException("'type' argument of pre-processor is required.");
			}
			String filenamePattern = preProcessorProvider.filenamePattern;
			if (Strings.isNullOrEmpty(filenamePattern)) {
				throw new IllegalArgumentException("'filenamePattern' argument of pre-processor is required.");
			}
			String outputDir = preProcessorProvider.outputDir;
			if (Strings.isNullOrEmpty(outputDir)) {
				throw new IllegalArgumentException("'outputDir' argument of pre-processor is required.");
			}

			try {
				@SuppressWarnings("unchecked")
				Class<IPreProcessor> clazz = (Class<IPreProcessor>) Class.forName(type);
				@SuppressWarnings("rawtypes")
				Class[] argsClass = new Class[] { String.class, String.class };
				Object[] argsObjs = new Object[] { filenamePattern, outputDir };
				if (clazz != null) {
					Constructor<IPreProcessor> constructor;
					constructor = clazz.getConstructor(argsClass);
					IPreProcessor preprocessor = constructor.newInstance(argsObjs);
					logFilesBuilder.preProcessor(preprocessor);
					LOGGER.info(String.format("Successfully constructed pre-processor '%s'", type));
				}
			} catch (Throwable t) {
				throw new IllegalArgumentException(String.format("Unable to instantiate pre-processor of type '%s'", type), t);
			}
		}
		return logFilesBuilder.build();
	}
}
