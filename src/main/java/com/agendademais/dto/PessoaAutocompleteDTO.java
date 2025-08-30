package com.agendademais.dto;

import com.agendademais.entities.Pessoa;

public class PessoaAutocompleteDTO {
    private Long id;
    private String nome;
    private String email;

    public PessoaAutocompleteDTO(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public static PessoaAutocompleteDTO fromEntity(Pessoa pessoa) {
        return new PessoaAutocompleteDTO(
                pessoa.getId(),
                pessoa.getNomePessoa(),
                pessoa.getEmailPessoa());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}
