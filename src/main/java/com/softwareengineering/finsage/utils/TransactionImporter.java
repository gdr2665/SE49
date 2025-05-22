package com.softwareengineering.finsage.utils;

import com.softwareengineering.finsage.dao.TransactionDao;
import com.softwareengineering.finsage.model.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionImporter {
    private final TransactionDao transactionDao;
    private final String userId;

    public TransactionImporter(TransactionDao transactionDao, String userId) {
        this.transactionDao = transactionDao;
        this.userId = userId;
    }

    public List<Transaction> importFromCsv(Path filePath) throws IOException {
        List<Transaction> importedTransactions = new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(filePath);
             CSVParser csvParser = new CSVParser(reader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    Transaction transaction = parseTransactionRecord(record);
                    importedTransactions.add(transaction);
                } catch (Exception e) {
                    // Log error and continue with next record
                    System.err.println("Error parsing record: " + record + ", Error: " + e.getMessage());
                }
            }

            // Save all valid transactions
            if (!importedTransactions.isEmpty()) {
                transactionDao.saveAll(importedTransactions);
            }
        }

        return importedTransactions;
    }

    private Transaction parseTransactionRecord(CSVRecord record) {
        Transaction transaction = new Transaction();

        // Generate new ID for imported transaction
        transaction.setId(UUID.randomUUID().toString());

        // Parse amount (required)
        String amountStr = record.get("amount");
        transaction.setAmount(new BigDecimal(amountStr));

        // Parse date (required)
        String dateStr = record.get("date");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        transaction.setDate(LocalDate.parse(dateStr, dateFormatter));

        // Parse category (required)
        String categoryId = record.get("categoryId");
        transaction.setCategoryId(categoryId);

        // Parse note (optional)
        String note = record.isMapped("note") ? record.get("note") : "";
        transaction.setNote(note);

        // Set user ID
        transaction.setUserId(userId);

        return transaction;
    }
}
