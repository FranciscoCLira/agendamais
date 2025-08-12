package com.agendademais.enums;

public enum FuncaoAutor {
    PALESTRANTE("Palestrante"),
    MEDIADOR("Mediador"),
    COORDENADOR("Coordenador"),
    AUTOR("Autor"),
    OUTRA("Outra - destacar no Currículo");

    private final String descricao;

    FuncaoAutor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
