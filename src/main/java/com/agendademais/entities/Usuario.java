package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cod_usuario", length = 25, unique = true, nullable = false)
    private String codUsuario;

    private String senha;
    private Integer nivelAcessoUsuario;

    
    // GETTERS AND SETTERS 

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
	public Integer getNivelAcessoUsuario() {
		return nivelAcessoUsuario;
	}
	public void setNivelAcessoUsuario(Integer nivelAcessoUsuario) {
		this.nivelAcessoUsuario = nivelAcessoUsuario;
	}
}
