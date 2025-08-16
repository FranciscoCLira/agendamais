package com.agendademais.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para resposta da carga massiva de dados
 */
public class DataEntryResponse {
    
    private boolean sucesso = true;
    private String mensagem;
    private int totalRegistros;
    private int registrosProcessados;
    private int registrosIncluidos;
    private int registrosLidos;
    private int registrosComErro;
    private List<String> erros;
    private List<String> avisos;
    private List<String> informacoes;
    private String arquivoGerado; // Path do CSV com credenciais geradas
    private LocalDateTime inicioProcessamento;
    private LocalDateTime fimProcessamento;
    
    public DataEntryResponse() {
        this.erros = new ArrayList<>();
        this.avisos = new ArrayList<>();
        this.informacoes = new ArrayList<>();
    }
    
    public DataEntryResponse(boolean sucesso, String mensagem) {
        this();
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    
    // Métodos utilitários
    public void addError(String erro) {
        if (this.erros == null) {
            this.erros = new ArrayList<>();
        }
        this.erros.add(erro);
        this.registrosComErro++;
        this.sucesso = false;
    }
    
    public void addWarning(String aviso) {
        if (this.avisos == null) {
            this.avisos = new ArrayList<>();
        }
        this.avisos.add(aviso);
    }
    
    public void addInfo(String info) {
        if (this.informacoes == null) {
            this.informacoes = new ArrayList<>();
        }
        this.informacoes.add(info);
    }
    
    public void adicionarErro(String erro) {
        addError(erro);
    }
    
    public void adicionarAviso(String aviso) {
        addWarning(aviso);
    }
    
    public void incrementarRegistrosProcessados() {
        this.registrosProcessados++;
    }
    
    public void incrementarRegistrosIncluidos() {
        this.registrosIncluidos++;
    }
    
    public void incrementarRegistrosLidos() {
        this.registrosLidos++;
    }
    
    public boolean isSuccess() {
        return sucesso && erros.isEmpty();
    }
    
    
    // Getters e Setters
    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
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
    
    public int getRegistrosIncluidos() {
        return registrosIncluidos;
    }

    public void setRegistrosIncluidos(int registrosIncluidos) {
        this.registrosIncluidos = registrosIncluidos;
    }
    
    public int getRegistrosLidos() {
        return registrosLidos;
    }

    public void setRegistrosLidos(int registrosLidos) {
        this.registrosLidos = registrosLidos;
    }

    public int getRegistrosComErro() {
        return registrosComErro;
    }

    public void setRegistrosComErro(int registrosComErro) {
        this.registrosComErro = registrosComErro;
    }

    public List<String> getErros() {
        return erros;
    }

    public void setErros(List<String> erros) {
        this.erros = erros;
    }

    public List<String> getAvisos() {
        return avisos;
    }

    public void setAvisos(List<String> avisos) {
        this.avisos = avisos;
    }
    
    public List<String> getInformacoes() {
        return informacoes;
    }

    public void setInformacoes(List<String> informacoes) {
        this.informacoes = informacoes;
    }

    public String getArquivoGerado() {
        return arquivoGerado;
    }

    public void setArquivoGerado(String arquivoGerado) {
        this.arquivoGerado = arquivoGerado;
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
}
