package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Pessoa {
	@Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
	private String nomePessoa;
    private String situacaoPessoa;
    private String emailPessoa;
    private String celularPessoa;
    private String nomePaisPessoa;
    private String NomeEstadoPessoa;
    private String NomeCidadePessoa;
    private String comentarios;
    private LocalDate dataInclusao;
    private LocalDate dataUltimaAtualizacao;
    private String curriculoPessoal;
    
    // GETTERS AND SETTERS 
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getNomePessoa() {
		return nomePessoa;
	}
	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}
	public String getSituacaoPessoa() {
		return situacaoPessoa;
	}
	public void setSituacaoPessoa(String situacaoPessoa) {
		this.situacaoPessoa = situacaoPessoa;
	}
	public String getEmailPessoa() {
		return emailPessoa;
	}
	public void setEmailPessoa(String emailPessoa) {
		this.emailPessoa = emailPessoa;
	}
	public String getCelularPessoa() {
		return celularPessoa;
	}
	public void setCelularPessoa(String celularPessoa) {
		this.celularPessoa = celularPessoa;
	}
	public String getNomePaisPessoa() {
		return nomePaisPessoa;
	}
	public void setNomePaisPessoa(String nomePaisPessoa) {
		this.nomePaisPessoa = nomePaisPessoa;
	}
	public String getNomeEstadoPessoa() {
		return NomeEstadoPessoa;
	}
	public void setNomeEstadoPessoa(String NomeEstadoPessoa) {
		this.NomeEstadoPessoa = NomeEstadoPessoa;
	}
	public String getNomeCidadePessoa() {
		return NomeCidadePessoa;
	}
	public void setNomeCidadePessoa(String NomeCidadePessoa) {
		this.NomeCidadePessoa = NomeCidadePessoa;
	}
	public String getComentarios() {
		return comentarios;
	}
	public void setComentarios(String comentarios) {
		this.comentarios = comentarios;
	}
	public LocalDate getDataInclusao() {
		return dataInclusao;
	}
	public void setDataInclusao(LocalDate dataInclusao) {
		this.dataInclusao = dataInclusao;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	public String getCurriculoPessoal() {
		return curriculoPessoal;
	}
	public void setCurriculoPessoal(String curriculoPessoal) {
		this.curriculoPessoal = curriculoPessoal;
	}
    
}

