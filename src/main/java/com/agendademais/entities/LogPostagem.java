package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
public class LogPostagem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Data e hora do log
	private java.time.LocalDateTime dataHoraPostagem;

	// ID da ocorrência de atividade
	private Long ocorrenciaAtividadeId;

	// Título da atividade (de OcorrenciaAtividade)
	private String tituloAtividade;

	// Assunto da divulgação (de OcorrenciaAtividade)
	private String assuntoDivulgacao;

	// Detalhe da divulgação (de OcorrenciaAtividade)
	@Column(columnDefinition = "TEXT")
	private String textoDetalheDivulgacao;

	// Autor (id)
	private Long autorId;

	// Quantidade de e-mails enviados
	private Integer qtEnviados;

	// Quantidade de e-mails com falha
	private Integer qtFalhas;

	// Mensagem de log (e-mails com falha, mensagens pertinentes)
	@Column(columnDefinition = "TEXT")
	private String mensagemLogPostagem;

	// GETTERS AND SETTERS
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public java.time.LocalDateTime getDataHoraPostagem() {
		return dataHoraPostagem;
	}

	public void setDataHoraPostagem(java.time.LocalDateTime dataHoraPostagem) {
		this.dataHoraPostagem = dataHoraPostagem;
	}

	public Long getOcorrenciaAtividadeId() {
		return ocorrenciaAtividadeId;
	}

	public void setOcorrenciaAtividadeId(Long ocorrenciaAtividadeId) {
		this.ocorrenciaAtividadeId = ocorrenciaAtividadeId;
	}

	public String getTituloAtividade() {
		return tituloAtividade;
	}

	public void setTituloAtividade(String tituloAtividade) {
		this.tituloAtividade = tituloAtividade;
	}

	public String getAssuntoDivulgacao() {
		return assuntoDivulgacao;
	}

	public void setAssuntoDivulgacao(String assuntoDivulgacao) {
		this.assuntoDivulgacao = assuntoDivulgacao;
	}

	public String getTextoDetalheDivulgacao() {
		return textoDetalheDivulgacao;
	}

	public void setTextoDetalheDivulgacao(String textoDetalheDivulgacao) {
		this.textoDetalheDivulgacao = textoDetalheDivulgacao;
	}

	public Long getAutorId() {
		return autorId;
	}

	public void setAutorId(Long autorId) {
		this.autorId = autorId;
	}

	public Integer getQtEnviados() {
		return qtEnviados;
	}

	public void setQtEnviados(Integer qtEnviados) {
		this.qtEnviados = qtEnviados;
	}

	public Integer getQtFalhas() {
		return qtFalhas;
	}

	public void setQtFalhas(Integer qtFalhas) {
		this.qtFalhas = qtFalhas;
	}

	public String getMensagemLogPostagem() {
		return mensagemLogPostagem;
	}

	public void setMensagemLogPostagem(String mensagemLogPostagem) {
		this.mensagemLogPostagem = mensagemLogPostagem;
	}
}