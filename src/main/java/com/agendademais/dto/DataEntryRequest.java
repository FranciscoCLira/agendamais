package com.agendademais.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para requisições de carga massiva de dados
 */
public class DataEntryRequest {
    
    private MultipartFile arquivo;
    private String tipoArquivo; // "csv", "excel"
    private String tipoCarga; // "teste", "real"
    private String formatoUsuario; // "incremental", "email", "custom"
    private String prefixoUsuario; // "X" para teste, "" para real
    private String separadorCsv = ";"; // ";", ",", "\t"
    private boolean validarCelular = true;
    private boolean gerarSenhaAutomatica = true;
    private boolean gerarArquivoResultado = true;
    private Long instituicaoId;
    private Long subInstituicaoId;
    
    // Construtores
    public DataEntryRequest() {}

    // Getters e Setters
    public MultipartFile getArquivo() {
        return arquivo;
    }

    public void setArquivo(MultipartFile arquivo) {
        this.arquivo = arquivo;
    }

    public String getTipoArquivo() {
        return tipoArquivo;
    }

    public void setTipoArquivo(String tipoArquivo) {
        this.tipoArquivo = tipoArquivo;
    }

    public String getTipoCarga() {
        return tipoCarga;
    }

    public void setTipoCarga(String tipoCarga) {
        this.tipoCarga = tipoCarga;
    }

    public String getFormatoUsuario() {
        return formatoUsuario;
    }

    public void setFormatoUsuario(String formatoUsuario) {
        this.formatoUsuario = formatoUsuario;
    }

    public String getPrefixoUsuario() {
        return prefixoUsuario;
    }

    public void setPrefixoUsuario(String prefixoUsuario) {
        this.prefixoUsuario = prefixoUsuario;
    }

    public String getSeparadorCsv() {
        return separadorCsv != null ? separadorCsv : ";";
    }

    public void setSeparadorCsv(String separadorCsv) {
        this.separadorCsv = separadorCsv;
    }
    
    public String getSeparadorCSV() {
        return getSeparadorCsv();
    }

    public boolean isValidarCelular() {
        return validarCelular;
    }

    public void setValidarCelular(boolean validarCelular) {
        this.validarCelular = validarCelular;
    }

    public boolean isGerarSenhaAutomatica() {
        return gerarSenhaAutomatica;
    }

    public void setGerarSenhaAutomatica(boolean gerarSenhaAutomatica) {
        this.gerarSenhaAutomatica = gerarSenhaAutomatica;
    }
    
    public boolean isGerarArquivoResultado() {
        return gerarArquivoResultado;
    }

    public void setGerarArquivoResultado(boolean gerarArquivoResultado) {
        this.gerarArquivoResultado = gerarArquivoResultado;
    }

    public Long getInstituicaoId() {
        return instituicaoId;
    }

    public void setInstituicaoId(Long instituicaoId) {
        this.instituicaoId = instituicaoId;
    }

    public Long getSubInstituicaoId() {
        return subInstituicaoId;
    }

    public void setSubInstituicaoId(Long subInstituicaoId) {
        this.subInstituicaoId = subInstituicaoId;
    }
}
