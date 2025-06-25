package com.agendademais.config;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner loadData(
            UsuarioRepository usuarioRepo,
            PessoaRepository pessoaRepo,
            TipoAtividadeRepository tipoAtividadeRepo,
            InstituicaoRepository instituicaoRepo,
            SubInstituicaoRepository subInstituicaoRepo,
            AutorRepository autorRepo
    ) {
        return args -> {
            if (usuarioRepo.count() == 0) {
                // Insere dados apenas se não houver nenhum usuário 	
                Usuario admin = new Usuario();
                admin.setCodUsuario("admin01");
                admin.setSenha("admin123");
                admin.setNivelAcessoUsuario(1);
                usuarioRepo.save(admin);

                Usuario autor = new Usuario();
                autor.setCodUsuario("autor01");
                autor.setSenha("autor123");
                autor.setNivelAcessoUsuario(2);
                usuarioRepo.save(autor);

                Pessoa p1 = new Pessoa();
                p1.setCodUsuario(admin);
                p1.setNomePessoa("Administrador Geral");
                p1.setSituacaoPessoa("A");
                p1.setEmailPessoa("admin@agenda.com");
                p1.setCelularPessoa("+55-11-99999-0000");
                p1.setPaisPessoa("Brasil");
                p1.setEstadoEnderecoPessoa("SP");
                p1.setCidadeEnderecoPessoa("São Paulo");
                p1.setComentarios("Usuário inicial do sistema");
                p1.setDataInclusao(LocalDate.now());
                p1.setDataUltimaAtualizacao(LocalDate.now());
                p1.setCurriculoPessoal("Administrador do sistema");
                pessoaRepo.save(p1);

                Pessoa p2 = new Pessoa();
                p2.setCodUsuario(autor);
                p2.setNomePessoa("João Autor");
                p2.setSituacaoPessoa("A");
                p2.setEmailPessoa("joao@autor.com");
                p2.setCelularPessoa("+55-21-88888-1111");
                p2.setPaisPessoa("Brasil");
                p2.setEstadoEnderecoPessoa("RJ");
                p2.setCidadeEnderecoPessoa("Rio de Janeiro");
                p2.setComentarios("Autor de conteúdo");
                p2.setDataInclusao(LocalDate.now());
                p2.setDataUltimaAtualizacao(LocalDate.now());
                p2.setCurriculoPessoal("Experiência em palestras");
                pessoaRepo.save(p2);

                TipoAtividade palestra = new TipoAtividade();
                palestra.setTituloTipoAtividade("Palestra");
                palestra.setDescricaoTipoAtividade("Apresentação formal");
                tipoAtividadeRepo.save(palestra);

                TipoAtividade encontro = new TipoAtividade();
                encontro.setTituloTipoAtividade("Encontro");
                encontro.setDescricaoTipoAtividade("Reunião temática");
                tipoAtividadeRepo.save(encontro);

                Instituicao inst = new Instituicao();
                inst.setNomeInstituicao("Instituto Luz");
                inst.setSituacaoInstituicao("A");
                inst.setDataUltimaAtualizacao(LocalDate.now());
                instituicaoRepo.save(inst);

                SubInstituicao sub = new SubInstituicao();
                sub.setIdInstituicao(inst);
                sub.setNomeSubInstituicao("Núcleo SP");
                sub.setSituacaoSubInstituicao("A");
                sub.setDataUltimaAtualizacao(LocalDate.now());
                subInstituicaoRepo.save(sub);

                Autor autor1 = new Autor();
                autor1.setIdPessoa(p2);
                autor1.setFuncaoAutor(1);
                autor1.setSituacaoAutor("A");
                autor1.setCurriculoFuncaoAutor("Especialista em eventos culturais");
                autor1.setLinkImgAutor(null);
                autor1.setLinkMaterialAutor(null);
                autor1.setDataUltimaAtualizacao(LocalDate.now());
                autorRepo.save(autor1);
             	 
            	System.out.println("************************************************************************************");
                System.out.println(".../agendamais/src/main/java/com/agendamais/config/DataLoader.java:");
           	    System.out.println("*** Recarregou a basa de dados ***********");
           	    System.out.println("************************************************************************************");
             }else
             if (usuarioRepo.count() != 0) {
              	System.out.println("************************************************************************************");
                System.out.println(".../agendamais/src/main/java/com/agendamais/config/DataLoader.java:");
         	    System.out.println("*** Nao Recarregou a basa de dados *******");
         	    System.out.println("************************************************************************************");
            }
        };
    }
}
