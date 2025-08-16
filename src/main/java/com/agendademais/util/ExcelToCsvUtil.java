package com.agendademais.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para conversão de arquivos Excel para CSV
 */
public class ExcelToCsvUtil {
    
    private static final String SEPARATOR_SEMICOLON = ";";
    private static final String SEPARATOR_COMMA = ",";
    
    /**
     * Converte arquivo Excel para CSV usando ponto e vírgula como separador
     */
    public static File convertExcelToCsv(MultipartFile excelFile) throws IOException {
        return convertExcelToCsv(excelFile, SEPARATOR_SEMICOLON);
    }
    
    /**
     * Converte arquivo Excel para CSV com separador personalizado
     */
    public static File convertExcelToCsv(MultipartFile excelFile, String separator) throws IOException {
        if (excelFile == null || excelFile.isEmpty()) {
            throw new IllegalArgumentException("Arquivo Excel não pode ser vazio");
        }
        
        String originalFileName = excelFile.getOriginalFilename();
        if (originalFileName == null || 
            (!originalFileName.toLowerCase().endsWith(".xlsx") && 
             !originalFileName.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Arquivo deve ser do tipo Excel (.xlsx ou .xls)");
        }
        
        Workbook workbook = null;
        try {
            // Determina o tipo de arquivo Excel
            if (originalFileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(excelFile.getInputStream());
            } else {
                workbook = new HSSFWorkbook(excelFile.getInputStream());
            }
            
            // Pega a primeira planilha
            Sheet sheet = workbook.getSheetAt(0);
            
            // Cria arquivo temporário CSV
            String csvFileName = originalFileName.replaceFirst("\\.[^.]+$", "_converted.csv");
            File csvFile = File.createTempFile("excel_to_csv_", ".csv");
            
            // Escreve os dados no CSV
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(csvFile), StandardCharsets.UTF_8);
                 PrintWriter printWriter = new PrintWriter(writer)) {
                
                // BOM para UTF-8 (para Excel reconhecer acentos)
                writer.write('\uFEFF');
                
                DataFormatter dataFormatter = new DataFormatter();
                FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
                
                for (Row row : sheet) {
                    List<String> cellValues = new ArrayList<>();
                    
                    // Processa todas as células da linha
                    int lastCellNum = row.getLastCellNum();
                    if (lastCellNum < 0) {
                        continue; // Linha vazia
                    }
                    
                    for (int i = 0; i < lastCellNum; i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        String cellValue = getCellValueAsString(cell, dataFormatter, formulaEvaluator);
                        cellValues.add(escapeForCsv(cellValue, separator));
                    }
                    
                    // Escreve a linha no CSV (apenas se não estiver vazia)
                    if (!cellValues.isEmpty() && !isEmptyRow(cellValues)) {
                        printWriter.println(String.join(separator, cellValues));
                    }
                }
            }
            
            return csvFile;
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }
    
    /**
     * Obtém o valor da célula como String
     */
    private static String getCellValueAsString(Cell cell, DataFormatter dataFormatter, FormulaEvaluator formulaEvaluator) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return dataFormatter.formatCellValue(cell);
                } else {
                    // Para números, remove decimais desnecessários
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return dataFormatter.formatCellValue(cell);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    CellValue cellValue = formulaEvaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue().trim();
                        case NUMERIC:
                            double numValue = cellValue.getNumberValue();
                            if (numValue == Math.floor(numValue)) {
                                return String.valueOf((long) numValue);
                            } else {
                                return String.valueOf(numValue);
                            }
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue());
                        default:
                            return "";
                    }
                } catch (Exception e) {
                    return dataFormatter.formatCellValue(cell);
                }
            case BLANK:
            default:
                return "";
        }
    }
    
    /**
     * Escapa valores para CSV (trata aspas e separadores)
     */
    private static String escapeForCsv(String value, String separator) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        
        // Se contém o separador, quebra de linha ou aspas, envolve em aspas
        if (value.contains(separator) || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            // Escapa aspas duplas
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        
        return value;
    }
    
    /**
     * Verifica se a linha está vazia
     */
    private static boolean isEmptyRow(List<String> cellValues) {
        return cellValues.stream().allMatch(value -> value == null || value.trim().isEmpty());
    }
    
    /**
     * Valida se o arquivo é um Excel válido
     */
    public static boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }
        
        String lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".xlsx") || lowerCaseFileName.endsWith(".xls");
    }
    
    /**
     * Obtém informações sobre o arquivo Excel
     */
    public static ExcelInfo getExcelInfo(MultipartFile excelFile) throws IOException {
        if (!isValidExcelFile(excelFile)) {
            throw new IllegalArgumentException("Arquivo não é um Excel válido");
        }
        
        Workbook workbook = null;
        try {
            String fileName = excelFile.getOriginalFilename();
            if (fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(excelFile.getInputStream());
            } else {
                workbook = new HSSFWorkbook(excelFile.getInputStream());
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum() + 1;
            int colCount = 0;
            
            if (sheet.getRow(0) != null) {
                colCount = sheet.getRow(0).getLastCellNum();
            }
            
            return new ExcelInfo(fileName, rowCount, colCount, sheet.getSheetName());
            
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }
    
    /**
     * Classe para informações do arquivo Excel
     */
    public static class ExcelInfo {
        private final String fileName;
        private final int rowCount;
        private final int columnCount;
        private final String sheetName;
        
        public ExcelInfo(String fileName, int rowCount, int columnCount, String sheetName) {
            this.fileName = fileName;
            this.rowCount = rowCount;
            this.columnCount = columnCount;
            this.sheetName = sheetName;
        }
        
        public String getFileName() { return fileName; }
        public int getRowCount() { return rowCount; }
        public int getColumnCount() { return columnCount; }
        public String getSheetName() { return sheetName; }
        
        @Override
        public String toString() {
            return String.format("Excel: %s, Planilha: %s, Linhas: %d, Colunas: %d", 
                               fileName, sheetName, rowCount, columnCount);
        }
    }
}
