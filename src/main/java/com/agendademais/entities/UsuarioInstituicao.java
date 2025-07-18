package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
public class UsuarioInstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    private String sitAcessoUsuarioInstituicao; // A=Ativo, B=Bloqueado, C=Cancelado

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public String getSitAcessoUsuarioInstituicao() {
        return sitAcessoUsuarioInstituicao;
    }

    public void setSitAcessoUsuarioInstituicao(String sitAcessoUsuarioInstituicao) {
        this.sitAcessoUsuarioInstituicao = sitAcessoUsuarioInstituicao;
    }
}        
