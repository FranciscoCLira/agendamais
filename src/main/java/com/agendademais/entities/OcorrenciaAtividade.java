package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class OcorrenciaAtividade {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Atividade idAtividade;

	@ManyToOne
	private Autor idAutor;

	private String temaOcorrencia;
	private String situacaoOcorrencia; // P=Programada, R=Realizada, C=Cancelada
	private String bibliografia;
	private LocalDate dataOcorrencia;
	private LocalTime horaInicioOcorrencia;
	private LocalTime horaFimOcorrencia;
	private String linkMaterialTema;
	private String assuntoDivulgacao;
	@Lob
	private String detalheDivulgacao;
	private String linkImgDivulgacao;
	private Integer qtdeParticipantes;
	private String obsEncerramento;
	private LocalDate dataAtualizacao;

	// GETTERS AND SETTERS

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Atividade getIdAtividade() {
		return idAtividade;
	}

	public void setIdAtividade(Atividade idAtividade) {
		this.idAtividade = idAtividade;
	}

	public Autor getIdAutor() {
		return idAutor;
	}

	public void setIdAutor(Autor idAutor) {
		this.idAutor = idAutor;
	}

	public String getTemaOcorrencia() {
		return temaOcorrencia;
	}

	public void setTemaOcorrencia(String temaOcorrencia) {
		this.temaOcorrencia = temaOcorrencia;
	}

	public String getSituacaoOcorrencia() {
		return situacaoOcorrencia;
	}

	public void setSituacaoOcorrencia(String situacaoOcorrencia) {
		this.situacaoOcorrencia = situacaoOcorrencia;
	}

	public String getBibliografia() {
		return bibliografia;
	}

	public void setBibliografia(String bibliografia) {
		this.bibliografia = bibliografia;
	}

	public LocalDate getDataOcorrencia() {
		return dataOcorrencia;
	}

	public void setDataOcorrencia(LocalDate dataOcorrencia) {
		this.dataOcorrencia = dataOcorrencia;
	}

	public LocalTime getHoraInicioOcorrencia() {
		return horaInicioOcorrencia;
	}

	public void setHoraInicioOcorrencia(LocalTime horaInicioOcorrencia) {
		this.horaInicioOcorrencia = horaInicioOcorrencia;
	}

	public LocalTime getHoraFimOcorrencia() {
		return horaFimOcorrencia;
	}

	public void setHoraFimOcorrencia(LocalTime horaFimOcorrencia) {
		this.horaFimOcorrencia = horaFimOcorrencia;
	}

	public String getLinkMaterialTema() {
		return linkMaterialTema;
	}

	public void setLinkMaterialTema(String linkMaterialTema) {
		this.linkMaterialTema = linkMaterialTema;
	}

	public String getAssuntoDivulgacao() {
		return assuntoDivulgacao;
	}

	public void setAssuntoDivulgacao(String assuntoDivulgacao) {
		this.assuntoDivulgacao = assuntoDivulgacao;
	}

	public String getDetalheDivulgacao() {
		return detalheDivulgacao;
	}

	public void setDetalheDivulgacao(String detalheDivulgacao) {
		this.detalheDivulgacao = detalheDivulgacao;
	}

	public String getLinkImgDivulgacao() {
		return linkImgDivulgacao;
	}

	public void setLinkImgDivulgacao(String linkImgDivulgacao) {
		this.linkImgDivulgacao = linkImgDivulgacao;
	}

	public Integer getQtdeParticipantes() {
		return qtdeParticipantes;
	}

	public void setQtdeParticipantes(Integer qtdeParticipantes) {
		this.qtdeParticipantes = qtdeParticipantes;
	}

	public String getObsEncerramento() {
		return obsEncerramento;
	}

	public void setObsEncerramento(String obsEncerramento) {
		this.obsEncerramento = obsEncerramento;
	}

	public LocalDate getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(LocalDate dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
}
