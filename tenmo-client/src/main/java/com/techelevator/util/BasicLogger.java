package com.techelevator.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BasicLogger {

	private static PrintWriter pw = null;
	
	public static void log(String message) {
		try {
			if (pw == null) {
				String logFilename = "log.txt" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".log";
				pw = new PrintWriter(new FileOutputStream(logFilename, true));
			}
			pw.println(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + " " + message);
			pw.flush();
		}
		catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			throw new BasicLoggerException(e.getMessage());
		}
	}

}
