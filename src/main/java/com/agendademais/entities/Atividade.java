
package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tipo_atividade_id", nullable = false)
    private TipoAtividade tipoAtividade;

    @ManyToOne
    @JoinColumn(name = "id_instituicao", nullable = false)
    private Instituicao instituicao;

    @ManyToOne
    @JoinColumn(name = "id_sub_instituicao")
    private SubInstituicao subInstituicao;

    @ManyToOne
    @JoinColumn(name = "id_solicitante")
    private Pessoa idSolicitante;

    @Column(length = 30)
    private String tituloAtividade;

    private String situacaoAtividade; // P=Proposta, A=Aprovada, R=Rejeitada, C=Cancelada, F=Finalizada
    private Integer formaApresentacao; // 1=Presencial, 2=Online, 3=Híbrido, 4=outro
    private Integer publicoAlvo; // 1-Publico, 2=Restrito, 3=Academico, 4=outro
    private String descricaoAtividade;
    private String comentariosAtividade;
    private String linkMaterialAtividade;
    private String linkAtividadeOnLine;
    private LocalDate dataAtualizacao;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoAtividade getTipoAtividade() {
        return tipoAtividade;
    }

    public void setTipoAtividade(TipoAtividade tipoAtividade) {
        this.tipoAtividade = tipoAtividade;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public SubInstituicao getSubInstituicao() {
        return subInstituicao;
    }

    public void setSubInstituicao(SubInstituicao subInstituicao) {
        this.subInstituicao = subInstituicao;
    }

    public Pessoa getIdSolicitante() {
        return idSolicitante;
    }

    public void setIdSolicitante(Pessoa idSolicitante) {
        this.idSolicitante = idSolicitante;
    }

    public String getTituloAtividade() {
        return tituloAtividade;
    }

    public void setTituloAtividade(String tituloAtividade) {
        this.tituloAtividade = tituloAtividade;
    }

    public String getSituacaoAtividade() {
        return situacaoAtividade;
    }

    public void setSituacaoAtividade(String situacaoAtividade) {
        this.situacaoAtividade = situacaoAtividade;
    }

    public Integer getFormaApresentacao() {
        return formaApresentacao;
    }

    public void setFormaApresentacao(Integer formaApresentacao) {
        this.formaApresentacao = formaApresentacao;
    }

    public Integer getPublicoAlvo() {
        return publicoAlvo;
    }

    public void setPublicoAlvo(Integer publicoAlvo) {
        this.publicoAlvo = publicoAlvo;
    }

    public String getDescricaoAtividade() {
        return descricaoAtividade;
    }

    public void setDescricaoAtividade(String descricaoAtividade) {
        this.descricaoAtividade = descricaoAtividade;
    }

    public String getComentariosAtividade() {
        return comentariosAtividade;
    }

    public void setComentariosAtividade(String comentariosAtividade) {
        this.comentariosAtividade = comentariosAtividade;
    }

    public String getLinkMaterialAtividade() {
        return linkMaterialAtividade;
    }

    public void setLinkMaterialAtividade(String linkMaterialAtividade) {
        this.linkMaterialAtividade = linkMaterialAtividade;
    }

    public String getLinkAtividadeOnLine() {
        return linkAtividadeOnLine;
    }

    public void setLinkAtividadeOnLine(String linkAtividadeOnLine) {
        this.linkAtividadeOnLine = linkAtividadeOnLine;
    }

    public LocalDate getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDate dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
