package com.agendademais.config;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Value("${app.reload-data:false}")
    private boolean reloadData;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;
    
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
        
    	System.out.println("****************************************************************************");
    	System.out.println("****** spring.jpa.hibernate.ddl-auto=" + ddlAuto +  "                         ********");
    	System.out.println("****** app.reload-data=" + reloadData +  "                                        ********");
    	
    	if (!reloadData) { 
            System.out.println("****** /config/DataLoader.java - Não recarregou a base de dados     ********");
            System.out.println("****************************************************************************");
            return;
        }

        if  (usuarioRepository.count() > 0 || instituicaoRepository.count() > 0) {
            System.out.println("****** /config/DataLoader.java - Não recarregou a base de dados     ********");
            System.out.println("****************************************************************************");
            return;
        }

        System.out.println("****** /config/DataLoader.java - Recarregando a base de dados.....   *******");
        System.out.println("****************************************************************************");

        Instituicao inst1 = new Instituicao();
        inst1.setNomeInstituicao("Instituto Aurora");
        inst1.setSituacaoInstituicao("A");
        inst1.setDataUltimaAtualizacao(LocalDate.now());

        instituicaoRepository.save(inst1);

        Pessoa pessoa1 = new Pessoa();
        pessoa1.setNomePessoa("Carlos Silva");
        pessoa1.setSituacaoPessoa("A");
        pessoa1.setEmailPessoa("carlos@email.com");
        pessoa1.setDataInclusao(LocalDate.now());
        pessoa1.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa1);

        Usuario user1 = new Usuario();
        user1.setCodUsuario("admin01");
        user1.setSenha("admin123");
        user1.setNivelAcessoUsuario(5);
        user1.setPessoa(pessoa1);
        usuarioRepository.save(user1);

        UsuarioInstituicao ui1 = new UsuarioInstituicao();
        ui1.setUsuario(user1);
        ui1.setInstituicao(inst1);
        ui1.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui1);

        System.out.println("****** Recarregou a base de dados     **************************************");
        System.out.println("****************************************************************************");
    }
}
