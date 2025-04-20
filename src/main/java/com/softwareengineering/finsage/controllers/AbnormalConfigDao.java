package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.AbnormalConfig;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AbnormalConfigDao extends BaseDao<AbnormalConfig> {
    private static final String[] HEADERS = {"id", "itemName", "threshold", "userId"};
    private static final String CSV_FILE = "data/abnormal_configs.csv";

    public AbnormalConfigDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, AbnormalConfig config) throws IOException {
        printer.printRecord(
                config.getId(),
                config.getItemName(),
                config.getThreshold(),
                config.getUserId()
        );
    }

    @Override
    protected AbnormalConfig parseRecord(CSVRecord record) {
        AbnormalConfig config = new AbnormalConfig();
        config.setId(record.get("id"));
        config.setItemName(record.get("itemName"));
        config.setThreshold(new BigDecimal(record.get("threshold")));
        config.setUserId(record.get("userId"));
        return config;
    }

    @Override
    protected String getId(AbnormalConfig config) {
        return config.getId();
    }

    public List<AbnormalConfig> getByUserId(String userId) {
        return findBy(c -> c.getUserId().equals(userId));
    }

    public Optional<AbnormalConfig> getByItemNameAndUserId(String itemName, String userId) {
        return getAll().stream()
                .filter(c -> c.getItemName().equalsIgnoreCase(itemName) && c.getUserId().equals(userId))
                .findFirst();
    }

    public boolean existsByItemNameAndUserId(String itemName, String userId) {
        return getByItemNameAndUserId(itemName, userId).isPresent();
    }

    public List<AbnormalConfig> findByThresholdGreaterThan(BigDecimal threshold, String userId) {
        return findBy(c -> c.getThreshold().compareTo(threshold) > 0 && c.getUserId().equals(userId));
    }

    public List<AbnormalConfig> findByThresholdLessThan(BigDecimal threshold, String userId) {
        return findBy(c -> c.getThreshold().compareTo(threshold) < 0 && c.getUserId().equals(userId));
    }
}
