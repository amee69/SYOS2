package com.syos.infrastructure.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHelper {
    private static final String BASE_PATH = "/Users/ameer/SYOS2/reports/";


     // save reports and receipts to the local file system.

    public static void saveToFile(String fileNamePrefix, String content) {
        try {
            // create directory if no exist
            File directory = new File(BASE_PATH);
            if (!directory.exists()) directory.mkdir();

            // generate timestamped filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            String fileName = BASE_PATH + fileNamePrefix + "_" + timestamp + ".txt";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(content);
                System.out.println(":) File Saved: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("! File System Error: Could not save report.");
        }
    }
}