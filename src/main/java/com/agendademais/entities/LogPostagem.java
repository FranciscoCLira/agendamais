package com.agendademais.entities;

import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
public class LogPostagem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OcorrenciaAtividade idOcorrenciaAtividade;

    private LocalDate dataPostagem;
    private java.time.LocalTime horaPostagem;
	public Long getId() {
		return id;
	}
	
	
	// GETTERS AND SETTERS 
	
	public void setId(Long id) {
		this.id = id;
	}
	public OcorrenciaAtividade getIdOcorrenciaAtividade() {
		return idOcorrenciaAtividade;
	}
	public void setIdOcorrenciaAtividade(OcorrenciaAtividade idOcorrenciaAtividade) {
		this.idOcorrenciaAtividade = idOcorrenciaAtividade;
	}
	public LocalDate getDataPostagem() {
		return dataPostagem;
	}
	public void setDataPostagem(LocalDate dataPostagem) {
		this.dataPostagem = dataPostagem;
	}
	public java.time.LocalTime getHoraPostagem() {
		return horaPostagem;
	}
	public void setHoraPostagem(java.time.LocalTime horaPostagem) {
		this.horaPostagem = horaPostagem;
	}
    
    
}