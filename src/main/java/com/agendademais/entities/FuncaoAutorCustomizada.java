package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FuncaoAutorCustomizada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String nomeFuncao;

    private String descricaoFuncao;
    
    private boolean ativa = true;
    
    private LocalDateTime dataCriacao = LocalDateTime.now();
    
    @ManyToOne
    private Usuario criadoPor;

    // Construtores
    public FuncaoAutorCustomizada() {}

    public FuncaoAutorCustomizada(String nomeFuncao, String descricaoFuncao, Usuario criadoPor) {
        this.nomeFuncao = nomeFuncao;
        this.descricaoFuncao = descricaoFuncao;
        this.criadoPor = criadoPor;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeFuncao() {
        return nomeFuncao;
    }

    public void setNomeFuncao(String nomeFuncao) {
        this.nomeFuncao = nomeFuncao;
    }

    public String getDescricaoFuncao() {
        return descricaoFuncao;
    }

    public void setDescricaoFuncao(String descricaoFuncao) {
        this.descricaoFuncao = descricaoFuncao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }
}
