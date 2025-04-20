package com.softwareengineering.finsage.dao;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class BaseDao<T> {
    protected String csvFilePath;
    protected String[] headers;

    public BaseDao(String csvFilePath, String[] headers) {
        this.csvFilePath = csvFilePath;
        this.headers = headers;
        createFileIfNotExists();
    }

    protected void createFileIfNotExists() {
        try {
            File file = new File(csvFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvFilePath),
                        CSVFormat.DEFAULT.withHeader(headers))) {
                    // Just create the file with headers
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<CSVRecord> readAllRecords() throws IOException {
        try (Reader reader = Files.newBufferedReader(Paths.get(csvFilePath));
             CSVParser csvParser = new CSVParser(reader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            return csvParser.getRecords();
        }
    }

    protected void writeAllRecords(List<T> items) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(csvFilePath),
                CSVFormat.DEFAULT.withHeader(headers))) {
            for (T item : items) {
                printRecord(printer, item);
            }
        }
    }

    protected abstract void printRecord(CSVPrinter printer, T item) throws IOException;
    protected abstract T parseRecord(CSVRecord record);
    protected abstract String getId(T item);

    public List<T> getAll() {
        List<T> items = new ArrayList<>();
        try {
            List<CSVRecord> records = readAllRecords();
            for (CSVRecord record : records) {
                items.add(parseRecord(record));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public T getById(String id) {
        try {
            List<CSVRecord> records = readAllRecords();
            for (CSVRecord record : records) {
                if (getId(parseRecord(record)).equals(id)) {
                    return parseRecord(record);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean exists(String id) {
        return getById(id) != null;
    }

    public boolean save(T item) {
        List<T> items = getAll();
        items.add(item);
        try {
            writeAllRecords(items);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveAll(List<T> items) {
        List<T> allItems = getAll();
        allItems.addAll(items);
        try {
            writeAllRecords(allItems);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(T item) {
        List<T> items = getAll();
        for (int i = 0; i < items.size(); i++) {
            if (getId(items.get(i)).equals(getId(item))) {
                items.set(i, item);
                try {
                    writeAllRecords(items);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public boolean delete(String id) {
        List<T> items = getAll();
        boolean removed = items.removeIf(item -> getId(item).equals(id));
        if (removed) {
            try {
                // Create a backup file in case the write fails
                File originalFile = new File(csvFilePath);
                File backupFile = new File(csvFilePath + ".bak");
                if (originalFile.exists()) {
                    Files.copy(originalFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                // Write the new data
                writeAllRecords(items);

                // Delete the backup if successful
                backupFile.delete();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                // Restore from backup if possible
                try {
                    File originalFile = new File(csvFilePath);
                    File backupFile = new File(csvFilePath + ".bak");
                    if (backupFile.exists()) {
                        Files.copy(backupFile.toPath(), originalFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        backupFile.delete();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    public boolean deleteAll(List<String> ids) {
        List<T> items = getAll();
        boolean removed = items.removeIf(item -> ids.contains(getId(item)));
        if (removed) {
            try {
                writeAllRecords(items);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<T> findBy(Predicate<T> predicate) {
        return getAll().stream()
                .filter(predicate)
                .collect(java.util.stream.Collectors.toList());
    }

    public long count() {
        return getAll().size();
    }
}
