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
    private final PessoaInstituicaoRepository pessoaInstituicaoRepository;
    private final PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    
    @Value("${app.reload-data:false}")
    private boolean reloadData;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlAuto;
    
    public DataLoader(InstituicaoRepository instituicaoRepository,
    	           	  SubInstituicaoRepository subInstituicaoRepository,
    	      	      TipoAtividadeRepository tipoAtividadeRepository, 
                      UsuarioRepository usuarioRepository,
                      PessoaRepository pessoaRepository,
                      UsuarioInstituicaoRepository usuarioInstituicaoRepository,
                      PessoaInstituicaoRepository pessoaInstituicaoRepository,
                      PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository
                      ) {
        this.instituicaoRepository = instituicaoRepository;
        this.subInstituicaoRepository = subInstituicaoRepository;
        this.tipoAtividadeRepository = tipoAtividadeRepository;
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioInstituicaoRepository = usuarioInstituicaoRepository;
        this.pessoaInstituicaoRepository = pessoaInstituicaoRepository;
        this.pessoaSubInstituicaoRepository = pessoaSubInstituicaoRepository;
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
        user1.setCodUsuario("parti1");
        user1.setSenha("parti1$");
        user1.setNivelAcessoUsuario(1);
        user1.setSituacaoUsuario("A");
        user1.setDataUltimaAtualizacao(LocalDate.now());
        user1.setPessoa(pessoaPersistida1);
        
        System.out.println("*** ");
        System.out.println("*** Persistindo Usuário: " + user1.getCodUsuario() + " com senha: '" + user1.getSenha() + "'");
        
        usuarioRepository.save(user1);

        UsuarioInstituicao ui11 = new UsuarioInstituicao();
        ui11.setUsuario(user1);
        ui11.setInstituicao(inst1);
        ui11.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui11);

        UsuarioInstituicao ui12 = new UsuarioInstituicao();
        ui12.setUsuario(user1);
        ui12.setInstituicao(inst2);
        ui12.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui12);

        UsuarioInstituicao ui13 = new UsuarioInstituicao();
        ui13.setUsuario(user1);
        ui13.setInstituicao(inst3);
        ui13.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui13);
        
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
        user2.setSenha("admin1$");
        user2.setNivelAcessoUsuario(5);
        user2.setSituacaoUsuario("A");
        user2.setDataUltimaAtualizacao(LocalDate.now());
        user2.setPessoa(pessoaPersistida2);

        System.out.println("*** Persistindo Usuário: " + user2.getCodUsuario() + " com senha: '" + user2.getSenha() + "'");
        System.out.println("*** ");
        
        usuarioRepository.save(user2);

        UsuarioInstituicao ui21 = new UsuarioInstituicao();
        ui21.setUsuario(user2);
        ui21.setInstituicao(inst1);
        ui21.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui21);

        UsuarioInstituicao ui22 = new UsuarioInstituicao();
        ui22.setUsuario(user2);
        ui22.setInstituicao(inst2);
        ui22.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui22);

        UsuarioInstituicao ui23 = new UsuarioInstituicao();
        ui23.setUsuario(user2);
        ui23.setInstituicao(inst3);
        ui23.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui23);

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
        user3.setSenha("superu1$");
        user3.setNivelAcessoUsuario(9);
        user3.setSituacaoUsuario("A");
        user3.setDataUltimaAtualizacao(LocalDate.now());
        user3.setPessoa(pessoaPersistida3);
        
        System.out.println("*** Persistindo Usuário: " + user3.getCodUsuario() + " com senha: '" + user3.getSenha() + "'");
        System.out.println("*** ");
        
        usuarioRepository.save(user3);

        UsuarioInstituicao ui31 = new UsuarioInstituicao();
        ui31.setUsuario(user3);
        ui31.setInstituicao(inst1);
        ui31.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui31);

        UsuarioInstituicao ui32 = new UsuarioInstituicao();
        ui32.setUsuario(user3);
        ui32.setInstituicao(inst2);
        ui32.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui32);

        UsuarioInstituicao ui33 = new UsuarioInstituicao();
        ui33.setUsuario(user3);
        ui33.setInstituicao(inst3);
        ui33.setSitAcessoUsuarioInstituicao("A");
        usuarioInstituicaoRepository.save(ui33);

        // PESSOAINSTITUICAO E PESSOASUBINSTITUICAO 
        
        // PessoaInstituicao - 11
        PessoaInstituicao psInst11 = new PessoaInstituicao();
        psInst11.setPessoa(pessoa1);
        psInst11.setInstituicao(inst1);
        psInst11.setDataUltimaAtualizacao(LocalDate.now());
        psInst11.setDataAfiliacao(LocalDate.now());
        psInst11.setIdentificacaoPessoaInstituicao("psInst11");
        pessoaInstituicaoRepository.save(psInst11);

        // PessoaSubInstituicao - 11
        PessoaSubInstituicao psSub11 = new PessoaSubInstituicao();
        psSub11.setPessoa(pessoa1);
        psSub11.setSubInstituicao(subInst11);
        psSub11.setInstituicao(inst1);
        psSub11.setDataUltimaAtualizacao(LocalDate.now());
        psSub11.setDataAfiliacao(LocalDate.now());
        psSub11.setIdentificacaoPessoaSubInstituicao("psSub11");
        pessoaSubInstituicaoRepository.save(psSub11);        

        // PessoaInstituicao - 12
        PessoaInstituicao psInst12 = new PessoaInstituicao();
        psInst12.setPessoa(pessoa1);
        psInst12.setInstituicao(inst2);
        psInst12.setDataUltimaAtualizacao(LocalDate.now());
        psInst12.setDataAfiliacao(LocalDate.now());
        psInst12.setIdentificacaoPessoaInstituicao("psInst12");
        pessoaInstituicaoRepository.save(psInst12);

        // PessoaSubInstituicao - 12
        PessoaSubInstituicao psSub12 = new PessoaSubInstituicao();
        psSub12.setPessoa(pessoa1);
        psSub12.setSubInstituicao(subInst12);
        psSub12.setInstituicao(inst2);
        psSub12.setDataUltimaAtualizacao(LocalDate.now());
        psSub12.setDataAfiliacao(LocalDate.now());
        psSub12.setIdentificacaoPessoaSubInstituicao("psSub12");
        pessoaSubInstituicaoRepository.save(psSub12);              

        // PessoaInstituicao - 13
        PessoaInstituicao psInst13 = new PessoaInstituicao();
        psInst13.setPessoa(pessoa1);
        psInst13.setInstituicao(inst3);
        psInst13.setDataUltimaAtualizacao(LocalDate.now());
        psInst13.setDataAfiliacao(LocalDate.now());
        psInst13.setIdentificacaoPessoaInstituicao("psInst13");
        pessoaInstituicaoRepository.save(psInst13);

        // PessoaSubInstituicao - 13
        PessoaSubInstituicao psSub13 = new PessoaSubInstituicao();
        psSub13.setPessoa(pessoa1);
        psSub13.setSubInstituicao(subInst13);
        psSub13.setInstituicao(inst3);
        psSub13.setDataUltimaAtualizacao(LocalDate.now());
        psSub13.setDataAfiliacao(LocalDate.now());
        psSub13.setIdentificacaoPessoaSubInstituicao("psSub13");
        pessoaSubInstituicaoRepository.save(psSub13);        

        
        // PessoaInstituicao - 21
        PessoaInstituicao psInst21 = new PessoaInstituicao();
        psInst21.setPessoa(pessoa2);
        psInst21.setInstituicao(inst1);
        psInst21.setDataUltimaAtualizacao(LocalDate.now());
        psInst21.setDataAfiliacao(LocalDate.now());
        psInst21.setIdentificacaoPessoaInstituicao("psInst21");
        pessoaInstituicaoRepository.save(psInst21);

        // PessoaSubInstituicao - 21
        PessoaSubInstituicao psSub21 = new PessoaSubInstituicao();
        psSub21.setPessoa(pessoa2);
        psSub21.setSubInstituicao(subInst21);
        psSub21.setInstituicao(inst1);
        psSub21.setDataUltimaAtualizacao(LocalDate.now());
        psSub21.setDataAfiliacao(LocalDate.now());
        psSub21.setIdentificacaoPessoaSubInstituicao("psSub21");
        pessoaSubInstituicaoRepository.save(psSub21);        

        // PessoaInstituicao - 22
        PessoaInstituicao psInst22 = new PessoaInstituicao();
        psInst22.setPessoa(pessoa2);
        psInst22.setInstituicao(inst2);
        psInst22.setDataUltimaAtualizacao(LocalDate.now());
        psInst22.setDataAfiliacao(LocalDate.now());
        psInst22.setIdentificacaoPessoaInstituicao("psInst22");
        pessoaInstituicaoRepository.save(psInst22);

        // PessoaSubInstituicao - 22
        PessoaSubInstituicao psSub22 = new PessoaSubInstituicao();
        psSub22.setPessoa(pessoa2);
        psSub22.setSubInstituicao(subInst22);
        psSub22.setInstituicao(inst2);
        psSub22.setDataUltimaAtualizacao(LocalDate.now());
        psSub22.setDataAfiliacao(LocalDate.now());
        psSub22.setIdentificacaoPessoaSubInstituicao("psSub22");
        pessoaSubInstituicaoRepository.save(psSub22);              

        // PessoaInstituicao - 23
        PessoaInstituicao psInst23 = new PessoaInstituicao();
        psInst23.setPessoa(pessoa2);
        psInst23.setInstituicao(inst3);
        psInst23.setDataUltimaAtualizacao(LocalDate.now());
        psInst23.setDataAfiliacao(LocalDate.now());
        psInst23.setIdentificacaoPessoaInstituicao("psInst23");
        pessoaInstituicaoRepository.save(psInst23);

        // PessoaSubInstituicao - 23
        PessoaSubInstituicao psSub23 = new PessoaSubInstituicao();
        psSub23.setPessoa(pessoa2);
        psSub23.setSubInstituicao(subInst23);
        psSub23.setInstituicao(inst3);
        psSub23.setDataUltimaAtualizacao(LocalDate.now());
        psSub23.setDataAfiliacao(LocalDate.now());
        psSub23.setIdentificacaoPessoaSubInstituicao("psSub23");
        pessoaSubInstituicaoRepository.save(psSub23);        
        
        
        // PessoaInstituicao - 31
        PessoaInstituicao psInst31 = new PessoaInstituicao();
        psInst31.setPessoa(pessoa3);
        psInst31.setInstituicao(inst1);
        psInst31.setDataUltimaAtualizacao(LocalDate.now());
        psInst31.setDataAfiliacao(LocalDate.now());
        psInst31.setIdentificacaoPessoaInstituicao("psInst31");
        pessoaInstituicaoRepository.save(psInst31);

        // PessoaSubInstituicao - 31
        PessoaSubInstituicao psSub31 = new PessoaSubInstituicao();
        psSub31.setPessoa(pessoa3);
        psSub31.setSubInstituicao(subInst31);
        psSub31.setInstituicao(inst1);
        psSub31.setDataUltimaAtualizacao(LocalDate.now());
        psSub31.setDataAfiliacao(LocalDate.now());
        psSub31.setIdentificacaoPessoaSubInstituicao("psSub31");
        pessoaSubInstituicaoRepository.save(psSub31);        

        // PessoaInstituicao - 32
        PessoaInstituicao psInst32 = new PessoaInstituicao();
        psInst32.setPessoa(pessoa3);
        psInst32.setInstituicao(inst2);
        psInst32.setDataUltimaAtualizacao(LocalDate.now());
        psInst32.setDataAfiliacao(LocalDate.now());
        psInst32.setIdentificacaoPessoaInstituicao("psInst32");
        pessoaInstituicaoRepository.save(psInst32);

        // PessoaSubInstituicao - 32
        PessoaSubInstituicao psSub32 = new PessoaSubInstituicao();
        psSub32.setPessoa(pessoa3);
        psSub32.setSubInstituicao(subInst32);
        psSub32.setInstituicao(inst2);
        psSub32.setDataUltimaAtualizacao(LocalDate.now());
        psSub32.setDataAfiliacao(LocalDate.now());
        psSub32.setIdentificacaoPessoaSubInstituicao("psSub32");
        pessoaSubInstituicaoRepository.save(psSub32);              

        // PessoaInstituicao - 33
        PessoaInstituicao psInst33 = new PessoaInstituicao();
        psInst33.setPessoa(pessoa3);
        psInst33.setInstituicao(inst3);
        psInst33.setDataUltimaAtualizacao(LocalDate.now());
        psInst33.setDataAfiliacao(LocalDate.now());
        psInst33.setIdentificacaoPessoaInstituicao("psInst33");
        pessoaInstituicaoRepository.save(psInst33);

        // PessoaSubInstituicao - 33
        PessoaSubInstituicao psSub33 = new PessoaSubInstituicao();
        psSub33.setPessoa(pessoa3);
        psSub33.setSubInstituicao(subInst33);
        psSub33.setInstituicao(inst3);
        psSub33.setDataUltimaAtualizacao(LocalDate.now());
        psSub33.setDataAfiliacao(LocalDate.now());
        psSub33.setIdentificacaoPessoaSubInstituicao("psSub33");
        pessoaSubInstituicaoRepository.save(psSub33);        
        
       
        
        
        System.out.println("****** Recarregou a base de dados     **************************************");
        System.out.println("****** ");
    }
}
