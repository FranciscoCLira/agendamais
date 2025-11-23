package com.agendademais.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "pais", "estado", "cidade", "pessoaSubInstituicao" })
public class Pessoa {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nomePessoa;
	private String situacaoPessoa; // A=Ativa, B=Bloqueada, C=Cancelada

	@Column(name = "email_pessoa", unique = true, nullable = false)
	private String emailPessoa;
	private String celularPessoa;

	// Campos normalizados - referências para a tabela Local
	@ManyToOne
	@JoinColumn(name = "id_pais")
	private Local pais;

	@ManyToOne
	@JoinColumn(name = "id_estado")
	private Local estado;

	@ManyToOne
	@JoinColumn(name = "id_cidade")
	private Local cidade;

	@Column(length = 255)
	private String comentarios;
	private LocalDate dataInclusao;
	private LocalDate dataUltimaAtualizacao;
	@Column(length = 1000)
	private String curriculoPessoal;

	// Relacionamento com Sub-Instituições
	@OneToMany(mappedBy = "pessoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private java.util.List<PessoaSubInstituicao> pessoaSubInstituicao;

	// Relacionamento com Autor
	@OneToOne(mappedBy = "pessoa")
	@com.fasterxml.jackson.annotation.JsonIgnore
	private Autor autor;

	public Autor getAutor() {
		return autor;
	}

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

	// Getters e Setters para campos normalizados
	public Local getPais() {
		return pais;
	}

	public void setPais(Local pais) {
		this.pais = pais;
	}

	public Local getEstado() {
		return estado;
	}

	public void setEstado(Local estado) {
		this.estado = estado;
	}

	public Local getCidade() {
		return cidade;
	}

	public void setCidade(Local cidade) {
		this.cidade = cidade;
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

	public java.util.List<PessoaSubInstituicao> getPessoaSubInstituicao() {
		return pessoaSubInstituicao;
	}

	public void setPessoaSubInstituicao(java.util.List<PessoaSubInstituicao> pessoaSubInstituicao) {
		this.pessoaSubInstituicao = pessoaSubInstituicao;
	}

	// --- Métodos auxiliares para obter nomes dos locais ---

	/**
	 * Retorna o nome do país
	 */
	public String getNomePais() {
		return pais != null ? pais.getNomeLocal() : null;
	}

	/**
	 * Retorna o nome do estado
	 */
	public String getNomeEstado() {
		return estado != null ? estado.getNomeLocal() : null;
	}

	/**
	 * Retorna o nome da cidade
	 */
	public String getNomeCidade() {
		return cidade != null ? cidade.getNomeLocal() : null;
	}

}
