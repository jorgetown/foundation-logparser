package org.logparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.Immutable;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Preconditions;

/**
 * Represents a log file snapshot, containing filtered log {@code E}ntries and log summaries.
 * 
 * @author jorge.decastro
 * 
 * @param <E> the type of log entries held.
 */
@Immutable
@JsonPropertyOrder({ "totalEntries", "summary", "timeBreakdown" })
public class LogSnapshot<E> {
	private final List<E> filteredEntries;
	private final int totalEntries;
	private final Map<String, Integer> summary;
	private final Map<Integer, Integer> timeBreakdown;
	private ObjectMapper jsonMapper;

	public LogSnapshot(final List<E> filteredEntries, final int totalEntries, final Map<String, Integer> summary, final Map<Integer, Integer> timeBreakdown) {
		Preconditions.checkNotNull(filteredEntries);
		Preconditions.checkNotNull(summary);
		Preconditions.checkNotNull(timeBreakdown);
		this.filteredEntries = new ArrayList<E>(filteredEntries);
		this.totalEntries = totalEntries;
		this.summary = summary;
		this.timeBreakdown = timeBreakdown;
		this.jsonMapper = new ObjectMapper();
	}

	public List<E> getFilteredEntries() {
		return Collections.unmodifiableList(filteredEntries);
	}

	public int getTotalEntries() {
		return totalEntries;
	}

	public Map<String, Integer> getSummary() {
		return Collections.unmodifiableMap(summary);
	}

	public Map<Integer, Integer> getTimeBreakdown() {
		return Collections.unmodifiableMap(timeBreakdown);
	}
	
	public String toJsonString() throws JsonGenerationException, JsonMappingException, IOException {
		return jsonMapper.writeValueAsString(this);
	}
}
