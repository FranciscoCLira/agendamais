package com.agendademais.repositories;

import com.agendademais.entities.Instituicao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

	// Object findBySituacaoInstituicao(String situacao);
	List<Instituicao> findBySituacaoInstituicao(String situacao);

	// Busca a instituição do admin logado pelo username
	@Query("SELECT ui.instituicao FROM UsuarioInstituicao ui WHERE ui.usuario.username = :username AND ui.nivelAcessoUsuarioInstituicao = 5")
	Instituicao findByAdminUsername(@Param("username") String username);
}
