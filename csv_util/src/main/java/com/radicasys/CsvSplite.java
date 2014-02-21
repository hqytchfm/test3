package com.radicasys;


import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CsvSplite {
	public static void main(String[] args) throws Exception {
		String filePath = inputFilePath();
		String[] domains = inputDomains();
		Map<String, CsvWriter> map = new HashMap<String, CsvWriter>();
		RICsvReader csvReader = new RICsvReader(filePath);
		String[] headers = csvReader.getHeaders();
		try {
			for (String domain : domains) {
				CsvWriter csvWriter = new CsvWriter(domain + ".csv", Charset.defaultCharset());
				if (csvReader.isHasHeaders()) {
					csvWriter.writeRecord(headers);
				}
				map.put(domain, csvWriter);
			}
			CsvWriter writer = new CsvWriter("others.csv", Charset.defaultCharset());
			writer.writeRecord(headers);
			map.put("others", writer);
			int index = inputEmailColumnIndex();
			while (csvReader.readRecord()) {
				String email = csvReader.get(index - 1);
				if (email != null && email.length() > 0) {
					boolean inDomains = false;
					for (String domain : domains) {
						email = email.toLowerCase();
						if (email.endsWith(domain.toLowerCase())) {
							CsvWriter csvWriter = map.get(domain);
							csvWriter.writeRecord(csvReader.getValues());
							inDomains = true;
							break;
						}
					}
					if (!inDomains) {
						CsvWriter csvWriter = map.get("others");
						csvWriter.writeRecord(csvReader.getValues());
					}
				}
			}
		} finally {
			csvReader.close();
			Collection<CsvWriter> values = map.values();
			for (CsvWriter csvWriter : values) {
				csvWriter.close();
			}
		}
	}

	private static String inputFilePath() {
		System.out.println("请输入csv文件路径： ");
		Scanner scanner = new Scanner(System.in);
		if (scanner.hasNext()) {
			String filePath = scanner.next();
			if (!filePath.endsWith(".csv")) {
				System.err.println("文件名必须以.csv结尾！");
				filePath = inputFilePath();
			} else if (!new File(filePath).exists()) {
				System.err.println("文件不存在！");
				filePath = inputFilePath();
			}
			return filePath;
		}
		return null;
	}

	private static String[] inputDomains() {
		System.out.println("请输入要分割的domain，多个以逗号分割： ");
		Scanner scanner = new Scanner(System.in);
		if (scanner.hasNext()) {
			String domains = scanner.next();
			return domains.split(",");
		}
		return null;
	}

	private static int inputEmailColumnIndex() {
		System.out.println("请输入email在第几列：");
		Scanner scanner = new Scanner(System.in);
		if (scanner.hasNext()) {
			String indexStr = scanner.next();
			int index = 0;
			try {
				index = Integer.parseInt(indexStr);
			} catch (NumberFormatException e) {
				System.err.println("请输入正确的数字！");
				inputEmailColumnIndex();

			}
			if (index < 1) {
				System.err.println("请输入大于0的数字！");
				inputEmailColumnIndex();
			}
			return index;
		}
		return 0;
	}

}
