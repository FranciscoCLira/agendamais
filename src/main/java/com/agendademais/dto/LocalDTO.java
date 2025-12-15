package com.agendademais.dto;

import java.io.Serializable;

/**
 * DTO para representar Local (País, Estado, Cidade) em respostas JSON
 * Evita problemas de serialização com referências circulares
 */
public class LocalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer tipoLocal;
    private String nomeLocal;
    private String revisadoLocal;

    public LocalDTO() {
    }

    public LocalDTO(Long id, Integer tipoLocal, String nomeLocal, String revisadoLocal) {
        this.id = id;
        this.tipoLocal = tipoLocal;
        this.nomeLocal = nomeLocal;
        this.revisadoLocal = revisadoLocal;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTipoLocal() {
        return tipoLocal;
    }

    public void setTipoLocal(Integer tipoLocal) {
        this.tipoLocal = tipoLocal;
    }

    public String getNomeLocal() {
        return nomeLocal;
    }

    public void setNomeLocal(String nomeLocal) {
        this.nomeLocal = nomeLocal;
    }

    public String getRevisadoLocal() {
        return revisadoLocal;
    }

    public void setRevisadoLocal(String revisadoLocal) {
        this.revisadoLocal = revisadoLocal;
    }
}
