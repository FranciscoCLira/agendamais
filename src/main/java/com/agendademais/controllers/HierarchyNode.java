package com.agendademais.controllers;

import com.agendademais.entities.Local;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe auxiliar para representar a estrutura hierárquica de locais
 */
public class HierarchyNode {
    private Local pais;
    private Local estado;
    private List<Local> cidades;
    private List<HierarchyNode> estados;

    public HierarchyNode(Local local) {
        if (local.getTipoLocal() == 1) {
            // País
            this.pais = local;
            this.estados = new ArrayList<>();
        } else if (local.getTipoLocal() == 2) {
            // Estado
            this.estado = local;
            this.cidades = new ArrayList<>();
        }
    }

    // Getters e Setters
    public Local getPais() {
        return pais;
    }

    public void setPais(Local pais) {
        this.pais = pais;
    }

    public Local getEstado() {
        return estado;
    }

    public void setEstado(Local estado) {
        this.estado = estado;
    }

    public List<Local> getCidades() {
        return cidades;
    }

    public void setCidades(List<Local> cidades) {
        this.cidades = cidades;
    }

    public List<HierarchyNode> getEstados() {
        return estados;
    }

    public void setEstados(List<HierarchyNode> estados) {
        this.estados = estados;
    }
}
