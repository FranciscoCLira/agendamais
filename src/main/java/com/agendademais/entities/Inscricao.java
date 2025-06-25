package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Inscricao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pessoa idPessoa;

    @ManyToOne
    private Instituicao idInstituicao;

    @ManyToMany
    private List<TipoAtividade> tipoAtividade;

    private String identificacaoInstituicao;
    private String identificacaoSubInstituicao;
    private String comentarios;
    private LocalDate dataInclusao;
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
	public List<TipoAtividade> getTipoAtividade() {
		return tipoAtividade;
	}
	public void setTipoAtividade(List<TipoAtividade> tipoAtividade) {
		this.tipoAtividade = tipoAtividade;
	}
	public String getIdentificacaoInstituicao() {
		return identificacaoInstituicao;
	}
	public void setIdentificacaoInstituicao(String identificacaoInstituicao) {
		this.identificacaoInstituicao = identificacaoInstituicao;
	}
	public String getIdentificacaoSubInstituicao() {
		return identificacaoSubInstituicao;
	}
	public void setIdentificacaoSubInstituicao(String identificacaoSubInstituicao) {
		this.identificacaoSubInstituicao = identificacaoSubInstituicao;
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

}
