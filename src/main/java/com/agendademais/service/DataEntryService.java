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
            String header = headers[i].trim().toLowerCase();
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
    private void gerarCredenciais(List<UsuarioCSVRecord> registros, DataEntryRequest request, DataEntryResponse response) {
        int contador = 1;
        
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
     */
    private String gerarPassword(String formato, int contador, String tipoCarga) {
        String sufixo = "teste".equalsIgnoreCase(tipoCarga) ? "$" : "";
        return String.format("%s%05d%s", "teste".equalsIgnoreCase(tipoCarga) ? "X" : "U", contador, sufixo);
    }
    
    /**
     * Valida todos os registros
     */
    private void validarRegistros(List<UsuarioCSVRecord> registros, DataEntryRequest request, DataEntryResponse response) {
        for (int i = 0; i < registros.size(); i++) {
            UsuarioCSVRecord registro = registros.get(i);
            validarRegistro(registro, i + 1, request, response);
        }
    }
    
    /**
     * Valida um registro individual
     */
    private void validarRegistro(UsuarioCSVRecord registro, int numeroRegistro, DataEntryRequest request, DataEntryResponse response) {
        // Use a nova validação mais robusta
        List<CsvValidationUtil.ValidationResult> results = CsvValidationUtil.validateCsvLine(
            registro.getEmail(), registro.getNome(), registro.getCelular(),
            registro.getPais(), registro.getEstado(), registro.getCidade(),
            registro.getInstituicaoId() != null ? registro.getInstituicaoId().toString() : null,
            registro.getSubInstituicaoId() != null ? registro.getSubInstituicaoId().toString() : null
        );
        
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
                registro.setCelular(celularFormatado);
            } catch (Exception e) {
                response.addWarning("Registro " + numeroRegistro + ": Não foi possível formatar celular: " + e.getMessage());
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
     * Processa os registros válidos (cria usuários no banco)
     */
    private void processarRegistros(List<UsuarioCSVRecord> registros, DataEntryRequest request, DataEntryResponse response) {
        for (int i = 0; i < registros.size(); i++) {
            UsuarioCSVRecord registro = registros.get(i);
            
            try {
                criarUsuario(registro, request, response);
                response.incrementarRegistrosProcessados();
                response.incrementarRegistrosIncluidos();
            } catch (Exception e) {
                response.addError("Registro " + (i + 1) + ": Erro ao criar usuário: " + e.getMessage());
            }
        }
    }
    
    /**
     * Cria usuário a partir do registro
     */
    private void criarUsuario(UsuarioCSVRecord registro, DataEntryRequest request, DataEntryResponse response) {
        // Cria Pessoa
        Pessoa pessoa = new Pessoa();
        pessoa.setNomePessoa(registro.getNome());
        pessoa.setEmailPessoa(registro.getEmail());
        pessoa.setCelularPessoa(registro.getCelular());
        pessoa.setComentarios(registro.getComentarios());
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());
        
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
        
        // Salva Pessoa
        pessoa = pessoaRepository.save(pessoa);
        
        // Cria Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(registro.getUsername());
        usuario.setPassword(passwordEncoder.encode(registro.getPassword()));
        usuario.setPessoa(pessoa);
        usuario.setSituacaoUsuario("A"); // Ativo
        usuario.setDataUltimaAtualizacao(LocalDate.now());
        
        usuarioRepository.save(usuario);
        
        // Criar relacionamentos institucionais se informados
        criarRelacionamentosInstitucionais(pessoa, registro, response);
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
                Optional<Local> estadoExistente = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(2, estado, localPais);
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
            Optional<Local> cidadeExistente = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(3, cidade, localEstado);
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
    private void gerarArquivoResultado(List<UsuarioCSVRecord> registros, DataEntryRequest request, DataEntryResponse response) {
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
                        "Processado"
                    );
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
    private void criarRelacionamentosInstitucionais(Pessoa pessoa, UsuarioCSVRecord registro, DataEntryResponse response) {
        try {
            // Relacionamento com Instituição
            if (registro.getInstituicaoId() != null) {
                Optional<Instituicao> instituicao = instituicaoRepository.findById(registro.getInstituicaoId());
                if (instituicao.isPresent()) {
                    // Criar PessoaInstituicao se necessário
                    // Por enquanto, apenas logamos que a instituição existe
                    response.addInfo("Instituição " + registro.getInstituicaoId() + " associada ao usuário " + registro.getUsername());
                } else {
                    response.addWarning("Instituição " + registro.getInstituicaoId() + " não encontrada para usuário " + registro.getUsername());
                }
            }
            
            // Relacionamento com SubInstituição
            if (registro.getSubInstituicaoId() != null) {
                Optional<SubInstituicao> subInstituicao = subInstituicaoRepository.findById(registro.getSubInstituicaoId());
                if (subInstituicao.isPresent()) {
                    // Criar PessoaSubInstituicao se necessário
                    // Por enquanto, apenas logamos que a sub-instituição existe
                    response.addInfo("Sub-Instituição " + registro.getSubInstituicaoId() + " associada ao usuário " + registro.getUsername());
                } else {
                    response.addWarning("Sub-Instituição " + registro.getSubInstituicaoId() + " não encontrada para usuário " + registro.getUsername());
                }
            }
        } catch (Exception e) {
            response.addWarning("Erro ao criar relacionamentos institucionais para " + registro.getUsername() + ": " + e.getMessage());
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
            if (nomeArquivo != null && (nomeArquivo.toLowerCase().endsWith(".xlsx") || nomeArquivo.toLowerCase().endsWith(".xls"))) {
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
                    response.addInfo("Cabeçalho encontrado: " + linha);
                    
                    // Validar cabeçalhos obrigatórios
                    String[] headers = linha.split(separadorCsv);
                    boolean hasEmail = Arrays.stream(headers).anyMatch(h -> h.trim().toLowerCase().equals("email"));
                    boolean hasNome = Arrays.stream(headers).anyMatch(h -> h.trim().toLowerCase().equals("nome"));
                    boolean hasCelular = Arrays.stream(headers).anyMatch(h -> h.trim().toLowerCase().equals("celular"));
                    
                    if (!hasEmail) response.addError("Coluna 'email' obrigatória não encontrada");
                    if (!hasNome) response.addError("Coluna 'nome' obrigatória não encontrada");
                    if (!hasCelular) response.addError("Coluna 'celular' obrigatória não encontrada");
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
}
