package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.ThirdParty;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ThirdPartyDao extends BaseDao<ThirdParty> {
    private static final String[] HEADERS = {"id", "serviceName", "serviceUsername", "servicePassword", "userId"};
    private static final String CSV_FILE = "data/third_parties.csv";

    public ThirdPartyDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, ThirdParty thirdParty) throws IOException {
        printer.printRecord(
                thirdParty.getId(),
                thirdParty.getServiceName(),
                thirdParty.getServiceUsername(),
                thirdParty.getServicePassword(),
                thirdParty.getUserId()
        );
    }

    @Override
    protected ThirdParty parseRecord(CSVRecord record) {
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setId(record.get("id"));
        thirdParty.setServiceName(record.get("serviceName"));
        thirdParty.setServiceUsername(record.get("serviceUsername"));
        thirdParty.setServicePassword(record.get("servicePassword"));
        thirdParty.setUserId(record.get("userId"));
        return thirdParty;
    }

    @Override
    protected String getId(ThirdParty thirdParty) {
        return thirdParty.getId();
    }

    public List<ThirdParty> getByUserId(String userId) {
        return findBy(t -> t.getUserId().equals(userId));
    }

    public Optional<ThirdParty> getByServiceNameAndUserId(String serviceName, String userId) {
        return getAll().stream()
                .filter(t -> t.getServiceName().equalsIgnoreCase(serviceName) && t.getUserId().equals(userId))
                .findFirst();
    }

    public boolean existsByServiceNameAndUserId(String serviceName, String userId) {
        return getByServiceNameAndUserId(serviceName, userId).isPresent();
    }

    public List<ThirdParty> searchByServiceName(String serviceName, String userId) {
        return findBy(t -> t.getServiceName().toLowerCase().contains(serviceName.toLowerCase()) && t.getUserId().equals(userId));
    }

    public Optional<ThirdParty> getByServiceUsernameAndUserId(String serviceUsername, String userId) {
        return getAll().stream()
                .filter(t -> t.getServiceUsername().equals(serviceUsername) && t.getUserId().equals(userId))
                .findFirst();
    }
}
