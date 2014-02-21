package com.radicasys;


import java.io.IOException;

public interface RIReader {
	int getHeaderCount();

	boolean hasNext() throws IOException;

	Object getColumnValue(int columnIndex) throws IOException;

	String getStringColumnValue(int columnIndex) throws IOException;

	String[] getColumnValues() throws IOException;

	boolean isHasHeaders();

	String[] getHeaders() throws IOException;
	
	void close();
}
