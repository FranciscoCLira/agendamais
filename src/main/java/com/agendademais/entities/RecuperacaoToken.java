package com.agendademais.entities;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
public class RecuperacaoToken {
	
    @Id @GeneratedValue
    private Long id;

    private String token;
    private String email;
    private LocalDate dataExpiracao;
    

    // getters/setters
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public LocalDate getDataExpiracao() {
		return dataExpiracao;
	}
	public void setDataExpiracao(LocalDate dataExpiracao) {
		this.dataExpiracao = dataExpiracao;
	}
}
