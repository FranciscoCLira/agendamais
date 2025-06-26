package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, unique = true, nullable = false)
    private String codUsuario;

    @Column(nullable = false)
    private String senha;

    // 1=Participante, 2=Autor, 5=Administrador, 9=SuperUsuario
    private int nivelAcessoUsuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}        
