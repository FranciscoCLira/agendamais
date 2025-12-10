package com.agendademais.dto;

import java.time.LocalDateTime;

/**
 * DTO para representar um registro de inscrição da planilha Excel do Microsoft
 * Forms
 * Colunas: B (data), G-O (email, nome, celular, identificacaoPessoaInstituicao,
 * identificacaoPessoaSubInstituicao, cidade, estado, pais, comentarios)
 */
public class InscricaoFormsRecord {

    // Coluna B - Data de preenchimento do formulário
    private LocalDateTime dataInclusaoForms; // Coluna B (formato: 25/05/2025 06:10:41)

    // Colunas da planilha (G a O)
    private String email; // Coluna G
    private String nome; // Coluna H
    private String celular; // Coluna I
    private String identificacaoPessoaInstituicao; // Coluna J
    private String identificacaoPessoaSubInstituicao; // Coluna K
    private String cidade; // Coluna L
    private String estado; // Coluna M
    private String pais; // Coluna N
    private String comentarios; // Coluna O

    // Controle de processamento
    private int linha;
    private boolean valido = true;
    private String mensagemErro;
    private String mensagemSucesso;

    // Credenciais geradas
    private String usuarioGerado;
    private String senhaGerada;

    // Getters e Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getIdentificacaoPessoaInstituicao() {
        return identificacaoPessoaInstituicao;
    }

    public void setIdentificacaoPessoaInstituicao(String identificacaoPessoaInstituicao) {
        this.identificacaoPessoaInstituicao = identificacaoPessoaInstituicao;
    }

    public String getIdentificacaoPessoaSubInstituicao() {
        return identificacaoPessoaSubInstituicao;
    }

    public void setIdentificacaoPessoaSubInstituicao(String identificacaoPessoaSubInstituicao) {
        this.identificacaoPessoaSubInstituicao = identificacaoPessoaSubInstituicao;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public int getLinha() {
        return linha;
    }

    public void setLinha(int linha) {
        this.linha = linha;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public void setMensagemErro(String mensagemErro) {
        this.mensagemErro = mensagemErro;
        this.valido = false;
    }

    public String getMensagemSucesso() {
        return mensagemSucesso;
    }

    public void setMensagemSucesso(String mensagemSucesso) {
        this.mensagemSucesso = mensagemSucesso;
    }

    public String getUsuarioGerado() {
        return usuarioGerado;
    }

    public void setUsuarioGerado(String usuarioGerado) {
        this.usuarioGerado = usuarioGerado;
    }

    public String getSenhaGerada() {
        return senhaGerada;
    }

    public void setSenhaGerada(String senhaGerada) {
        this.senhaGerada = senhaGerada;
    }

    public LocalDateTime getDataInclusaoForms() {
        return dataInclusaoForms;
    }

    public void setDataInclusaoForms(LocalDateTime dataInclusaoForms) {
        this.dataInclusaoForms = dataInclusaoForms;
    }
}
