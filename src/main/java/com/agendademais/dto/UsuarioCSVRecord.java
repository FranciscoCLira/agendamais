package com.agendademais.dto;

/**
 * DTO para representar um registro do CSV de usuários
 */
public class UsuarioCSVRecord {
    
    private String email;
    private String nome;
    private String celular;
    private String pais;
    private String estado;
    private String cidade;
    private String comentarios;
    private Long instituicaoId;
    private String identificacaoPessoaInstituicao;
    private Long subInstituicaoId;
    private String identificacaoPessoaSubInstituicao;
    
    // Campos gerados automaticamente
    private String username;
    private String password;
    private Integer numeroSequencial;
    
    public UsuarioCSVRecord() {}
    
    public UsuarioCSVRecord(String email, String nome, String celular, String pais, String estado, String cidade) {
        this.email = email;
        this.nome = nome;
        this.celular = celular;
        this.pais = pais;
        this.estado = estado;
        this.cidade = cidade;
    }

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

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public Long getInstituicaoId() {
        return instituicaoId;
    }

    public void setInstituicaoId(Long instituicaoId) {
        this.instituicaoId = instituicaoId;
    }

    public String getIdentificacaoPessoaInstituicao() {
        return identificacaoPessoaInstituicao;
    }

    public void setIdentificacaoPessoaInstituicao(String identificacaoPessoaInstituicao) {
        this.identificacaoPessoaInstituicao = identificacaoPessoaInstituicao;
    }

    public Long getSubInstituicaoId() {
        return subInstituicaoId;
    }

    public void setSubInstituicaoId(Long subInstituicaoId) {
        this.subInstituicaoId = subInstituicaoId;
    }

    public String getIdentificacaoPessoaSubInstituicao() {
        return identificacaoPessoaSubInstituicao;
    }

    public void setIdentificacaoPessoaSubInstituicao(String identificacaoPessoaSubInstituicao) {
        this.identificacaoPessoaSubInstituicao = identificacaoPessoaSubInstituicao;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getNumeroSequencial() {
        return numeroSequencial;
    }

    public void setNumeroSequencial(Integer numeroSequencial) {
        this.numeroSequencial = numeroSequencial;
    }
}
