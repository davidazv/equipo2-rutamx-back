package org.acme.domain.models;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResult {

    private int totalRows;
    private int importedRows;
    private int skippedRows;
    private List<String> errors;
    private String tableName;

    public CsvImportResult() {
        this.errors = new ArrayList<>();
    }

    public CsvImportResult(String tableName, int totalRows, int importedRows, int skippedRows, List<String> errors) {
        this.tableName = tableName;
        this.totalRows = totalRows;
        this.importedRows = importedRows;
        this.skippedRows = skippedRows;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getImportedRows() {
        return importedRows;
    }

    public void setImportedRows(int importedRows) {
        this.importedRows = importedRows;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public void setSkippedRows(int skippedRows) {
        this.skippedRows = skippedRows;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
