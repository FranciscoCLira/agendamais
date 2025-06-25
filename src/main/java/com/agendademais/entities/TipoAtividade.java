package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
public class TipoAtividade {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tituloTipoAtividade;
    private String descricaoTipoAtividade;
    
    // GETTERS AND SETTERS
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTituloTipoAtividade() {
		return tituloTipoAtividade;
	}
	public void setTituloTipoAtividade(String tituloTipoAtividade) {
		this.tituloTipoAtividade = tituloTipoAtividade;
	}
	public String getDescricaoTipoAtividade() {
		return descricaoTipoAtividade;
	}
	public void setDescricaoTipoAtividade(String descricaoTipoAtividade) {
		this.descricaoTipoAtividade = descricaoTipoAtividade;
	}
}
