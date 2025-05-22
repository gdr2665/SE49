package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.Transaction;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TransactionDao extends BaseDao<Transaction> {
    private static final String[] HEADERS = {"id", "amount", "date", "categoryId", "note", "userId"};
    private static final String CSV_FILE = "data/transactions.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public TransactionDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, Transaction transaction) throws IOException {
        printer.printRecord(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate().format(DATE_FORMATTER),
                transaction.getCategoryId(),
                transaction.getNote(),
                transaction.getUserId()
        );
    }

    @Override
    protected Transaction parseRecord(CSVRecord record) {
        Transaction transaction = new Transaction();
        transaction.setId(record.get("id"));
        transaction.setAmount(new BigDecimal(record.get("amount")));
        transaction.setDate(LocalDate.parse(record.get("date"), DATE_FORMATTER));
        transaction.setCategoryId(record.get("categoryId"));
        transaction.setNote(record.get("note"));
        transaction.setUserId(record.get("userId"));
        return transaction;
    }

    @Override
    protected String getId(Transaction transaction) {
        return transaction.getId();
    }

    // 基本查询方法
    public List<Transaction> getByUserId(String userId) {
        return findBy(t -> t.getUserId().equals(userId));
    }

    public List<Transaction> getByUserIdAndDateRange(String userId, LocalDate start, LocalDate end) {
        return getByUserId(userId).stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Transaction> getByCategoryId(String categoryId, String userId) {
        return findBy(t -> t.getCategoryId().equals(categoryId) && t.getUserId().equals(userId));
    }

    // 金额相关查询方法
    public List<Transaction> getByAmountGreaterThan(BigDecimal amount, String userId) {
        return findBy(t -> t.getAmount().compareTo(amount) > 0 && t.getUserId().equals(userId));
    }

    public List<Transaction> getByAmountLessThan(BigDecimal amount, String userId) {
        return findBy(t -> t.getAmount().compareTo(amount) < 0 && t.getUserId().equals(userId));
    }

    public List<Transaction> getByAmountBetween(BigDecimal min, BigDecimal max, String userId) {
        return findBy(t -> t.getAmount().compareTo(min) >= 0 &&
                t.getAmount().compareTo(max) <= 0 &&
                t.getUserId().equals(userId));
    }

    // 文本搜索方法
    public List<Transaction> searchByNote(String keyword, String userId) {
        return findBy(t -> t.getNote().toLowerCase().contains(keyword.toLowerCase()) &&
                t.getUserId().equals(userId));
    }

    // 统计方法
    public BigDecimal getTotalAmountByUserId(String userId) {
        return getByUserId(userId).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmountByCategoryId(String categoryId, String userId) {
        return getByCategoryId(categoryId, userId).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmountByDateRange(LocalDate start, LocalDate end, String userId) {
        return getByUserIdAndDateRange(userId, start, end).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long countByUserId(String userId) {
        return getByUserId(userId).size();
    }

    public long countByCategoryId(String categoryId, String userId) {
        return getByCategoryId(categoryId, userId).size();
    }

    // 预算功能专用方法
    public List<Transaction> getByMonthAndUserId(YearMonth month, String userId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return getByUserIdAndDateRange(userId, start, end);
    }

    public List<Transaction> getByMonthCategoryAndUserId(YearMonth month, String categoryId, String userId) {
        return getByMonthAndUserId(month, userId).stream()
                .filter(t -> t.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalAmountByCategoryIdAndDateRange(String categoryId, LocalDate start, LocalDate end, String userId) {
        return getByUserId(userId).stream()
                .filter(t -> t.getCategoryId().equals(categoryId))
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetExpenseByDateRange(LocalDate start, LocalDate end, String userId) {
        return getByUserId(userId).stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .map(t -> {
                    if (t.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                        return t.getAmount().abs();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
