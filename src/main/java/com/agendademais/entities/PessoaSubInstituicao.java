package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PessoaSubInstituicao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pessoa idPessoa;

    @ManyToOne
    private Instituicao idInstituicao;

    @ManyToOne
    private SubInstituicao idSubInstituicao;

    private String identificacaoPessoaSubInstituicao;
    private LocalDate dataAssociacao;
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
	public Instituicao getIdInstituicao() {
		return idInstituicao;
	}
	public void setIdInstituicao(Instituicao idInstituicao) {
		this.idInstituicao = idInstituicao;
	}
	public SubInstituicao getIdSubInstituicao() {
		return idSubInstituicao;
	}
	public void setIdSubInstituicao(SubInstituicao idSubInstituicao) {
		this.idSubInstituicao = idSubInstituicao;
	}
	public String getIdentificacaoPessoaSubInstituicao() {
		return identificacaoPessoaSubInstituicao;
	}
	public void setIdentificacaoPessoaSubInstituicao(String identificacaoPessoaSubInstituicao) {
		this.identificacaoPessoaSubInstituicao = identificacaoPessoaSubInstituicao;
	}
	public LocalDate getDataAssociacao() {
		return dataAssociacao;
	}
	public void setDataAssociacao(LocalDate dataAssociacao) {
		this.dataAssociacao = dataAssociacao;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
}
