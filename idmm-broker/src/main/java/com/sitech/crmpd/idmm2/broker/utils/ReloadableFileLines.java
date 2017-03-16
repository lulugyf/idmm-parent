package com.sitech.crmpd.idmm2.broker.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年9月1日 上午9:50:54
 */
public class ReloadableFileLines {

	/** name="{@link com.sitech.crmpd.idmm2.broker.utils.ReloadableFileLines}" */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReloadableFileLines.class);
	private final String filename;
	private FileStatHolder holder;
	private String defaultEncoding;
	private long cacheMillis = -1;
	private boolean concurrentRefresh = true;

	/**
	 * @param filename
	 * @throws IOException
	 */
	public ReloadableFileLines(String filename) throws IOException {
		this.filename = filename;
		holder = new FileStatHolder(filename);
	}

	/**
	 * @return {@link FileStatHolder#lines()}
	 */
	public List<String> getLines() {
		return getHolder(filename).lines();
	}

	/**
	 * @return {@link FileStatHolder#lineSet()}
	 */
	public Set<String> getLineSet() {
		return getHolder(filename).lineSet();
	}

	protected FileStatHolder getHolder(String filename) {
		long originalTimestamp = -2;

		originalTimestamp = holder.getRefreshTimestamp();
		if (originalTimestamp == -1 || originalTimestamp > System.currentTimeMillis() - cacheMillis) {
			// Up to date
			return holder;
		}

		// At this point, we need to refresh...
		if (concurrentRefresh && holder.getRefreshTimestamp() >= 0) {
			// A populated but stale holder -> could keep using it.
			if (!holder.refreshLock.tryLock()) {
				// Getting refreshed by another thread already ->
				// let's return the existing properties for the time being.
				return holder;
			}
		} else {
			holder.refreshLock.lock();
		}
		try {
			return refreshProperties(holder);
		} finally {
			holder.refreshLock.unlock();
		}
	}

	protected FileStatHolder refreshProperties(FileStatHolder holder) {
		final long refreshTimestamp = cacheMillis < 0 ? -1 : System.currentTimeMillis();

		final File file = holder.getFile();
		if (file.isFile()) {
			long fileTimestamp = -1;
			if (cacheMillis >= 0) {
				fileTimestamp = file.lastModified();
				if (holder != null && holder.fileTimestamp() == fileTimestamp) {
					LOGGER.debug(
							"Re-caching the lines for filename [{}][{}] - file hasn't been modified",
							filename, file);
					holder.setRefreshTimestamp(refreshTimestamp);
					return holder;
				}
			}
			return new FileStatHolder(filename);
		}
		throw new IllegalStateException(new FileNotFoundException(filename));
	}

	protected class FileStatHolder {

		private final File file;

		private final List<String> lines;

		private final Set<String> lineSet;

		private final long fileTimestamp;

		private volatile long refreshTimestamp = -2;

		private final ReentrantLock refreshLock = new ReentrantLock();

		public FileStatHolder() {
			this(null);
		}

		public FileStatHolder(String filename) {
			this(filename, -1);
		}

		public FileStatHolder(String filename, long fileTimestamp) {
			try {
				file = ResourceUtils.getFile(filename);
				lines = Files.readLines(
						file,
						Strings.isNullOrEmpty(defaultEncoding) ? Charset.defaultCharset() : Charset
								.forName(defaultEncoding), new LineProcessor<List<String>>() {
							final List<String> result = Lists.newArrayList();

							@Override
							public boolean processLine(String line) {
								if (StringUtils.hasText(line)) {
									result.add(line);
								}
								return true;
							}

							@Override
							public List<String> getResult() {
								return result;
							}
						});
			} catch (final IOException e) {
				throw new IllegalStateException(e);
			}
			lineSet = Collections.unmodifiableSet(Sets.newHashSet(lines));
			this.fileTimestamp = fileTimestamp;
		}

		public File getFile() {
			return file;
		}

		public List<String> lines() {
			return lines;
		}

		public Set<String> lineSet() {
			return lineSet;
		}

		public long fileTimestamp() {
			return fileTimestamp;
		}

		public void setRefreshTimestamp(long refreshTimestamp) {
			this.refreshTimestamp = refreshTimestamp;
		}

		public long getRefreshTimestamp() {
			return refreshTimestamp;
		}

	}
}
