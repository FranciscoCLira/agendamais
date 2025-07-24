package com.agendademais.dto;

import java.util.ArrayList;
import java.util.List;

public class InscricaoForm {
    private List<Long> tiposAtividadeIds = new ArrayList<>();
    private String comentarios;

    // getters e setters
    public List<Long> getTiposAtividadeIds() {
        return tiposAtividadeIds;
    }
    public void setTiposAtividadeIds(List<Long> tiposAtividadeIds) {
        this.tiposAtividadeIds = tiposAtividadeIds;
    }
    public String getComentarios() {
        return comentarios;
    }
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
}
