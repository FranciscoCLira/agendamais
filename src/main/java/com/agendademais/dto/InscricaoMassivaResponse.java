package com.agendademais.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para response de carga massiva de inscrições
 */
public class InscricaoMassivaResponse {
    
    private boolean success = true;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    
    private int totalRegistros = 0;
    private int registrosProcessados = 0;
    private int registrosComErro = 0;
    private int inscricoesExistentes = 0;
    private int novasInscricoes = 0;
    
    private LocalDateTime inicioProcessamento;
    private LocalDateTime fimProcessamento;
    
    private String arquivoResultado;
    
    // Getters e Setters
    
    public boolean isSuccess() {
        return success && errors.isEmpty();
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public int getTotalRegistros() {
        return totalRegistros;
    }
    
    public void setTotalRegistros(int totalRegistros) {
        this.totalRegistros = totalRegistros;
    }
    
    public int getRegistrosProcessados() {
        return registrosProcessados;
    }
    
    public void setRegistrosProcessados(int registrosProcessados) {
        this.registrosProcessados = registrosProcessados;
    }
    
    public void incrementarProcessados() {
        this.registrosProcessados++;
    }
    
    public int getRegistrosComErro() {
        return registrosComErro;
    }
    
    public void setRegistrosComErro(int registrosComErro) {
        this.registrosComErro = registrosComErro;
    }
    
    public void incrementarErros() {
        this.registrosComErro++;
    }
    
    public int getInscricoesExistentes() {
        return inscricoesExistentes;
    }
    
    public void setInscricoesExistentes(int inscricoesExistentes) {
        this.inscricoesExistentes = inscricoesExistentes;
    }
    
    public void incrementarExistentes() {
        this.inscricoesExistentes++;
    }
    
    public int getNovasInscricoes() {
        return novasInscricoes;
    }
    
    public void setNovasInscricoes(int novasInscricoes) {
        this.novasInscricoes = novasInscricoes;
    }
    
    public void incrementarNovas() {
        this.novasInscricoes++;
    }
    
    public LocalDateTime getInicioProcessamento() {
        return inicioProcessamento;
    }
    
    public void setInicioProcessamento(LocalDateTime inicioProcessamento) {
        this.inicioProcessamento = inicioProcessamento;
    }
    
    public LocalDateTime getFimProcessamento() {
        return fimProcessamento;
    }
    
    public void setFimProcessamento(LocalDateTime fimProcessamento) {
        this.fimProcessamento = fimProcessamento;
    }
    
    public String getArquivoResultado() {
        return arquivoResultado;
    }
    
    public void setArquivoResultado(String arquivoResultado) {
        this.arquivoResultado = arquivoResultado;
    }
}
