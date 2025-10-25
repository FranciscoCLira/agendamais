package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Instituicao {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String nomeInstituicao;
	private String situacaoInstituicao; // A=Ativa, I=Inativa, B=Bloqueada
	private LocalDate dataUltimaAtualizacao;
	private String emailInstituicao;
	// SMTP settings (optional) to allow sending using institution SMTP credentials
	private String smtpHost;
	private Integer smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private Boolean smtpSsl; // true = SSL (465) / false = STARTTLS (587)

	// GETTERS AND SETTERS

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeInstituicao() {
		return nomeInstituicao;
	}

	public void setNomeInstituicao(String nomeInstituicao) {
		this.nomeInstituicao = nomeInstituicao;
	}

	public String getSituacaoInstituicao() {
		return situacaoInstituicao;
	}

	public void setSituacaoInstituicao(String situacaoInstituicao) {
		this.situacaoInstituicao = situacaoInstituicao;
	}

	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}

	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}

	public String getEmailInstituicao() {
		return emailInstituicao;
	}

	public void setEmailInstituicao(String emailInstituicao) {
		this.emailInstituicao = emailInstituicao;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public Boolean getSmtpSsl() {
		return smtpSsl;
	}

	public void setSmtpSsl(Boolean smtpSsl) {
		this.smtpSsl = smtpSsl;
	}

	@Override
	public String toString() {
		return "Instituicao [id=" + id + ", nomeInstituicao=" + nomeInstituicao + ", situacaoInstituicao="
				+ situacaoInstituicao + ", dataUltimaAtualizacao=" + dataUltimaAtualizacao + "]";
	}

}
