package com.radicasys;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

/**
 * 
 * @author root
 * 
 *         该类主要用来检测文本文件的字符编码，判断的准确率取决于抽样的大小，抽样字符越多越准确
 */
public class FileEncodingDetector {

	private static final int DEFAULT_SAMPLE_SIZE = 1;
	private static final int DEFAULT_BUFFER_SIZE = 1024;

	private static class CharsetObserver implements nsICharsetDetectionObserver {

		private String encoding;
		private boolean found = false;

		public void Notify(String charset) {
			found = true;
			encoding = charset;
		}

		public String getEncoding() {
			return encoding;
		}

		public boolean isFound() {
			return found;
		}

	}

	public static String getFileEncoding(String fileName) throws IOException {
		File file = new File(fileName);
		return getFileEncoding(file);
	}

	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * 
	 *             默认以1m作为抽样
	 */
	public static String getFileEncoding(File file) throws IOException {
		return getFileEncoding(file, DEFAULT_SAMPLE_SIZE);
	}

	/**
	 * 
	 * @param file
	 * @param size
	 *            用以判断字符编码所需的抽样的大小，以M为单位
	 * @return
	 * @throws IOException
	 */
	public static String getFileEncoding(File file, int size) throws IOException {

		// 如果size大于文件实际的大小，则以整个文件作为抽样
		long sizeOfSample = size * 1024 * 1024;
		if (sizeOfSample > FileUtils.sizeOf(file)) {
			sizeOfSample = FileUtils.sizeOf(file);
		}

		boolean found = false;
		String encoding = null;
		boolean done = false;
		boolean isAscii = true;

		CharsetObserver observer = new CharsetObserver();

		nsDetector det = new nsDetector(nsDetector.ALL);

		det.Init(observer);

		BufferedInputStream imp = null;

		try {
			imp = new BufferedInputStream(new FileInputStream(file));

			byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
			int len;
			long total = 0;

			while ((len = imp.read(buf, 0, buf.length)) != -1 && total < sizeOfSample) {
				total += len;
				// Check if the stream is only ascii.
				if (isAscii) {
					isAscii = det.isAscii(buf, len);
				}
				// DoIt if non-ascii and not done yet.
				if (!isAscii && !done) {
					done = det.DoIt(buf, len, false);
				}
			}
			det.DataEnd();
		} finally {
			IOUtils.closeQuietly(imp);
		}
		encoding = observer.getEncoding();
		found = observer.isFound();

		if (isAscii) {
			encoding = "ASCII";
			found = true;
		}

		if (!found) {
			String prob[] = det.getProbableCharsets();
			if (prob.length > 0) {
				encoding = prob[0];
			} else {
				return null;
			}
		}
		
		return encoding;
	}
}
