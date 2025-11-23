package com.agendademais.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Usuario implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, nullable = false, unique = true)
    @Size(min = 4, max = 25, message = "Usuário deve ter entre 4 e 25 caracteres.")
    private String username;

    @Column(nullable = false)
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{6,}$", message = "A senha deve conter letras, números e símbolos.")
    private String password;

    // REMOVIDO: nivelAcessoUsuario - agora está em UsuarioInstituicao

    // // nullable = false exige obrigatoriedade typeof XLSX

    // @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "pessoa_id", referencedColumnName = "id", nullable = false)
    private Pessoa pessoa;

    private String situacaoUsuario; // A=Ativo, B=Bloqueado, P=Pendente de Ativação de alteração senha

    private LocalDate dataUltimaAtualizacao;

    @Column(name = "token_recuperacao", length = 36)
    private String tokenRecuperacao;

    @Column(name = "data_expiracao_token")
    private LocalDateTime dataExpiracaoToken;

    // Getters e setters
    public Long getId() {
        return id;
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

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public String getSituacaoUsuario() {
        return situacaoUsuario;
    }

    public void setSituacaoUsuario(String situacaoUsuario) {
        this.situacaoUsuario = situacaoUsuario;
    }

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    public String getTokenRecuperacao() {
        return tokenRecuperacao;
    }

    public void setTokenRecuperacao(String tokenRecuperacao) {
        this.tokenRecuperacao = tokenRecuperacao;
    }

    public LocalDateTime getDataExpiracaoToken() {
        return dataExpiracaoToken;
    }

    public void setDataExpiracaoToken(LocalDateTime dataExpiracaoToken) {
        this.dataExpiracaoToken = dataExpiracaoToken;
    }

    @PreUpdate
    public void onUpdate() {
        this.dataUltimaAtualizacao = LocalDate.now();
    }
}
