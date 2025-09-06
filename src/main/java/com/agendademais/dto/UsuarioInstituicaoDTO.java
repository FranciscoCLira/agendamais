package com.agendademais.dto;

import com.agendademais.entities.UsuarioInstituicao;
import com.agendademais.entities.Pessoa;

public class UsuarioInstituicaoDTO {
    public String getLocalizacaoFormatada() {
        if (usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null
                && usuarioInstituicao.getUsuario().getPessoa() != null) {
            var pessoa = usuarioInstituicao.getUsuario().getPessoa();
            String pais = (pessoa.getPais() != null && pessoa.getPais().getNomeLocal() != null)
                    ? pessoa.getPais().getNomeLocal()
                    : "---";
            String estado = (pessoa.getEstado() != null && pessoa.getEstado().getNomeLocal() != null)
                    ? pessoa.getEstado().getNomeLocal()
                    : "---";
            String cidade = (pessoa.getCidade() != null && pessoa.getCidade().getNomeLocal() != null)
                    ? pessoa.getCidade().getNomeLocal()
                    : "---";
            return pais + " / " + estado + " / " + cidade;
        }
        return "--- / --- / ---";
    }

    public String getNomePessoa() {
        if (usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null
                && usuarioInstituicao.getUsuario().getPessoa() != null) {
            return usuarioInstituicao.getUsuario().getPessoa().getNomePessoa();
        }
        return "";
    }

    public String getUsername() {
        if (usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null) {
            return usuarioInstituicao.getUsuario().getUsername();
        }
        return "";
    }

    public boolean isAutorDisponivel() {
        Pessoa pessoa = usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null
                ? usuarioInstituicao.getUsuario().getPessoa()
                : null;
        if (pessoa != null) {
            System.out.println("[DEBUG Autor] Pessoa ID: " + pessoa.getId() + " | Autor: " + pessoa.getAutor());
        }
        return usuarioInstituicao != null
                && usuarioInstituicao.getUsuario() != null
                && usuarioInstituicao.getUsuario().getPessoa() != null
                && usuarioInstituicao.getUsuario().getPessoa().getAutor() != null
                && usuarioInstituicao.getNivelAcessoUsuarioInstituicao() >= 2;
    }

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

    public boolean isPessoaDisponivel() {
        return usuarioInstituicao != null
                && usuarioInstituicao.getUsuario() != null
                && usuarioInstituicao.getUsuario().getPessoa() != null;
    }

    public String getEmailPessoa() {
        if (isPessoaDisponivel()) {
            return usuarioInstituicao.getUsuario().getPessoa().getEmailPessoa();
        }
        return "";
    }

    public String getCelularFormatado() {
        if (isPessoaDisponivel()) {
            String cel = usuarioInstituicao.getUsuario().getPessoa().getCelularPessoa();
            if (cel != null && !cel.isEmpty()) {
                // Se já estiver formatado, retorna, senão formata
                if (cel.matches(".*\\-.*"))
                    return cel;
                return com.agendademais.utils.StringUtils.formatarCelularParaExibicao(cel);
            }
        }
        return "Não informado";
    }

    public String getSubInstituicaoNome() {
        if (isPessoaDisponivel() && usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao() != null
                && !usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty()
                && usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                        .getSubInstituicao() != null) {
            return usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().get(0).getSubInstituicao()
                    .getNomeSubInstituicao();
        }
        return "---";
    }

    public String getNivelAcesso() {
        if (usuarioInstituicao != null) {
            return String.valueOf(usuarioInstituicao.getNivelAcessoUsuarioInstituicao());
        }
        return "";
    }

    public String getSituacaoAcesso() {
        if (usuarioInstituicao != null) {
            return usuarioInstituicao.getSitAcessoUsuarioInstituicao();
        }
        return "";
    }
}
