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
    private String codUsuario;

    
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{6,}$",
        message = "A senha deve conter letras, números e símbolos."
    )    
    private String senha;
    
    
    // 1=Participante, 2=Autor, 5=Administrador, 9=SuperUsuario
    private int nivelAcessoUsuario;

    // // nullable = false exige obrigatoriedade
    
    @OneToOne
    @JoinColumn(name = "pessoa_id", referencedColumnName = "id", nullable = false)
    private Pessoa pessoa;

    private String situacaoUsuario; // A=Ativo, B=Bloqueado

    private LocalDate dataUltimaAtualizacao;
    
    @Column(name = "token_recuperacao", length = 36)
    private String tokenRecuperacao;

    @Column(name = "data_expiracao_token")
    private LocalDateTime dataExpiracaoToken;
    
    
    // Getters e setters
    public Long getId() {
        return id;
    }

    public String getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(String codUsuario) {
        this.codUsuario = codUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getNivelAcessoUsuario() {
        return nivelAcessoUsuario;
    }

    public void setNivelAcessoUsuario(int nivelAcessoUsuario) {
        this.nivelAcessoUsuario = nivelAcessoUsuario;
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
