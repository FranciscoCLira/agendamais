package com.agendademais.repositories;

import com.agendademais.entities.FuncaoAutorCustomizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FuncaoAutorCustomizadaRepository extends JpaRepository<FuncaoAutorCustomizada, Long> {
    List<FuncaoAutorCustomizada> findByAtivaTrue();
    Optional<FuncaoAutorCustomizada> findByNomeFuncao(String nomeFuncao);
    boolean existsByNomeFuncao(String nomeFuncao);
}
