package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Autor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pessoa idPessoa;

    private Integer funcaoAutor;
    private String situacaoAutor;
    private String curriculoFuncaoAutor;
    private String linkImgAutor;
    private String linkMaterialAutor;
    private LocalDate dataUltimaAtualizacao;

    
    // GETTERS AND SETTERS 

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Pessoa getIdPessoa() {
		return idPessoa;
	}
	public void setIdPessoa(Pessoa idPessoa) {
		this.idPessoa = idPessoa;
	}
	public Integer getFuncaoAutor() {
		return funcaoAutor;
	}
	public void setFuncaoAutor(Integer funcaoAutor) {
		this.funcaoAutor = funcaoAutor;
	}
	public String getSituacaoAutor() {
		return situacaoAutor;
	}
	public void setSituacaoAutor(String situacaoAutor) {
		this.situacaoAutor = situacaoAutor;
	}
	public String getCurriculoFuncaoAutor() {
		return curriculoFuncaoAutor;
	}
	public void setCurriculoFuncaoAutor(String curriculoFuncaoAutor) {
		this.curriculoFuncaoAutor = curriculoFuncaoAutor;
	}
	public String getLinkImgAutor() {
		return linkImgAutor;
	}
	public void setLinkImgAutor(String linkImgAutor) {
		this.linkImgAutor = linkImgAutor;
	}
	public String getLinkMaterialAutor() {
		return linkMaterialAutor;
	}
	public void setLinkMaterialAutor(String linkMaterialAutor) {
		this.linkMaterialAutor = linkMaterialAutor;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}


}
