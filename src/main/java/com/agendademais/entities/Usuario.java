package com.agendademais.entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, nullable = false, unique = true)
    private String codUsuario;

    @Column(nullable = false)
    private String senha;


    // 1=Participante, 2=Autor, 5=Administrador, 9=SuperUsuario
    private int nivelAcessoUsuario;

    // // nullable = false exige obrigatoriedade
    
    @OneToOne
    @JoinColumn(name = "pessoa_id", referencedColumnName = "id", nullable = false)
    private Pessoa pessoa;

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
}
