
package com.agendademais.repositories;

import com.agendademais.entities.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
	
	Pessoa findByEmailPessoa(String emailPessoa);
}
