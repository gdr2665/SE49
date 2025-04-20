package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.Holiday;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HolidayDao extends BaseDao<Holiday> {
    private static final String[] HEADERS = {"id", "name", "startDate", "endDate", "userId"};
    private static final String CSV_FILE = "data/holidays.csv";

    public HolidayDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, Holiday holiday) throws IOException {
        printer.printRecord(
                holiday.getId(),
                holiday.getName(),
                holiday.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                holiday.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                holiday.getUserId()
        );
    }

    @Override
    protected Holiday parseRecord(CSVRecord record) {
        Holiday holiday = new Holiday();
        holiday.setId(record.get("id"));
        holiday.setName(record.get("name"));
        holiday.setStartDate(LocalDate.parse(record.get("startDate"), DateTimeFormatter.ISO_LOCAL_DATE));
        holiday.setEndDate(LocalDate.parse(record.get("endDate"), DateTimeFormatter.ISO_LOCAL_DATE));
        holiday.setUserId(record.get("userId"));
        return holiday;
    }

    @Override
    protected String getId(Holiday holiday) {
        return holiday.getId();
    }

    public List<Holiday> getByUserId(String userId) {
        return findBy(h -> h.getUserId().equals(userId));
    }

    public Holiday getByDate(LocalDate date, String userId) {
        return getByUserId(userId).stream()
                .filter(h -> !date.isBefore(h.getStartDate()) && !date.isAfter(h.getEndDate()))
                .findFirst()
                .orElse(null);
    }

    public List<Holiday> getByDateRange(LocalDate start, LocalDate end, String userId) {
        return getByUserId(userId).stream()
                .filter(h -> h.getStartDate().isBefore(end) && h.getEndDate().isAfter(start))
                .toList();
    }

    public Optional<Holiday> getByNameAndUserId(String name, String userId) {
        return getAll().stream()
                .filter(h -> h.getName().equalsIgnoreCase(name) && h.getUserId().equals(userId))
                .findFirst();
    }

    public boolean existsByNameAndUserId(String name, String userId) {
        return getByNameAndUserId(name, userId).isPresent();
    }

    public List<Holiday> searchByName(String name, String userId) {
        return findBy(h -> h.getName().toLowerCase().contains(name.toLowerCase()) && h.getUserId().equals(userId));
    }
}
