package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidade que armazena configurações SMTP globais do sistema
 * Gerenciável apenas por usuários de nível 0 (Controle Total)
 */
@Entity
@Table(name = "configuracao_smtp_global")
public class ConfiguracaoSmtpGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "smtp_host", nullable = false, length = 255)
    private String smtpHost;

    @Column(name = "smtp_port", nullable = false)
    private Integer smtpPort = 587;

    @Column(name = "smtp_username", nullable = false, length = 255)
    private String smtpUsername;

    @Column(name = "smtp_password", nullable = false, columnDefinition = "TEXT")
    private String smtpPassword; // Criptografado com Jasypt (formato ENC(...))

    @Column(name = "smtp_nome_remetente", length = 255)
    private String smtpNomeRemetente;

    @Column(name = "smtp_email_remetente", length = 255)
    private String smtpEmailRemetente;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "usuario_atualizacao_id")
    private Usuario usuarioAtualizacao;

    // Construtores
    public ConfiguracaoSmtpGlobal() {}

    public ConfiguracaoSmtpGlobal(String smtpHost, Integer smtpPort, String smtpUsername, String smtpPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    // Callback para atualizar data antes de persistir
    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSmtpNomeRemetente() {
        return smtpNomeRemetente;
    }

    public void setSmtpNomeRemetente(String smtpNomeRemetente) {
        this.smtpNomeRemetente = smtpNomeRemetente;
    }

    public String getSmtpEmailRemetente() {
        return smtpEmailRemetente;
    }

    public void setSmtpEmailRemetente(String smtpEmailRemetente) {
        this.smtpEmailRemetente = smtpEmailRemetente;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Usuario getUsuarioAtualizacao() {
        return usuarioAtualizacao;
    }

    public void setUsuarioAtualizacao(Usuario usuarioAtualizacao) {
        this.usuarioAtualizacao = usuarioAtualizacao;
    }

    @Override
    public String toString() {
        return "ConfiguracaoSmtpGlobal{" +
                "id=" + id +
                ", smtpHost='" + smtpHost + '\'' +
                ", smtpPort=" + smtpPort +
                ", smtpUsername='" + smtpUsername + '\'' +
                ", ativo=" + ativo +
                ", dataAtualizacao=" + dataAtualizacao +
                '}';
    }
}
