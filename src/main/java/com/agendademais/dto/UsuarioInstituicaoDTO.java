package com.agendademais.dto;

import com.agendademais.entities.UsuarioInstituicao;

public class UsuarioInstituicaoDTO {
    private UsuarioInstituicao usuarioInstituicao;
    private String nomeSubInstituicao;

    public UsuarioInstituicaoDTO(UsuarioInstituicao usuarioInstituicao, String nomeSubInstituicao) {
        this.usuarioInstituicao = usuarioInstituicao;
        this.nomeSubInstituicao = nomeSubInstituicao;
    }

    public UsuarioInstituicao getUsuarioInstituicao() {
        return usuarioInstituicao;
    }

    public void setUsuarioInstituicao(UsuarioInstituicao usuarioInstituicao) {
        this.usuarioInstituicao = usuarioInstituicao;
    }

    public String getNomeSubInstituicao() {
        return nomeSubInstituicao;
    }

    public void setNomeSubInstituicao(String nomeSubInstituicao) {
        this.nomeSubInstituicao = nomeSubInstituicao;
    }

    public String getSituacao() {
        if (usuarioInstituicao != null) {
            return usuarioInstituicao.getSitAcessoUsuarioInstituicao();
        }
        return null;
    }
}
