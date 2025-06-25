package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Atividade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TipoAtividade tipoAtividade;

    @ManyToOne
    private Instituicao idInstituicao;

    @ManyToOne
    private SubInstituicao idSubInstituicao;

    @ManyToOne
    private Pessoa idSolicitante;

    private String tituloAtividade;
    private String situacaoAtividade;
    private Integer formaApresentacao;

    @ElementCollection
    private List<String> emailsSolicitante;

    private Integer publicoAlvo;
    private String descricaoAtividade;
    private String comentariosAtividade;
    private String linkMaterialAtividade;
    private String linkAtividadeOnLine;
    private LocalDate dataAtualizacao;
    

    // GETTERS AND SETTERS 

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
	public Instituicao getIdInstituicao() {
		return idInstituicao;
	}
	public void setIdInstituicao(Instituicao idInstituicao) {
		this.idInstituicao = idInstituicao;
	}
	public SubInstituicao getIdSubInstituicao() {
		return idSubInstituicao;
	}
	public void setIdSubInstituicao(SubInstituicao idSubInstituicao) {
		this.idSubInstituicao = idSubInstituicao;
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
	public List<String> getEmailsSolicitante() {
		return emailsSolicitante;
	}
	public void setEmailsSolicitante(List<String> emailsSolicitante) {
		this.emailsSolicitante = emailsSolicitante;
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
