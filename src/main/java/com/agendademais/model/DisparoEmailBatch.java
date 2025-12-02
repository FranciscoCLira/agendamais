package com.agendademais.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Imports necessários para relacionamentos
import com.agendademais.entities.Instituicao;
import com.agendademais.entities.SubInstituicao;
import com.agendademais.entities.Usuario;

/**
 * Entidade para gerenciamento de disparos em lote de emails genéricos.
 * Suporta diferentes tipos de disparo (boas-vindas, informativos, campanhas).
 */
@Entity
@Table(name = "disparo_email_batch")
public class DisparoEmailBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_disparo", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoDisparo tipoDisparo;

    @ManyToOne
    @JoinColumn(name = "instituicao_id", nullable = false)
    private Instituicao instituicao;

    @ManyToOne
    @JoinColumn(name = "sub_instituicao_id")
    private SubInstituicao subInstituicao;

    @ManyToOne
    @JoinColumn(name = "usuario_criador_id", nullable = false)
    private Usuario usuarioCriador;

    // Filtros
    @Column(name = "filtro_situacao_usuario", length = 10)
    private String filtroSituacaoUsuario;

    @Column(name = "filtro_tipo_atividade_ids", columnDefinition = "TEXT")
    private String filtroTipoAtividadeIds;

    @Column(name = "filtro_nivel_acesso", length = 10)
    private String filtroNivelAcesso;

    @Column(name = "filtro_data_inclusao_inicio")
    private LocalDate filtroDataInscricaoInicio;

    @Column(name = "filtro_data_inclusao_fim")
    private LocalDate filtroDataInscricaoFim;

    // Conteúdo
    @Column(name = "assunto", nullable = false)
    private String assunto;

    @Column(name = "corpo_html", nullable = false, columnDefinition = "TEXT")
    private String corpoHtml;

    // Controle
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatusDisparo status = StatusDisparo.PENDENTE;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_inicio_processamento")
    private LocalDateTime dataInicioProcessamento;

    @Column(name = "data_fim_processamento")
    private LocalDateTime dataFimProcessamento;

    // Estatísticas
    @Column(name = "total_destinatarios")
    private Integer totalDestinatarios = 0;

    @Column(name = "emails_enviados")
    private Integer emailsEnviados = 0;

    @Column(name = "emails_falhados")
    private Integer emailsFalhados = 0;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    // Enums
    public enum TipoDisparo {
        BOAS_VINDAS,
        INFORMATIVO,
        CAMPANHA
    }

    public enum StatusDisparo {
        PENDENTE,
        PROCESSANDO,
        CONCLUIDO,
        ERRO,
        CANCELADO
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoDisparo getTipoDisparo() {
        return tipoDisparo;
    }

    public void setTipoDisparo(TipoDisparo tipoDisparo) {
        this.tipoDisparo = tipoDisparo;
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

    public Usuario getUsuarioCriador() {
        return usuarioCriador;
    }

    public void setUsuarioCriador(Usuario usuarioCriador) {
        this.usuarioCriador = usuarioCriador;
    }

    public String getFiltroSituacaoUsuario() {
        return filtroSituacaoUsuario;
    }

    public void setFiltroSituacaoUsuario(String filtroSituacaoUsuario) {
        this.filtroSituacaoUsuario = filtroSituacaoUsuario;
    }

    public String getFiltroTipoAtividadeIds() {
        return filtroTipoAtividadeIds;
    }

    public void setFiltroTipoAtividadeIds(String filtroTipoAtividadeIds) {
        this.filtroTipoAtividadeIds = filtroTipoAtividadeIds;
    }

    public String getFiltroNivelAcesso() {
        return filtroNivelAcesso;
    }

    public void setFiltroNivelAcesso(String filtroNivelAcesso) {
        this.filtroNivelAcesso = filtroNivelAcesso;
    }

    public LocalDate getFiltroDataInscricaoInicio() {
        return filtroDataInscricaoInicio;
    }

    public void setFiltroDataInscricaoInicio(LocalDate filtroDataInscricaoInicio) {
        this.filtroDataInscricaoInicio = filtroDataInscricaoInicio;
    }

    public LocalDate getFiltroDataInscricaoFim() {
        return filtroDataInscricaoFim;
    }

    public void setFiltroDataInscricaoFim(LocalDate filtroDataInscricaoFim) {
        this.filtroDataInscricaoFim = filtroDataInscricaoFim;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getCorpoHtml() {
        return corpoHtml;
    }

    public void setCorpoHtml(String corpoHtml) {
        this.corpoHtml = corpoHtml;
    }

    public StatusDisparo getStatus() {
        return status;
    }

    public void setStatus(StatusDisparo status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataInicioProcessamento() {
        return dataInicioProcessamento;
    }

    public void setDataInicioProcessamento(LocalDateTime dataInicioProcessamento) {
        this.dataInicioProcessamento = dataInicioProcessamento;
    }

    public LocalDateTime getDataFimProcessamento() {
        return dataFimProcessamento;
    }

    public void setDataFimProcessamento(LocalDateTime dataFimProcessamento) {
        this.dataFimProcessamento = dataFimProcessamento;
    }

    public Integer getTotalDestinatarios() {
        return totalDestinatarios;
    }

    public void setTotalDestinatarios(Integer totalDestinatarios) {
        this.totalDestinatarios = totalDestinatarios;
    }

    public Integer getEmailsEnviados() {
        return emailsEnviados;
    }

    public void setEmailsEnviados(Integer emailsEnviados) {
        this.emailsEnviados = emailsEnviados;
    }

    public Integer getEmailsFalhados() {
        return emailsFalhados;
    }

    public void setEmailsFalhados(Integer emailsFalhados) {
        this.emailsFalhados = emailsFalhados;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
    }
}
