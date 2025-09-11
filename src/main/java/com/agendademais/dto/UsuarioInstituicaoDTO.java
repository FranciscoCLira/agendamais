
package com.agendademais.dto;

import com.agendademais.entities.UsuarioInstituicao;
// import com.agendademais.entities.Pessoa;

public class UsuarioInstituicaoDTO {
    // Campo público para teste de binding Thymeleaf
    public String username;
    private Long subInstituicaoId;
    private String identificacaoPessoaSubInstituicao;

    public String getUsername() {
        return username != null ? username : "";
    }

    public String getLocalizacaoFormatada() {
        try {
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
        } catch (Exception e) {
            System.err.println("[DTO DEBUG] getLocalizacaoFormatada exception: " + e.getMessage());
        }
        return "--- / --- / ---";
    }

    public UsuarioInstituicaoDTO(UsuarioInstituicao usuarioInstituicao, String nomeSubInstituicao) {
        // if (usuarioInstituicao == null) {
        // System.err.println("[DTO DEBUG] usuarioInstituicao is null when constructing
        // DTO");
        // }
        this.usuarioInstituicao = usuarioInstituicao;
        this.nomeSubInstituicao = nomeSubInstituicao;
        // Preencher o campo público username para teste de binding
        try {
            if (usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null) {
                this.username = usuarioInstituicao.getUsuario().getUsername();
                // Preencher subInstituicaoId e identificacaoPessoaSubInstituicao
                try {
                    var pessoa = usuarioInstituicao.getUsuario().getPessoa();
                    if (pessoa != null && pessoa.getPessoaSubInstituicao() != null
                            && !pessoa.getPessoaSubInstituicao().isEmpty()) {
                        var psi = pessoa.getPessoaSubInstituicao().get(0);
                        if (psi != null && psi.getSubInstituicao() != null) {
                            this.subInstituicaoId = psi.getSubInstituicao().getId();
                        }
                        this.identificacaoPessoaSubInstituicao = psi.getIdentificacaoPessoaSubInstituicao();
                    }
                } catch (Exception e) {
                    this.subInstituicaoId = null;
                    this.identificacaoPessoaSubInstituicao = null;
                }
            } else {
                this.username = null;
                this.subInstituicaoId = null;
                this.identificacaoPessoaSubInstituicao = null;
            }
        } catch (Exception e) {
            this.username = null;
            this.subInstituicaoId = null;
            this.identificacaoPessoaSubInstituicao = null;
        }
    }

    public String getNomePessoa() {
        try {
            if (usuarioInstituicao != null && usuarioInstituicao.getUsuario() != null
                    && usuarioInstituicao.getUsuario().getPessoa() != null) {
                return usuarioInstituicao.getUsuario().getPessoa().getNomePessoa();
            }
        } catch (Exception e) {
            System.err.println("[DTO DEBUG] getNomePessoa exception: " + e.getMessage());
        }
        return "";
    }

    public boolean isAutorDisponivel() {
        // Pessoa pessoa = usuarioInstituicao != null && usuarioInstituicao.getUsuario()
        // != null
        // ? usuarioInstituicao.getUsuario().getPessoa()
        // : null;
        // if (pessoa != null) {
        // System.out.println("[DEBUG Autor] Pessoa ID: " + pessoa.getId() + " | Autor:
        // " + pessoa.getAutor());
        // }
        return usuarioInstituicao != null
                && usuarioInstituicao.getUsuario() != null
                && usuarioInstituicao.getUsuario().getPessoa() != null
                && usuarioInstituicao.getUsuario().getPessoa().getAutor() != null
                && usuarioInstituicao.getNivelAcessoUsuarioInstituicao() >= 2;
    }

    private UsuarioInstituicao usuarioInstituicao;
    private String nomeSubInstituicao;

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
        try {
            if (isPessoaDisponivel()) {
                return usuarioInstituicao.getUsuario().getPessoa().getEmailPessoa();
            }
        } catch (Exception e) {
            System.err.println("[DTO DEBUG] getEmailPessoa exception: " + e.getMessage());
        }
        return "";
    }

    public String getCelularFormatado() {
        try {
            if (isPessoaDisponivel()) {
                String cel = usuarioInstituicao.getUsuario().getPessoa().getCelularPessoa();
                if (cel != null && !cel.isEmpty()) {
                    // Se já estiver formatado, retorna, senão formata
                    if (cel.matches(".*-.*"))
                        return cel;
                    return com.agendademais.utils.StringUtils.formatarCelularParaExibicao(cel);
                }
            }
        } catch (Exception e) {
            System.err.println("[DTO DEBUG] getCelularFormatado exception: " + e.getMessage());
        }
        return "Não informado";
    }

    public String getSubInstituicaoNome() {
        try {
            if (isPessoaDisponivel() && usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao() != null
                    && !usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().isEmpty()
                    && usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().get(0)
                            .getSubInstituicao() != null) {
                return usuarioInstituicao.getUsuario().getPessoa().getPessoaSubInstituicao().get(0).getSubInstituicao()
                        .getNomeSubInstituicao();
            }
        } catch (Exception e) {
            System.err.println("[DTO DEBUG] getSubInstituicaoNome exception: " + e.getMessage());
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

    // TESTE METODO PUBLICO ESTATICO
    public static String testStatic() {
        return "STATIC_OK";
    }
}
