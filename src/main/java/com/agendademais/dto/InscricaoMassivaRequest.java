package com.agendademais.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para request de carga massiva de inscrições em tipos de atividade
 */
public class InscricaoMassivaRequest {
    
    private Long subInstituicaoId;
    private Long tipoAtividadeId;
    private MultipartFile arquivo;
    private String tipoCarga = "producao"; // teste ou producao
    private boolean gerarArquivoResultado = true;
    
    // Getters e Setters
    
    public Long getSubInstituicaoId() {
        return subInstituicaoId;
    }
    
    public void setSubInstituicaoId(Long subInstituicaoId) {
        this.subInstituicaoId = subInstituicaoId;
    }
    
    public Long getTipoAtividadeId() {
        return tipoAtividadeId;
    }
    
    public void setTipoAtividadeId(Long tipoAtividadeId) {
        this.tipoAtividadeId = tipoAtividadeId;
    }
    
    public MultipartFile getArquivo() {
        return arquivo;
    }
    
    public void setArquivo(MultipartFile arquivo) {
        this.arquivo = arquivo;
    }
    
    public String getTipoCarga() {
        return tipoCarga;
    }
    
    public void setTipoCarga(String tipoCarga) {
        this.tipoCarga = tipoCarga;
    }
    
    public boolean isGerarArquivoResultado() {
        return gerarArquivoResultado;
    }
    
    public void setGerarArquivoResultado(boolean gerarArquivoResultado) {
        this.gerarArquivoResultado = gerarArquivoResultado;
    }
}
