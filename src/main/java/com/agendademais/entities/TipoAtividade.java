package com.agendademais.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
public class TipoAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tituloTipoAtividade;
    private String descricaoTipoAtividade;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    @JsonIgnoreProperties({ "tiposAtividade", "subInstituicoes", "pessoas", "hibernateLazyInitializer", "handler" })
    private Instituicao instituicao;

    @OneToMany(mappedBy = "tipoAtividade")
    @JsonIgnoreProperties({ "tipoAtividade", "atividade", "pessoa", "hibernateLazyInitializer", "handler" })
    private Set<InscricaoTipoAtividade> inscricoes = new HashSet<>();

    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTituloTipoAtividade() {
        return tituloTipoAtividade;
    }

    public void setTituloTipoAtividade(String tituloTipoAtividade) {
        this.tituloTipoAtividade = tituloTipoAtividade;
    }

    public String getDescricaoTipoAtividade() {
        return descricaoTipoAtividade;
    }

    public void setDescricaoTipoAtividade(String descricaoTipoAtividade) {
        this.descricaoTipoAtividade = descricaoTipoAtividade;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }
}
