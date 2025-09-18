package com.agendademais.forms;

public class PostagemForm {
    private Long ocorrenciaAtividade;
    private String titulo;
    private String conteudo;

    public Long getOcorrenciaAtividade() {
        return ocorrenciaAtividade;
    }

    public void setOcorrenciaAtividade(Long ocorrenciaAtividade) {
        this.ocorrenciaAtividade = ocorrenciaAtividade;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
