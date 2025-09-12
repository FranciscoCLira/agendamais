package com.agendademais.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.agendademais.enums.FuncaoAutor;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "funcaoAutorCustomizada" })
public class Autor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private FuncaoAutor funcaoAutor;
	private String funcaoAutorOutra; // Para quando funcaoAutor = OUTRA

	@ManyToOne
	private FuncaoAutorCustomizada funcaoAutorCustomizada; // Para funções personalizadas
	private String situacaoAutor; // A = Ativo ou I = Inativo
	private String curriculoFuncaoAutor;
	private String linkImgAutor;
	private String linkMaterialAutor;
	private LocalDate dataUltimaAtualizacao;

	// Relacionamento com Pessoa
	@OneToOne
	@JoinColumn(name = "pessoa_id")
	private Pessoa pessoa;

	// GETTERS AND SETTERS

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FuncaoAutor getFuncaoAutor() {
		return funcaoAutor;
	}

	public void setFuncaoAutor(FuncaoAutor funcaoAutor) {
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

	public String getFuncaoAutorOutra() {
		return funcaoAutorOutra;
	}

	public void setFuncaoAutorOutra(String funcaoAutorOutra) {
		this.funcaoAutorOutra = funcaoAutorOutra;
	}

	public FuncaoAutorCustomizada getFuncaoAutorCustomizada() {
		return funcaoAutorCustomizada;
	}

	public void setFuncaoAutorCustomizada(FuncaoAutorCustomizada funcaoAutorCustomizada) {
		this.funcaoAutorCustomizada = funcaoAutorCustomizada;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

}
