package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class SubInstituicao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    private String nomeSubInstituicao;
    private String situacaoSubInstituicao;
    private LocalDate dataUltimaAtualizacao;

    
    // GETTERS AND SETTERS 

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public Instituicao getInstituicao() { 
		return instituicao; 
	}
	public void setInstituicao(Instituicao instituicao) { 
		this.instituicao = instituicao; 
	}
	
	public String getNomeSubInstituicao() {
		return nomeSubInstituicao;
	}
	public void setNomeSubInstituicao(String nomeSubInstituicao) {
		this.nomeSubInstituicao = nomeSubInstituicao;
	}
	public String getSituacaoSubInstituicao() {
		return situacaoSubInstituicao;
	}
	public void setSituacaoSubInstituicao(String situacaoSubInstituicao) {
		this.situacaoSubInstituicao = situacaoSubInstituicao;
	}
	public LocalDate getDataUltimaAtualizacao() {
		return dataUltimaAtualizacao;
	}
	public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
		this.dataUltimaAtualizacao = dataUltimaAtualizacao;
	}
	
}
