package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Local {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer tipoLocal; // 1=País, 2=Estado, 3=Cidade
    private String nomeLocal;
    @ManyToOne
    @JoinColumn(name = "id_pai")
    private Local localPai;

    // Novos campos para controle e auditoria
    private String revisadoLocal = "n"; // "s"=sim, "n"=não (padrão "n")
    private LocalDate dataUltimaAtualizacao;

    // --- Constructors ---

    public Local() {
        this.revisadoLocal = "n";
        this.dataUltimaAtualizacao = LocalDate.now();
    } // Construtor padrão

    public Local(int tipoLocal, String nomeLocal, Local localPai) {
        this.tipoLocal = tipoLocal;
        this.nomeLocal = nomeLocal;
        this.localPai = localPai;
        this.revisadoLocal = "n";
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    // --- Getters e Setters ---

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
        this.dataUltimaAtualizacao = LocalDate.now(); // Atualiza data ao alterar nome
    }

    public Local getLocalPai() {
        return localPai;
    }

    public void setLocalPai(Local localPai) {
        this.localPai = localPai;
    }

    // Novos getters e setters
    public String getRevisadoLocal() {
        return revisadoLocal;
    }

    public void setRevisadoLocal(String revisadoLocal) {
        this.revisadoLocal = revisadoLocal;
        this.dataUltimaAtualizacao = LocalDate.now(); // Atualiza data ao revisar
    }

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }
}
