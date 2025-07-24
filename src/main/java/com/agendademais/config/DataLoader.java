package com.agendademais.config;

import com.agendademais.entities.*;
import com.agendademais.repositories.*;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final InstituicaoRepository instituicaoRepository;
    private final SubInstituicaoRepository subInstituicaoRepository;
    private final TipoAtividadeRepository tipoAtividadeRepository; 
    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Value("${app.reload-data:false}")
    private boolean reloadData;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;
    
    public DataLoader(InstituicaoRepository instituicaoRepository,
    	           	  SubInstituicaoRepository subInstituicaoRepository,
    	      	      TipoAtividadeRepository tipoAtividadeRepository, 
                      UsuarioRepository usuarioRepository,
                      PessoaRepository pessoaRepository,
                      UsuarioInstituicaoRepository usuarioInstituicaoRepository) {
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
        this.tipoAtividadeRepository = tipoAtividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        
    	System.out.println("****************************************************************************");
    	System.out.println("****** spring.jpa.hibernate.ddl-auto=" + ddlAuto +  "                         ********");
    	System.out.println("****** app.reload-data=" + reloadData +  "                                         ********");
    	
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

        Instituicao inst2 = new Instituicao();
        inst2.setNomeInstituicao("Instituto Luz");
        inst2.setSituacaoInstituicao("A");
        inst2.setDataUltimaAtualizacao(LocalDate.now());
        instituicaoRepository.save(inst2);

        Instituicao inst3 = new Instituicao();
        inst3.setNomeInstituicao("Instituto Cruz");
        inst3.setSituacaoInstituicao("A");
        inst3.setDataUltimaAtualizacao(LocalDate.now());
        instituicaoRepository.save(inst3);
        
        // SUBINSTITUICOES DA INSTITUICAO 1 
        
        SubInstituicao subInst11 = new SubInstituicao();
        subInst11.setInstituicao(inst1);
        subInst11.setNomeSubInstituicao("subInst1 1 Aurora 1");
        subInst11.setSituacaoSubInstituicao("A");
        subInst11.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst11);
        
        SubInstituicao subInst12 = new SubInstituicao();
        subInst12.setInstituicao(inst1);
        subInst12.setNomeSubInstituicao("subInst1 2 Aurora 1");
        subInst12.setSituacaoSubInstituicao("A");
        subInst12.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst12);

        SubInstituicao subInst13 = new SubInstituicao();
        subInst13.setInstituicao(inst1);
        subInst13.setNomeSubInstituicao("subInst1 3 Aurora 1");
        subInst13.setSituacaoSubInstituicao("A");
        subInst13.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst13);        
        
        // SUBINSTITUICOES DA INSTITUICAO 2 

        SubInstituicao subInst21 = new SubInstituicao();
        subInst21.setInstituicao(inst2);
        subInst21.setNomeSubInstituicao("subInst2 1 Luz 2");
        subInst21.setSituacaoSubInstituicao("A");
        subInst21.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst21);
        
        SubInstituicao subInst22 = new SubInstituicao();
        subInst22.setInstituicao(inst2);
        subInst22.setNomeSubInstituicao("subInst2 2 Luz 2");
        subInst22.setSituacaoSubInstituicao("A");
        subInst22.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst22);

        SubInstituicao subInst23 = new SubInstituicao();
        subInst23.setInstituicao(inst2);
        subInst23.setNomeSubInstituicao("subInst2 3 Luz 2");
        subInst23.setSituacaoSubInstituicao("A");
        subInst23.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst23);        
        
        // SUBINSTITUICOES DA INSTITUICAO 3 
        
        SubInstituicao subInst31 = new SubInstituicao();
        subInst31.setInstituicao(inst3);
        subInst31.setNomeSubInstituicao("subInst3 1 Cruz 3");
        subInst31.setSituacaoSubInstituicao("A");
        subInst31.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst31);
        
        SubInstituicao subInst32 = new SubInstituicao();
        subInst32.setInstituicao(inst3);
        subInst32.setNomeSubInstituicao("subInst3 2 Cruz 3");
        subInst32.setSituacaoSubInstituicao("A");
        subInst32.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst32);

        SubInstituicao subInst33 = new SubInstituicao();
        subInst33.setInstituicao(inst3);
        subInst33.setNomeSubInstituicao("subInst3 3 Cruz 3");
        subInst33.setSituacaoSubInstituicao("A");
        subInst33.setDataUltimaAtualizacao(LocalDate.now());
        subInstituicaoRepository.save(subInst33);        
        
        
        // TIPO_ATIVIDADE - INSTITUICAO 1 
        
        TipoAtividade tpativ1 = new TipoAtividade();
        tpativ1.setTituloTipoAtividade("Palestra Inst 1");
        tpativ1.setDescricaoTipoAtividade("As palestras devem ter duração aproximada de 1 hora");
        tpativ1.setInstituicao(inst1);
        tipoAtividadeRepository.save(tpativ1);        
        TipoAtividade tpativ2 = new TipoAtividade();
        tpativ2.setTituloTipoAtividade("Reuniao Inst 1 ");
        tpativ2.setDescricaoTipoAtividade("Reuniões semanais");
        tpativ2.setInstituicao(inst1);
        tipoAtividadeRepository.save(tpativ2);        
        TipoAtividade tpativ3 = new TipoAtividade();
        tpativ3.setTituloTipoAtividade("Passeio Turistico Inst 1");
        tpativ3.setDescricaoTipoAtividade("Passeios ao ar livre em instancias turisticas");
        tpativ3.setInstituicao(inst1);
        tipoAtividadeRepository.save(tpativ3);        

        // TIPO_ATIVIDADE - INSTITUICAO 2 
        
        TipoAtividade tpativ21 = new TipoAtividade();
        tpativ21.setTituloTipoAtividade("Palestra Inst 2");
        tpativ21.setDescricaoTipoAtividade("As palestras devem ter duração aproximada de 2 horas");
        tpativ21.setInstituicao(inst2);
        tipoAtividadeRepository.save(tpativ21);        
        TipoAtividade tpativ22 = new TipoAtividade();
        tpativ22.setTituloTipoAtividade("Reuniao Inst 2 ");
        tpativ22.setDescricaoTipoAtividade("Reuniões mensais");
        tpativ22.setInstituicao(inst2);
        tipoAtividadeRepository.save(tpativ22);        
        TipoAtividade tpativ23 = new TipoAtividade();
        tpativ23.setTituloTipoAtividade("Passeio Turistico Inst 2");
        tpativ23.setDescricaoTipoAtividade("Passeios ao ar livre em instancias turisticas");
        tpativ23.setInstituicao(inst2);
        tipoAtividadeRepository.save(tpativ23);        

        // TIPO_ATIVIDADE - INSTITUICAO 3 

        TipoAtividade tpativ31 = new TipoAtividade();
        tpativ31.setTituloTipoAtividade("Palestra Inst 3");
        tpativ31.setDescricaoTipoAtividade("As palestras devem ter duração aproximada de 3 horas");
        tpativ31.setInstituicao(inst3);
        tipoAtividadeRepository.save(tpativ31);        
        TipoAtividade tpativ32 = new TipoAtividade();
        tpativ32.setTituloTipoAtividade("Reuniao Inst 3 ");
        tpativ32.setDescricaoTipoAtividade("Reuniões trimestrais");
        tpativ32.setInstituicao(inst3);
        tipoAtividadeRepository.save(tpativ32);        
        TipoAtividade tpativ33 = new TipoAtividade();
        tpativ33.setTituloTipoAtividade("Passeio Turistico Inst 3");
        tpativ33.setDescricaoTipoAtividade("Passeios ao ar livre em instancias turisticas");
        tpativ33.setInstituicao(inst3);
        tipoAtividadeRepository.save(tpativ33);        
        

        // PESSOA 1: PARTICIPANTE - NIVEL 1

        Pessoa pessoa1 = new Pessoa();
        pessoa1.setNomePessoa("Carlos Silva");
        pessoa1.setEmailPessoa("carlos@email.com");
        pessoa1.setCelularPessoa("+55-11-99999-9999");
        pessoa1.setNomePaisPessoa("Brasil");
        pessoa1.setNomeEstadoPessoa("RJ");
        pessoa1.setNomeCidadePessoa("Rio de Janeiro");
        pessoa1.setCurriculoPessoal("Em preparação");
        pessoa1.setComentarios("Comentarios ainda a serem acrescentados");
        pessoa1.setSituacaoPessoa("A");
        pessoa1.setDataInclusao(LocalDate.now());
        pessoa1.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa1);
        
        // Após ter salvo pessoa1 acima,
        // recupera-a do banco antes de associar ao usuário:
        Pessoa pessoaPersistida1 = pessoaRepository.findById(pessoa1.getId()).orElseThrow();

        Usuario user1 = new Usuario();
        user1.setCodUsuario("partic");
        user1.setSenha("partic");
        user1.setNivelAcessoUsuario(1);
        user1.setSituacaoUsuario("A");
        user1.setDataUltimaAtualizacao(LocalDate.now());
        user1.setPessoa(pessoaPersistida1);
        usuarioRepository.save(user1);

        UsuarioInstituicao ui1 = new UsuarioInstituicao();
        ui1.setUsuario(user1);
        ui1.setInstituicao(inst1);
        ui1.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui1);
        
        // PESSOA 2: ADMINISTRADOR - NIVEL 5 
        
        Pessoa pessoa2 = new Pessoa();
        pessoa2.setNomePessoa("João de Souza");
        pessoa2.setEmailPessoa("joao@email.com");
        pessoa2.setCelularPessoa("+55-11-99999-9999");
        pessoa2.setNomePaisPessoa("Brasil");
        pessoa2.setNomeEstadoPessoa("SP");
        pessoa2.setNomeCidadePessoa("São Bernardo do Campo");
        pessoa2.setCurriculoPessoal("Em confecção");
        pessoa2.setComentarios("Sem comentarios por enquanto");
        pessoa2.setSituacaoPessoa("A");
        pessoa2.setDataInclusao(LocalDate.now());
        pessoa2.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa2);
        Pessoa pessoaPersistida2 = pessoaRepository.findById(pessoa2.getId()).orElseThrow();

        Usuario user2 = new Usuario();
        user2.setCodUsuario("admin1");
        user2.setSenha("admin1");
        user2.setNivelAcessoUsuario(5);
        user2.setSituacaoUsuario("A");
        user2.setDataUltimaAtualizacao(LocalDate.now());
        user2.setPessoa(pessoaPersistida2);
        usuarioRepository.save(user2);

        UsuarioInstituicao ui2 = new UsuarioInstituicao();
        ui2.setUsuario(user2);
        ui2.setInstituicao(inst1);
        ui2.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui2);

        // PESSOA 3: SUPER-USUARIO - NIVEL 9 

        Pessoa pessoa3 = new Pessoa();
        pessoa3.setNomePessoa("Maria dos Santos");
        pessoa3.setEmailPessoa("maria@email.com");
        pessoa3.setCelularPessoa("+55-11-99999-9999");
        pessoa3.setNomePaisPessoa("Brasil");
        pessoa3.setNomeEstadoPessoa("MG");
        pessoa3.setNomeCidadePessoa("Belo Horizonte");
        pessoa3.setCurriculoPessoal("Comentario será adiconado em breve");
        pessoa3.setComentarios("Comentarios em elaboracao");
        pessoa3.setSituacaoPessoa("A");
        pessoa3.setDataInclusao(LocalDate.now());
        pessoa3.setDataUltimaAtualizacao(LocalDate.now());
        pessoaRepository.save(pessoa3);
        Pessoa pessoaPersistida3 = pessoaRepository.findById(pessoa3.getId()).orElseThrow();

        Usuario user3 = new Usuario();
        user3.setCodUsuario("superu");
        user3.setSenha("superu");
        user3.setNivelAcessoUsuario(9);
        user3.setSituacaoUsuario("A");
        user3.setDataUltimaAtualizacao(LocalDate.now());
        user3.setPessoa(pessoaPersistida3);
        usuarioRepository.save(user3);

        UsuarioInstituicao ui3 = new UsuarioInstituicao();
        ui3.setUsuario(user3);
        ui3.setInstituicao(inst3);
        ui3.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui3);

        System.out.println("****** Recarregou a base de dados     **************************************");
        System.out.println("****** ");
    }
}
