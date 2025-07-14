package com.agendademais.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agendademais.entities.RecuperacaoToken;

public interface RecuperacaoTokenRepository extends JpaRepository<RecuperacaoToken, Long> {
    Optional<RecuperacaoToken> findByToken(String token);
}
