package com.softwareengineering.finsage.dao;

import com.softwareengineering.finsage.model.Category;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CategoryDao extends BaseDao<Category> {
    private static final String[] HEADERS = {"id", "name", "userId"};
    private static final String CSV_FILE = "data/categories.csv";

    public CategoryDao() {
        super(CSV_FILE, HEADERS);
    }

    @Override
    protected void printRecord(CSVPrinter printer, Category category) throws IOException {
        printer.printRecord(
                category.getId(),
                category.getName(),
                category.getUserId()
        );
    }

    @Override
    protected Category parseRecord(CSVRecord record) {
        Category category = new Category();
        category.setId(record.get("id"));
        category.setName(record.get("name"));
        category.setUserId(record.get("userId"));
        return category;
    }

    @Override
    protected String getId(Category category) {
        return category.getId();
    }

    public List<Category> getByUserId(String userId) {
        return findBy(c -> c.getUserId().equals(userId));
    }

    public Optional<Category> getByNameAndUserId(String name, String userId) {
        return getAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name) && c.getUserId().equals(userId))
                .findFirst();
    }

    public boolean existsByNameAndUserId(String name, String userId) {
        return getByNameAndUserId(name, userId).isPresent();
    }

    public List<Category> searchByName(String name, String userId) {
        return findBy(c -> c.getName().toLowerCase().contains(name.toLowerCase()) && c.getUserId().equals(userId));
    }

    public long countByUserId(String userId) {
        return getByUserId(userId).size();
    }

    public Optional<Category> findById(String id) {
        return getAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

}
