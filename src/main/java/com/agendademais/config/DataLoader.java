package com.agendademais.config;

import com.agendademais.entities.*;
import com.agendademais.repositories.InstituicaoRepository;
import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.UsuarioInstituicaoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;
    
    @Value("${app.reload-data:false}")
    private boolean reloadData;

    public DataLoader(InstituicaoRepository instituicaoRepository,
                      UsuarioRepository usuarioRepository,
                      PessoaRepository pessoaRepository,
                      UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.instituicaoRepository = instituicaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
     // if (usuarioRepository.count() > 0 || instituicaoRepository.count() > 0) {

    	if (!reloadData || usuarioRepository.count() > 0 || instituicaoRepository.count() > 0) {
        	System.out.println("****************************************************************************");
            System.out.println("****** /config/DataLoader.java - Não recarregou a base de dados       ******");
        	System.out.println("****** spring.jpa.hibernate.ddl-auto=update ==> reloadData=" + reloadData);
        	System.out.println("****************************************************************************");
            return; // Já existem dados, não recarregar
        }

    	System.out.println("****************************************************************************");
        System.out.println("****** /config/DataLoader.java - Recarregou a base de dados           ******");
    	System.out.println("****** spring.jpa.hibernate.ddl-auto=create ==> reloadData=" + reloadData);
    	System.out.println("****************************************************************************");

        Instituicao inst1 = new Instituicao();
        inst1.setNomeInstituicao("Instituto Aurora");
        inst1.setSituacaoInstituicao("A");
        inst1.setDataUltimaAtualizacao(LocalDate.now());

        Instituicao inst2 = new Instituicao();
        inst2.setNomeInstituicao("Centro Harmonia");
        inst2.setSituacaoInstituicao("A");
        inst2.setDataUltimaAtualizacao(LocalDate.now());

        instituicaoRepository.saveAll(Arrays.asList(inst1, inst2));

        Usuario user1 = new Usuario();
        user1.setCodUsuario("admin01");
        user1.setSenha("admin123");
        user1.setNivelAcessoUsuario(1); // Participante
        usuarioRepository.save(user1);

        Pessoa pessoa1 = new Pessoa();
        pessoa1.setCodUsuario(user1);
        pessoa1.setNomePessoa("Carlos Silva");
        pessoa1.setSituacaoPessoa("A");
        pessoa1.setEmailPessoa("carlos@email.com");
        pessoa1.setDataInclusao(LocalDate.now());
        pessoa1.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa1);

        UsuarioInstituicao ui1 = new UsuarioInstituicao();
        ui1.setUsuario(user1);
        ui1.setInstituicao(inst1);
        ui1.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui1);
    }
}
