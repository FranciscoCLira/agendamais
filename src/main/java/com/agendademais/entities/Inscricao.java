package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;

import java.util.Set;

@Entity
@Table(
    name = "inscricao",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_pessoa", "id_instituicao"})
)
public class Inscricao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_pessoa", nullable = false)
    private Pessoa idPessoa;

    @ManyToOne
    @JoinColumn(name = "id_instituicao", nullable = false)
    private Instituicao idInstituicao;

    // ANTES:
    // @ManyToMany
    // private List<TipoAtividade> tipoAtividade;
    
    // DEPOIS:
    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InscricaoTipoAtividade> tiposAtividade = new HashSet<>();    
    
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
	public Set<InscricaoTipoAtividade> getTiposAtividade() {
		return tiposAtividade;
	}
	public void setTiposAtividade(Set<InscricaoTipoAtividade> tiposAtividade) {
		this.tiposAtividade = tiposAtividade;
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
