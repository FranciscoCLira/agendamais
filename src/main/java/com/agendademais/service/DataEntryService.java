package com.agendademais.service;

import com.agendademais.dto.DataEntryRequest;
import com.agendademais.dto.DataEntryResponse;
import com.agendademais.dto.UsuarioCSVRecord;
import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import com.agendademais.util.ExcelToCsvUtil;
import com.agendademais.util.PhoneNumberUtil;
import com.agendademais.util.CsvValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Arrays;

/**
 * Serviço para processamento de dados em massa (Data Entry)
 */
@Service
@Transactional
public class DataEntryService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private PessoaInstituicaoRepository pessoaInstituicaoRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Processa arquivo de carga em massa de usuários
     */
    public DataEntryResponse processarCargaUsuarios(DataEntryRequest request) {
        DataEntryResponse response = new DataEntryResponse();
        response.setInicioProcessamento(LocalDateTime.now());

        try {
            // Validações iniciais
            validarRequest(request, response);
            if (!response.isSuccess()) {
                return response;
            }

            // Converte Excel para CSV se necessário
            File csvFile = obterArquivoCSV(request, response);
            if (csvFile == null) {
                return response;
            }

            // Lê e processa os registros
            List<UsuarioCSVRecord> registros = lerRegistrosCSV(csvFile, request, response);
            if (registros.isEmpty()) {
                response.addError("Nenhum registro válido encontrado no arquivo");
                return response;
            }

            // Gera credenciais automáticas
            gerarCredenciais(registros, request, response);

            // Valida registros
            validarRegistros(registros, request, response);

            // Processa os registros válidos
            processarRegistros(registros, request, response);

            // Gera arquivo de resultado se solicitado
            if (request.isGerarArquivoResultado()) {
                gerarArquivoResultado(registros, request, response);
            }

            response.setFimProcessamento(LocalDateTime.now());

        } catch (Exception e) {
            response.addError("Erro durante processamento: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Valida o request inicial
     */
    private void validarRequest(DataEntryRequest request, DataEntryResponse response) {
        if (request.getArquivo() == null || request.getArquivo().isEmpty()) {
            response.addError("Arquivo não informado");
        }

        if (request.getTipoCarga() == null) {
            response.addError("Tipo de carga não informado");
        }

        if (request.getFormatoUsuario() == null) {
            response.addError("Formato de usuário não informado");
        }

        // Valida instituição
        if (request.getInstituicaoId() != null) {
            Optional<Instituicao> instituicao = instituicaoRepository.findById(request.getInstituicaoId());
            if (!instituicao.isPresent()) {
                response.addError("Instituição não encontrada: " + request.getInstituicaoId());
            }
        }

        // Valida sub-instituição se informada
        if (request.getSubInstituicaoId() != null) {
            Optional<SubInstituicao> subInstituicao = subInstituicaoRepository.findById(request.getSubInstituicaoId());
            if (!subInstituicao.isPresent()) {
                response.addError("Sub-instituição não encontrada: " + request.getSubInstituicaoId());
            }
        }
    }

    /**
     * Obtém arquivo CSV (converte de Excel se necessário)
     */
    private File obterArquivoCSV(DataEntryRequest request, DataEntryResponse response) {
        try {
            MultipartFile arquivo = request.getArquivo();
            String nomeArquivo = arquivo.getOriginalFilename();

            if (nomeArquivo == null) {
                response.addError("Nome do arquivo não informado");
                return null;
            }

            // Se já é CSV, retorna um arquivo temporário
            if (nomeArquivo.toLowerCase().endsWith(".csv")) {
                File tempCsvFile = File.createTempFile("upload_csv_", ".csv");
                arquivo.transferTo(tempCsvFile);
                return tempCsvFile;
            }

            // Se é Excel, converte para CSV
            if (ExcelToCsvUtil.isValidExcelFile(arquivo)) {
                response.addInfo("Convertendo Excel para CSV...");
                File csvFile = ExcelToCsvUtil.convertExcelToCsv(arquivo, request.getSeparadorCSV());
                response.addInfo("Conversão concluída");
                return csvFile;
            }

            response.addError("Tipo de arquivo não suportado. Use .csv, .xlsx ou .xls");
            return null;

        } catch (Exception e) {
            response.addError("Erro ao processar arquivo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lê registros do arquivo CSV
     */
    private List<UsuarioCSVRecord> lerRegistrosCSV(File csvFile, DataEntryRequest request, DataEntryResponse response) {
        List<UsuarioCSVRecord> registros = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

            String linha;
            int numeroLinha = 0;
            String[] headers = null;

            while ((linha = reader.readLine()) != null) {
                numeroLinha++;

                // Remove BOM se presente
                if (numeroLinha == 1 && linha.startsWith("\uFEFF")) {
                    linha = linha.substring(1);
                }

                String[] campos = linha.split(request.getSeparadorCSV(), -1);

                // Primeira linha são os headers
                if (numeroLinha == 1) {
                    headers = campos;
                    continue;
                }

                // Pula linhas vazias
                if (linha.trim().isEmpty()) {
                    continue;
                }

                try {
                    UsuarioCSVRecord registro = criarRegistroFromCSV(campos, headers, numeroLinha);
                    registros.add(registro);
                    response.incrementarRegistrosLidos();
                } catch (Exception e) {
                    response.addWarning("Linha " + numeroLinha + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            response.addError("Erro ao ler arquivo CSV: " + e.getMessage());
        }

        return registros;
    }

    /**
     * Cria registro a partir dos dados do CSV
     */
    private UsuarioCSVRecord criarRegistroFromCSV(String[] campos, String[] headers, int numeroLinha) {
        if (headers == null) {
            throw new IllegalStateException("Headers não definidos");
        }

        UsuarioCSVRecord registro = new UsuarioCSVRecord();

        for (int i = 0; i < Math.min(campos.length, headers.length); i++) {
            String header = headers[i].trim().toLowerCase().replaceAll("[^a-z]", "");
            String valor = campos[i].trim();

            switch (header) {
                case "emailpessoa":
                case "email":
                    registro.setEmail(valor);
                    break;
                case "nomepessoa":
                case "nome":
                    registro.setNome(valor);
                    break;
                case "celularpessoa":
                case "celular":
                    registro.setCelular(valor);
                    break;
                case "pais":
                    registro.setPais(valor);
                    break;
                case "estado":
                    registro.setEstado(valor);
                    break;
                case "cidade":
                    registro.setCidade(valor);
                    break;
                case "comentarios":
                    registro.setComentarios(valor);
                    break;
                case "instituicaoid":
                    if (!valor.isEmpty()) {
                        try {
                            registro.setInstituicaoId(Long.parseLong(valor));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("InstituicaoId inválido: " + valor);
                        }
                    }
                    break;
                case "identificacaopessoainstituicao":
                    registro.setIdentificacaoPessoaInstituicao(valor);
                    break;
                case "subinstituicaoid":
                    if (!valor.isEmpty()) {
                        try {
                            registro.setSubInstituicaoId(Long.parseLong(valor));
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("SubInstituicaoId inválido: " + valor);
                        }
                    }
                    break;
                case "identificacaopessoasubinstituicao":
                    registro.setIdentificacaoPessoaSubInstituicao(valor);
                    break;
                case "username":
                    registro.setUsername(valor);
                    break;
                case "password":
                    registro.setPassword(valor);
                    break;
            }
        }

        return registro;
    }

    /**
     * Gera credenciais automáticas para os usuários
     */
    private void gerarCredenciais(List<UsuarioCSVRecord> registros, DataEntryRequest request,
            DataEntryResponse response) {
        // Busca o maior username já existente com o prefixo
        String prefixo = "teste".equalsIgnoreCase(request.getTipoCarga()) ? "X" : "U";
        String maxUsername = usuarioRepository.findMaxUsernameStartingWith(prefixo);
        int contador = 1;
        if (maxUsername != null && maxUsername.length() > 1) {
            try {
                contador = Integer.parseInt(maxUsername.substring(1)) + 1;
            } catch (NumberFormatException e) {
                // Se não conseguir converter, começa do 1
                contador = 1;
            }
        }
        for (UsuarioCSVRecord registro : registros) {
            // Se não tem username definido, gera automaticamente
            if (registro.getUsername() == null || registro.getUsername().trim().isEmpty()) {
                String username = gerarUsername(request.getFormatoUsuario(), contador, request.getTipoCarga());
                registro.setUsername(username);
            }
            // Se não tem password definido, gera automaticamente
            if (registro.getPassword() == null || registro.getPassword().trim().isEmpty()) {
                String password = gerarPassword(request.getFormatoUsuario(), contador, request.getTipoCarga());
                registro.setPassword(password);
            }
            registro.setNumeroSequencial(contador);
            contador++;
        }
        response.addInfo("Credenciais geradas para " + registros.size() + " usuários");
    }

    /**
     * Gera username baseado no formato especificado
     */
    private String gerarUsername(String formato, int contador, String tipoCarga) {
        String prefixo = "teste".equalsIgnoreCase(tipoCarga) ? "X" : "U";
        return String.format("%s%05d", prefixo, contador);
    }

    /**
     * Gera password baseado no formato especificado
     * TESTE: X00001$ (prefixo X com sufixo $)
     * REAL: U00001% (prefixo U com sufixo % para passar na validação)
     */
    private String gerarPassword(String formato, int contador, String tipoCarga) {
        String prefixo = "teste".equalsIgnoreCase(tipoCarga) ? "X" : "U";
        // TESTE: sufixo $ | REAL: sufixo %
        String sufixo = "teste".equalsIgnoreCase(tipoCarga) ? "$" : "%";
        return String.format("%s%05d%s", prefixo, contador, sufixo);
    }

    /**
     * Valida todos os registros
     */
    private void validarRegistros(List<UsuarioCSVRecord> registros, DataEntryRequest request,
            DataEntryResponse response) {
        for (int i = 0; i < registros.size(); i++) {
            UsuarioCSVRecord registro = registros.get(i);
            validarRegistro(registro, i + 1, request, response);
        }
    }

    /**
     * Valida um registro individual
     */
    private void validarRegistro(UsuarioCSVRecord registro, int numeroRegistro, DataEntryRequest request,
            DataEntryResponse response) {
        // Use a nova validação mais robusta
        List<CsvValidationUtil.ValidationResult> results = CsvValidationUtil.validateCsvLine(
                registro.getEmail(), registro.getNome(), registro.getCelular(),
                registro.getPais(), registro.getEstado(), registro.getCidade(),
                registro.getInstituicaoId() != null ? registro.getInstituicaoId().toString() : null,
                registro.getSubInstituicaoId() != null ? registro.getSubInstituicaoId().toString() : null);

        // Processa os resultados da validação
        for (CsvValidationUtil.ValidationResult result : results) {
            String message = "Registro " + numeroRegistro + ": " + result.getMessage();

            switch (result.getSeverity()) {
                case "error":
                    if (!result.isValid()) {
                        response.addError(message);
                        return; // Para na primeira validação crítica
                    }
                    break;
                case "warning":
                    response.addWarning(message);
                    break;
                case "info":
                    // Só adiciona infos se está validando celular
                    if (request.isValidarCelular() && result.getMessage().contains("Celular")) {
                        response.addInfo(message);
                    }
                    break;
            }
        }

        // Formata celular se a validação passou
        if (request.isValidarCelular() && registro.getCelular() != null && !registro.getCelular().trim().isEmpty()) {
            try {
                String celularFormatado = PhoneNumberUtil.formatPhoneNumber(registro.getCelular());
                // Salva no registro apenas os números para o banco
                String celularSomenteNumeros = com.agendademais.utils.StringUtils.somenteNumeros(celularFormatado);
                registro.setCelular(celularSomenteNumeros);
            } catch (Exception e) {
                response.addWarning(
                        "Registro " + numeroRegistro + ": Não foi possível formatar celular: " + e.getMessage());
            }
        }

        // Verifica se email já existe (validação de negócio)
        if (pessoaRepository.findByEmailPessoa(registro.getEmail()).isPresent()) {
            response.addWarning("Registro " + numeroRegistro + ": Email já existe no sistema: " + registro.getEmail());
        }

        // Verifica se username já existe (validação de negócio)
        if (usuarioRepository.findByUsername(registro.getUsername()).isPresent()) {
            response.addError("Registro " + numeroRegistro + ": Username já existe: " + registro.getUsername());
        }
    }

    /**
     * Processa todos os registros válidos
     */
    @Transactional
    private void processarRegistros(List<UsuarioCSVRecord> registros, DataEntryRequest request,
            DataEntryResponse response) {
        for (int i = 0; i < registros.size(); i++) {
            UsuarioCSVRecord registro = registros.get(i);

            // Se o registro não tem instituicaoId/subInstituicaoId, usa o do request
            // (formulário)
            if (registro.getInstituicaoId() == null && request.getInstituicaoId() != null) {
                registro.setInstituicaoId(request.getInstituicaoId());
            }
            if (registro.getSubInstituicaoId() == null && request.getSubInstituicaoId() != null) {
                registro.setSubInstituicaoId(request.getSubInstituicaoId());
            }

            try {
                // Usa uma transação separada para cada usuário para evitar rollback em lote
                criarUsuarioTransactional(registro, request, response);
                response.incrementarRegistrosProcessados();
                response.incrementarRegistrosIncluidos();
            } catch (Exception e) {
                response.addError("Registro " + (i + 1) + ": Erro ao criar usuário: " + e.getMessage());
                e.printStackTrace(); // Log detalhado do erro
            }
        }
    }

    /**
     * Cria usuário em transação separada para evitar rollback em lote
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void criarUsuarioTransactional(UsuarioCSVRecord registro, DataEntryRequest request,
            DataEntryResponse response) {
        try {
            criarUsuario(registro, request, response);
        } catch (Exception e) {
            // Re-lança a exceção para que seja capturada no método chamador
            throw new RuntimeException("Erro ao processar usuário " + registro.getEmail() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Cria usuário a partir do registro
     */
    private void criarUsuario(UsuarioCSVRecord registro, DataEntryRequest request, DataEntryResponse response) {
        System.out.println("=== INICIANDO CRIAÇÃO DE USUÁRIO: " + registro.getEmail() + " ===");

        // Verifica se usuário já existe
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmailPessoa(registro.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new RuntimeException("Usuário com email " + registro.getEmail() + " já existe");
        }
        System.out.println("✓ Usuário não existe ainda");

        // Verifica também se já existe uma Pessoa com esse email
        Optional<Pessoa> pessoaExistente = pessoaRepository.findByEmailPessoa(registro.getEmail());
        if (pessoaExistente.isPresent()) {
            throw new RuntimeException("Pessoa com email " + registro.getEmail() + " já existe");
        }
        System.out.println("✓ Pessoa não existe ainda");

        // Cria Pessoa
        Pessoa pessoa = new Pessoa();
        pessoa.setNomePessoa(registro.getNome());
        pessoa.setEmailPessoa(registro.getEmail());
        pessoa.setCelularPessoa(registro.getCelular());
        pessoa.setComentarios(registro.getComentarios());
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        pessoa.setSituacaoPessoa("A"); // Ativo

        // Busca ou cria Local hierárquico
        Local localCidade = buscarOuCriarLocal(registro.getPais(), registro.getEstado(), registro.getCidade());
        if (localCidade != null) {
            pessoa.setCidade(localCidade);

            // Se a cidade tem estado pai, associa também
            if (localCidade.getLocalPai() != null && localCidade.getLocalPai().getTipoLocal() == 2) {
                pessoa.setEstado(localCidade.getLocalPai());

                // Se o estado tem país pai, associa também
                if (localCidade.getLocalPai().getLocalPai() != null &&
                        localCidade.getLocalPai().getLocalPai().getTipoLocal() == 1) {
                    pessoa.setPais(localCidade.getLocalPai().getLocalPai());
                }
            }
        }

        System.out.println("✓ Locais processados");

        // Salva Pessoa primeiro e assegura que tem ID
        System.out.println("→ Salvando Pessoa...");
        try {
            pessoa = pessoaRepository.saveAndFlush(pessoa);
            System.out.println("✓ Pessoa salva com ID: " + pessoa.getId());
        } catch (Exception e) {
            System.err.println("✗ ERRO ao salvar Pessoa: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar pessoa: " + e.getMessage(), e);
        }

        if (pessoa.getId() == null) {
            throw new RuntimeException("Erro ao salvar pessoa - ID não foi gerado");
        }

        // Cria Usuario
        System.out.println("→ Criando Usuário...");
        Usuario usuario = new Usuario();
        usuario.setUsername(registro.getUsername());
        usuario.setPassword(passwordEncoder.encode(registro.getPassword()));
        usuario.setPessoa(pessoa);
        usuario.setSituacaoUsuario("A"); // Ativo
        usuario.setDataUltimaAtualizacao(LocalDate.now());

        // Salva usuário e assegura que tem ID
        System.out.println("→ Salvando Usuário...");
        try {
            usuario = usuarioRepository.saveAndFlush(usuario);
            System.out.println("✓ Usuário salvo com ID: " + usuario.getId());
        } catch (Exception e) {
            System.err.println("✗ ERRO ao salvar Usuário: " + e.getMessage());
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        }

        if (usuario.getId() == null) {
            throw new RuntimeException("Erro ao salvar usuário - ID não foi gerado");
        }

        // Criar relacionamentos institucionais se informados
        criarRelacionamentosInstitucionais(pessoa, registro, response);

        System.out.println("✓ USUÁRIO CRIADO COM SUCESSO: " + registro.getEmail() + " (ID: " + usuario.getId() + ")");
    }

    /**
     * Busca ou cria Local baseado nos dados geográficos
     */
    private Local buscarOuCriarLocal(String pais, String estado, String cidade) {
        if (cidade == null || cidade.trim().isEmpty()) {
            return null;
        }

        try {
            // Busca ou cria país (tipoLocal = 1)
            Local localPais = null;
            if (pais != null && !pais.trim().isEmpty()) {
                Optional<Local> paisExistente = localRepository.findByTipoLocalAndNomeLocal(Integer.valueOf(1), pais);
                if (paisExistente.isPresent()) {
                    localPais = paisExistente.get();
                } else {
                    localPais = new Local();
                    localPais.setTipoLocal(1);
                    localPais.setNomeLocal(pais);
                    localPais.setLocalPai(null);
                    localPais.setDataUltimaAtualizacao(LocalDate.now());
                    localPais = localRepository.save(localPais);
                }
            }

            // Busca ou cria estado (tipoLocal = 2)
            Local localEstado = null;
            if (estado != null && !estado.trim().isEmpty()) {
                Optional<Local> estadoExistente = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(2, estado,
                        localPais);
                if (estadoExistente.isPresent()) {
                    localEstado = estadoExistente.get();
                } else {
                    localEstado = new Local();
                    localEstado.setTipoLocal(2);
                    localEstado.setNomeLocal(estado);
                    localEstado.setLocalPai(localPais);
                    localEstado.setDataUltimaAtualizacao(LocalDate.now());
                    localEstado = localRepository.save(localEstado);
                }
            }

            // Busca ou cria cidade (tipoLocal = 3)
            Optional<Local> cidadeExistente = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(3, cidade,
                    localEstado);
            if (cidadeExistente.isPresent()) {
                return cidadeExistente.get();
            } else {
                Local localCidade = new Local();
                localCidade.setTipoLocal(3);
                localCidade.setNomeLocal(cidade);
                localCidade.setLocalPai(localEstado);
                localCidade.setDataUltimaAtualizacao(LocalDate.now());
                return localRepository.save(localCidade);
            }

        } catch (Exception e) {
            // Se houver erro, criar apenas a cidade sem hierarquia
            Local localCidade = new Local();
            localCidade.setTipoLocal(3);
            localCidade.setNomeLocal(cidade);
            localCidade.setLocalPai(null);
            localCidade.setDataUltimaAtualizacao(LocalDate.now());
            return localRepository.save(localCidade);
        }
    }

    /**
     * Gera arquivo com resultado do processamento
     */
    private void gerarArquivoResultado(List<UsuarioCSVRecord> registros, DataEntryRequest request,
            DataEntryResponse response) {
        try {
            File arquivoResultado = File.createTempFile("resultado_carga_", ".csv");

            try (PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(arquivoResultado), StandardCharsets.UTF_8))) {

                // BOM para UTF-8
                writer.write('\uFEFF');

                // Cabeçalho
                writer.println("username;password;email;nome;celular;pais;estado;cidade;status");

                // Dados
                for (UsuarioCSVRecord registro : registros) {
                    writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                            registro.getUsername() != null ? registro.getUsername() : "",
                            registro.getPassword() != null ? registro.getPassword() : "",
                            registro.getEmail() != null ? registro.getEmail() : "",
                            registro.getNome() != null ? registro.getNome() : "",
                            registro.getCelular() != null ? registro.getCelular() : "",
                            registro.getPais() != null ? registro.getPais() : "",
                            registro.getEstado() != null ? registro.getEstado() : "",
                            registro.getCidade() != null ? registro.getCidade() : "",
                            "Processado");
                }
            }

            response.setArquivoGerado(arquivoResultado.getAbsolutePath());
            response.addInfo("Arquivo de resultado gerado: " + arquivoResultado.getAbsolutePath());

        } catch (Exception e) {
            response.addWarning("Erro ao gerar arquivo de resultado: " + e.getMessage());
        }
    }

    /**
     * Cria relacionamentos institucionais se informados
     */
    private void criarRelacionamentosInstitucionais(Pessoa pessoa, UsuarioCSVRecord registro,
            DataEntryResponse response) {
        try {
            // Relacionamento com Instituição
            if (registro.getInstituicaoId() != null) {
                Optional<Instituicao> instituicaoOpt = instituicaoRepository.findById(registro.getInstituicaoId());
                if (instituicaoOpt.isPresent()) {
                    Instituicao instituicao = instituicaoOpt.get();
                    // Cria e persiste PessoaInstituicao
                    PessoaInstituicao pessoaInstituicao = new PessoaInstituicao();
                    pessoaInstituicao.setPessoa(pessoa);
                    pessoaInstituicao.setInstituicao(instituicao);
                    pessoaInstituicao.setIdentificacaoPessoaInstituicao(registro.getIdentificacaoPessoaInstituicao());
                    pessoaInstituicao.setDataAfiliacao(null); // Não vem do CSV
                    pessoaInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
                    pessoaInstituicaoRepository.save(pessoaInstituicao);

                    // Cria e persiste UsuarioInstituicao
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(registro.getUsername());
                    if (usuarioOpt.isPresent()) {
                        UsuarioInstituicao usuarioInstituicao = new UsuarioInstituicao();
                        usuarioInstituicao.setUsuario(usuarioOpt.get());
                        usuarioInstituicao.setInstituicao(instituicao);
                        usuarioInstituicao.setSitAcessoUsuarioInstituicao("A"); // Ativo por padrão
                        usuarioInstituicao.setNivelAcessoUsuarioInstituicao(1); // Participante por padrão
                        usuarioInstituicaoRepository.save(usuarioInstituicao);
                    } else {
                        response.addWarning(
                                "Usuário não encontrado para criar vínculo institucional: " + registro.getUsername());
                    }

                    response.addInfo("Instituição " + registro.getInstituicaoId()
                            + " associada e persistida para usuário " + registro.getUsername());
                } else {
                    response.addWarning("Instituição " + registro.getInstituicaoId() + " não encontrada para usuário "
                            + registro.getUsername());
                }
            }

            // Relacionamento com SubInstituição
            if (registro.getSubInstituicaoId() != null) {
                Optional<SubInstituicao> subInstituicaoOpt = subInstituicaoRepository
                        .findById(registro.getSubInstituicaoId());
                if (subInstituicaoOpt.isPresent()) {
                    SubInstituicao subInstituicao = subInstituicaoOpt.get();
                    Optional<Instituicao> instituicaoOpt = instituicaoRepository
                            .findById(subInstituicao.getInstituicao().getId());
                    if (instituicaoOpt.isPresent()) {
                        Instituicao instituicao = instituicaoOpt.get();
                        // Cria e persiste PessoaSubInstituicao
                        PessoaSubInstituicao pessoaSubInstituicao = new PessoaSubInstituicao();
            pessoaSubInstituicao.setPessoa(pessoa);
            pessoaSubInstituicao.setInstituicao(instituicao);
            pessoaSubInstituicao.setSubInstituicao(subInstituicao);
            pessoaSubInstituicao
                .setIdentificacaoPessoaSubInstituicao(registro.getIdentificacaoPessoaSubInstituicao());
            pessoaSubInstituicao.setDataAfiliacao(null); // Não vem do CSV
            pessoaSubInstituicao.setDataUltimaAtualizacao(java.time.LocalDate.now());
            pessoaSubInstituicaoRepository.save(pessoaSubInstituicao);
                        response.addInfo("Sub-Instituição " + registro.getSubInstituicaoId()
                                + " associada e persistida para usuário " + registro.getUsername());
                    } else {
                        response.addWarning("Instituição da Sub-Instituição " + registro.getSubInstituicaoId()
                                + " não encontrada para usuário " + registro.getUsername());
                    }
                } else {
                    response.addWarning("Sub-Instituição " + registro.getSubInstituicaoId()
                            + " não encontrada para usuário " + registro.getUsername());
                }
            }
        } catch (Exception e) {
            response.addWarning("Erro ao criar relacionamentos institucionais para " + registro.getUsername() + ": "
                    + e.getMessage());
        }
    }

    /**
     * Valida conteúdo do arquivo e retorna número de registros
     */
    public int validarConteudoArquivo(MultipartFile arquivo, String separadorCsv, DataEntryResponse response) {
        try {
            File tempFile = File.createTempFile("validation_", "_temp");
            arquivo.transferTo(tempFile);

            File csvFile = tempFile;

            // Se for Excel, converte para CSV primeiro
            String nomeArquivo = arquivo.getOriginalFilename();
            if (nomeArquivo != null
                    && (nomeArquivo.toLowerCase().endsWith(".xlsx") || nomeArquivo.toLowerCase().endsWith(".xls"))) {
                File csvConverted = File.createTempFile("converted_", ".csv");
                ExcelToCsvUtil.convertExcelToCsv(tempFile, csvConverted);
                csvFile = csvConverted;
                tempFile.delete();
            }

            // Conta registros no CSV
            int totalRegistros = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

                String linha = reader.readLine(); // Header
                if (linha != null) {
                    // Remove BOM se presente no header
                    if (linha.startsWith("\uFEFF")) {
                        linha = linha.substring(1);
                    }

                    response.addInfo("Cabeçalho encontrado: " + linha);

                    // Validar cabeçalhos obrigatórios
                    String[] headers = linha.split(separadorCsv);

                    // Debug: mostrar headers encontrados
                    for (int i = 0; i < headers.length; i++) {
                        response.addInfo("Header[" + i + "]: '" + headers[i].trim() + "'");
                    }

                    boolean hasEmail = Arrays.stream(headers).anyMatch(h -> {
                        String cleanHeader = h.trim().toLowerCase().replaceAll("[^a-z]", "");
                        return cleanHeader.equals("email");
                    });
                    boolean hasNome = Arrays.stream(headers).anyMatch(h -> {
                        String cleanHeader = h.trim().toLowerCase().replaceAll("[^a-z]", "");
                        return cleanHeader.equals("nome");
                    });
                    boolean hasCelular = Arrays.stream(headers).anyMatch(h -> {
                        String cleanHeader = h.trim().toLowerCase().replaceAll("[^a-z]", "");
                        return cleanHeader.equals("celular");
                    });

                    if (!hasEmail)
                        response.addError("Coluna 'email' obrigatória não encontrada");
                    if (!hasNome)
                        response.addError("Coluna 'nome' obrigatória não encontrada");
                    if (!hasCelular)
                        response.addError("Coluna 'celular' obrigatória não encontrada");
                }

                // Conta linhas de dados
                while ((linha = reader.readLine()) != null) {
                    linha = linha.trim();
                    if (!linha.isEmpty()) {
                        totalRegistros++;
                    }
                }
            }

            csvFile.delete();
            return totalRegistros;

        } catch (Exception e) {
            response.addError("Erro ao validar conteúdo: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Processa exclusão em massa baseada em arquivo de importação
     * Remove apenas usuários e relacionamentos, preserva configurações sistêmicas
     */
    public DataEntryResponse processarExclusaoMassa(MultipartFile arquivo, String separadorCsv, boolean confirmacao) {
        DataEntryResponse response = new DataEntryResponse();
        response.setInicioProcessamento(LocalDateTime.now());

        try {
            // Lê emails do arquivo
            List<String> emailsParaExcluir = extrairEmailsDoArquivo(arquivo, separadorCsv, response);

            if (emailsParaExcluir.isEmpty()) {
                response.addError("Nenhum email encontrado no arquivo para exclusão");
                return response;
            }

            response.addInfo("Emails encontrados para exclusão: " + emailsParaExcluir.size());

            // Se não é confirmação, apenas lista o que seria excluído
            if (!confirmacao) {
                for (String email : emailsParaExcluir) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        response.addInfo("Seria excluído: " + email + " (ID: " + usuario.getId() + ")");
                        response.incrementarRegistrosLidos();
                    } else {
                        response.addWarning("Email não encontrado na base: " + email);
                    }
                }
                response.addInfo("SIMULAÇÃO: " + response.getRegistrosLidos() + " usuários seriam excluídos");
                response.addInfo("Para confirmar a exclusão, adicione o parâmetro confirmacao=true");
                return response;
            }

            // Processa exclusão confirmada
            int excluidos = 0;
            for (String email : emailsParaExcluir) {
                try {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        if (usuario.getPessoa() != null) {
                            Pessoa pessoa = usuario.getPessoa();

                            // Remove todos os vínculos de UsuarioInstituicao
                            usuarioInstituicaoRepository.findByUsuarioId(usuario.getId())
                                    .forEach(ui -> usuarioInstituicaoRepository.delete(ui));

                            // Remove todos os vínculos de PessoaInstituicao
                            pessoaInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());

                            // Remove todos os vínculos de PessoaSubInstituicao
                            pessoaSubInstituicaoRepository.deleteAllByPessoaId(pessoa.getId());

                            // Remove usuário
                            usuarioRepository.delete(usuario);

                            // Remove pessoa
                            pessoaRepository.delete(pessoa);

                            response.addInfo("Excluído: " + email + " (ID: " + usuario.getId() + ")");
                            excluidos++;
                        }
                    } else {
                        response.addWarning("Email não encontrado na base: " + email);
                    }
                } catch (Exception e) {
                    response.addError("Erro ao excluir " + email + ": " + e.getMessage());
                }
            }

            response.setRegistrosProcessados(excluidos);
            response.addInfo("EXCLUSÃO CONCLUÍDA: " + excluidos + " usuários removidos");

            if (excluidos > 0) {
                response.addInfo("⚠️ IMPORTANTE: Locais (países/estados/cidades) foram preservados");
                response.addInfo("⚠️ Instituições e sub-instituições foram preservadas");
            }

        } catch (Exception e) {
            response.addError("Erro durante exclusão em massa: " + e.getMessage());
            e.printStackTrace();
        }

        response.setFimProcessamento(LocalDateTime.now());
        return response;
    }

    /**
     * Extrai lista de emails do arquivo CSV/Excel
     */
    private List<String> extrairEmailsDoArquivo(MultipartFile arquivo, String separadorCsv,
            DataEntryResponse response) {
        List<String> emails = new ArrayList<>();

        try {
            // Cria arquivo temporário
            File csvFile = File.createTempFile("delete_bulk_", "_temp");
            arquivo.transferTo(csvFile);

            if (arquivo.getOriginalFilename().toLowerCase().endsWith(".xlsx") ||
                    arquivo.getOriginalFilename().toLowerCase().endsWith(".xls")) {
                File csvConverted = new File(csvFile.getParent(), "converted_" + System.currentTimeMillis() + ".csv");
                ExcelToCsvUtil.convertExcelToCsv(csvFile, csvConverted);
                csvFile.delete();
                csvFile = csvConverted;
            }

            // Lê emails do CSV
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

                String linha = reader.readLine(); // Header
                if (linha != null) {
                    // Remove BOM se presente
                    if (linha.startsWith("\uFEFF")) {
                        linha = linha.substring(1);
                    }

                    String[] headers = linha.split(separadorCsv);
                    int emailColIndex = -1;

                    // Encontra coluna de email
                    for (int i = 0; i < headers.length; i++) {
                        String header = headers[i].trim().toLowerCase().replaceAll("[^a-z]", "");
                        if (header.equals("email")) {
                            emailColIndex = i;
                            break;
                        }
                    }

                    if (emailColIndex == -1) {
                        response.addError("Coluna 'email' não encontrada no arquivo");
                        return emails;
                    }

                    // Lê emails das linhas de dados
                    while ((linha = reader.readLine()) != null) {
                        linha = linha.trim();
                        if (!linha.isEmpty()) {
                            String[] campos = linha.split(separadorCsv, -1);
                            if (campos.length > emailColIndex) {
                                String email = campos[emailColIndex].trim();
                                if (!email.isEmpty() && !emails.contains(email)) {
                                    emails.add(email);
                                }
                            }
                        }
                    }
                }
            }

            csvFile.delete();

        } catch (Exception e) {
            response.addError("Erro ao extrair emails do arquivo: " + e.getMessage());
        }

        return emails;
    }
}
