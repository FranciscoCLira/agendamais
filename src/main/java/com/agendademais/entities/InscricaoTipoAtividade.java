package com.agendademais.entities;

import jakarta.persistence.*;

@Entity
@Table(
    name = "inscricao_tipo_atividade",
    uniqueConstraints = @UniqueConstraint(columnNames = {"inscricao_id", "tipo_atividade_id"})
)
public class InscricaoTipoAtividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inscricao_id")
    private Inscricao inscricao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_atividade_id")
    private TipoAtividade tipoAtividade;

    // Constructors 

    public InscricaoTipoAtividade() {}

    public InscricaoTipoAtividade(Inscricao inscricao, TipoAtividade tipoAtividade) {
        this.inscricao = inscricao;
        this.tipoAtividade = tipoAtividade;
    }

    // getters/setters
    

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Inscricao getInscricao() {
		return inscricao;
	}

	public void setInscricao(Inscricao inscricao) {
		this.inscricao = inscricao;
	}

	public TipoAtividade getTipoAtividade() {
		return tipoAtividade;
	}

	public void setTipoAtividade(TipoAtividade tipoAtividade) {
		this.tipoAtividade = tipoAtividade;
	}
    
}
