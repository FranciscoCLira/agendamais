package com.agendademais.dtos;

import com.agendademais.entities.Local;

public class LocalDTO {
    private Long id;
    private String nomeLocal;

    // get/set
    public LocalDTO() {}
    public LocalDTO(Local local) {
        this.id = local.getId();
        this.nomeLocal = local.getNomeLocal();
    }
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNomeLocal() {
		return nomeLocal;
	}
	public void setNomeLocal(String nomeLocal) {
		this.nomeLocal = nomeLocal;
	}
    
    
}
