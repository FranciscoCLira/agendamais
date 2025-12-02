package com.agendademais.repositories;

import com.agendademais.entities.ConfiguracaoSmtpGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para gerenciamento de configurações SMTP globais
 */
@Repository
public interface ConfiguracaoSmtpGlobalRepository extends JpaRepository<ConfiguracaoSmtpGlobal, Long> {

    /**
     * Busca a configuração SMTP ativa mais recente
     * Retorna Optional vazio se não houver configuração ativa
     */
    Optional<ConfiguracaoSmtpGlobal> findFirstByAtivoTrueOrderByDataCriacaoDesc();

    /**
     * Verifica se existe alguma configuração ativa
     */
    boolean existsByAtivoTrue();
}
