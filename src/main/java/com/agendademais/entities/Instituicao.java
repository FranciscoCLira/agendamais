package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Instituicao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeInstituicao;
    private String situacaoInstituicao;
    private LocalDate dataUltimaAtualizacao;
    
    // GETTERS AND SETTERS
        
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNomeInstituicao() {
		return nomeInstituicao;
	}
	public void setNomeInstituicao(String nomeInstituicao) {
		this.nomeInstituicao = nomeInstituicao;
	}
	public String getSituacaoInstituicao() {
		return situacaoInstituicao;
	}
	public void setSituacaoInstituicao(String situacaoInstituicao) {
		this.situacaoInstituicao = situacaoInstituicao;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	
	
	@Override
	public String toString() {
		return "Instituicao [id=" + id + ", nomeInstituicao=" + nomeInstituicao + ", situacaoInstituicao="
				+ situacaoInstituicao + ", dataUltimaAtualizacao=" + dataUltimaAtualizacao + "]";
	}
	
	
}
