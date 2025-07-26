package com.agendademais.dtos;

public class PessoaForm {
    private String nomePessoa;
    private String emailPessoa;
    private String celularPessoa;
    
    private String nomePaisSelect;     // select no form
    private String paisOutro;          // input texto
    
    private String nomeEstadoSelect;   // select no form
    private String estadoOutro;        // input texto
    
    private String nomeCidadeSelect;   // select no form
    private String cidadeOutro;        // input texto

    private String curriculoPessoal;
    private String comentarios;
    
    // ... demais campos necessários ...

    
    // GETTERS e SETTERS 
    
	public String getNomePessoa() {
		return nomePessoa;
	}
	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
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
	public String getNomePaisSelect() {
		return nomePaisSelect;
	}
	public void setNomePaisSelect(String nomePaisSelect) {
		this.nomePaisSelect = nomePaisSelect;
	}
	public String getPaisOutro() {
		return paisOutro;
	}
	public void setPaisOutro(String paisOutro) {
		this.paisOutro = paisOutro;
	}
	public String getNomeEstadoSelect() {
		return nomeEstadoSelect;
	}
	public void setNomeEstadoSelect(String nomeEstadoSelect) {
		this.nomeEstadoSelect = nomeEstadoSelect;
	}
	public String getEstadoOutro() {
		return estadoOutro;
	}
	public void setEstadoOutro(String estadoOutro) {
		this.estadoOutro = estadoOutro;
	}
	
	public String getNomeCidadeSelect() {
		return nomeCidadeSelect;
	}
	public void setNomeCidadeSelect(String nomeCidadeSelect) {
		this.nomeCidadeSelect = nomeCidadeSelect;
	}
	public String getCidadeOutro() {
		return cidadeOutro;
	}
	public void setCidadeOutro(String cidadeOutro) {
		this.cidadeOutro = cidadeOutro;
	}
	public String getCurriculoPessoal() {
		return curriculoPessoal;
	}
	public void setCurriculoPessoal(String curriculoPessoal) {
		this.curriculoPessoal = curriculoPessoal;
	}
	public String getComentarios() {
		return comentarios;
	}
	public void setComentarios(String comentarios) {
		this.comentarios = comentarios;
	}
}
