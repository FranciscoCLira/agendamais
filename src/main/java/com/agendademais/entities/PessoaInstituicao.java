package com.agendademais.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
public class PessoaInstituicao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Column(length = 20)
    private String identificacaoPessoaInstituicao;

    private LocalDate dataAfiliacao;

    private LocalDate dataUltimaAtualizacao;

    
    // GETTERS AND SETTERS 

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getIdentificacaoPessoaInstituicao() {
		return identificacaoPessoaInstituicao;
	}
	public void setIdentificacaoPessoaInstituicao(String identificacaoPessoaInstituicao) {
		this.identificacaoPessoaInstituicao = identificacaoPessoaInstituicao;
	}
	public LocalDate getDataAfiliacao() {
		return dataAfiliacao;
	}
	public void setDataAfiliacao(LocalDate dataAfiliacao) {
		this.dataAfiliacao = dataAfiliacao;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	
	public Pessoa getPessoa() {
		return pessoa;
	}
	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	public Instituicao getInstituicao() {
		return instituicao;
	}
	public void setInstituicao(Instituicao instituicao) {
		this.instituicao = instituicao;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
