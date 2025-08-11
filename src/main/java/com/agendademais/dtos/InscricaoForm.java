package com.agendademais.dtos;

import java.util.ArrayList;
import java.util.List;

public class InscricaoForm {
    private List<Long> tiposAtividadeIds = new ArrayList<>();

    // getters e setters
    public List<Long> getTiposAtividadeIds() {
        return tiposAtividadeIds;
    }

    public void setTiposAtividadeIds(List<Long> tiposAtividadeIds) {
        this.tiposAtividadeIds = tiposAtividadeIds;
    }
}
