package com.radicasys;


import java.io.IOException;
import java.nio.charset.Charset;

public class RICsvReader extends CsvReader implements RIReader {

	private boolean hasHeaders = false;

	public RICsvReader(String fileName) throws IOException {
		super(fileName, Letters.COMMA);

		String encoding = FileEncodingDetector.getFileEncoding(fileName);
		Charset charset = Charset.forName(encoding);

		setCharset(charset);
		initHeaders();
	}

	@Override
	public boolean readHeaders() {
		throw new RuntimeException("Not supported. Use getHeaders() to retrive csv file header.");
	}

	private void initHeaders() throws IOException {
		boolean result = readRecord();

		hasHeaders = true;

		headersHolder.Length = getColumnCount();

		headersHolder.Headers = new String[getColumnCount()];

		if (hasHeaders) {
			for (int i = 0; i < headersHolder.Length; i++) {
				String columnValue = get(i);

				headersHolder.Headers[i] = columnValue;

				// if there are duplicate header names, we will save the last
				// one
				headersHolder.IndexByName.put(columnValue, new Integer(i));
			}
		} else {
			if (result) {
				rollbackCurrentRecord();
			}
			resetDataBuffer();
		}

		resetColumnCount();
	}

	public boolean isHasHeaders() {
		return hasHeaders;
	}

	@Override
	public boolean hasNext() throws IOException {
		return readRecord();
	}

	@Override
	public Object getColumnValue(int columnIndex) throws IOException {
		return get(columnIndex);
	}

	@Override
	public String[] getColumnValues() throws IOException {
		return getValues();
	}

	@Override
	public String getStringColumnValue(int columnIndex) throws IOException {
		return get(columnIndex);
	}
}
