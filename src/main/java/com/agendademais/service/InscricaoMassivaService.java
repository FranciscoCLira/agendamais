package com.agendademais.service;

import com.agendademais.dto.*;
import com.agendademais.entities.*;
import com.agendademais.repositories.*;
import com.agendademais.util.ExcelToCsvUtil;
import com.agendademais.util.PhoneNumberUtil;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Serviço para processamento de carga massiva de inscrições em tipos de atividade
 * Lê planilha Excel do Microsoft Forms (colunas G-O)
 */
@Service
public class InscricaoMassivaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private SubInstituicaoRepository subInstituicaoRepository;

    @Autowired
    private TipoAtividadeRepository tipoAtividadeRepository;

    @Autowired
    private PessoaInstituicaoRepository pessoaInstituicaoRepository;

    @Autowired
    private PessoaSubInstituicaoRepository pessoaSubInstituicaoRepository;

    @Autowired
    private UsuarioInstituicaoRepository usuarioInstituicaoRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    @Autowired
    private InscricaoTipoAtividadeRepository inscricaoTipoAtividadeRepository;

    @Autowired
    private LocalRepository localRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Valida arquivo sem processar (apenas retorna estatísticas e erros)
     */
    public InscricaoMassivaResponse validarArquivo(InscricaoMassivaRequest request, Long instituicaoId) {
        InscricaoMassivaResponse response = new InscricaoMassivaResponse();
        response.setInicioProcessamento(LocalDateTime.now());

        try {
            // Validações iniciais
            if (!validarRequest(request, instituicaoId, response)) {
                return response;
            }

            // Verifica se entidades existem
            if (!subInstituicaoRepository.existsById(request.getSubInstituicaoId())) {
                response.addError("SubInstituição não encontrada");
                return response;
            }
            
            if (!tipoAtividadeRepository.existsById(request.getTipoAtividadeId())) {
                response.addError("Tipo de Atividade não encontrado");
                return response;
            }

            // Lê registros do Excel
            List<InscricaoFormsRecord> registros = lerRegistrosExcel(request.getArquivo(), response);
            if (registros.isEmpty()) {
                response.addError("Nenhum registro válido encontrado no arquivo");
                return response;
            }

            response.setTotalRegistros(registros.size());

            // Valida registros (sem processar)
            validarRegistros(registros, response);

            // Conta registros válidos
            long registrosValidos = registros.stream().filter(InscricaoFormsRecord::isValido).count();
            response.setRegistrosProcessados((int) registrosValidos);
            
            response.setFimProcessamento(LocalDateTime.now());
            
            System.out.println("=== VALIDAÇÃO CONCLUÍDA ===");
            System.out.println("Total de registros: " + response.getTotalRegistros());
            System.out.println("Registros válidos: " + registrosValidos);
            System.out.println("Erros encontrados: " + response.getErrors().size());

        } catch (Exception e) {
            System.err.println("Erro na validação: " + e.getMessage());
            e.printStackTrace();
            response.addError("Erro ao validar arquivo: " + e.getMessage());
        }

        return response;
    }

    /**
     * Processa carga massiva de inscrições
     */
    @Transactional
    public InscricaoMassivaResponse processarCargaInscricoes(InscricaoMassivaRequest request, Long instituicaoId) {
        InscricaoMassivaResponse response = new InscricaoMassivaResponse();
        response.setInicioProcessamento(LocalDateTime.now());
        
        // Gera ID único para este batch (para rastreamento de reversão)
        String batchId = UUID.randomUUID().toString();
        response.setBatchId(batchId);

        try {
            // Validações iniciais
            if (!validarRequest(request, instituicaoId, response)) {
                return response;
            }

            // Carrega entidades
            SubInstituicao subInstituicao = subInstituicaoRepository.findById(request.getSubInstituicaoId())
                    .orElseThrow(() -> new RuntimeException("SubInstituição não encontrada"));
            
            TipoAtividade tipoAtividade = tipoAtividadeRepository.findById(request.getTipoAtividadeId())
                    .orElseThrow(() -> new RuntimeException("Tipo de Atividade não encontrado"));
            
            Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                    .orElseThrow(() -> new RuntimeException("Instituição não encontrada"));

            // Lê registros do Excel
            List<InscricaoFormsRecord> registros = lerRegistrosExcel(request.getArquivo(), response);
            if (registros.isEmpty()) {
                response.addError("Nenhum registro válido encontrado no arquivo");
                return response;
            }

            response.setTotalRegistros(registros.size());

            // Valida registros
            validarRegistros(registros, response);

            // Processa registros válidos
            for (InscricaoFormsRecord registro : registros) {
                if (registro.isValido()) {
                    processarInscricao(registro, instituicao, subInstituicao, tipoAtividade, request, response);
                } else {
                    response.incrementarErros();
                }
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
    private boolean validarRequest(InscricaoMassivaRequest request, Long instituicaoId, 
                                   InscricaoMassivaResponse response) {
        if (request.getArquivo() == null || request.getArquivo().isEmpty()) {
            response.addError("Arquivo não informado");
            return false;
        }

        if (request.getSubInstituicaoId() == null) {
            response.addError("SubInstituição não informada");
            return false;
        }

        if (request.getTipoAtividadeId() == null) {
            response.addError("Tipo de Atividade não informado");
            return false;
        }

        if (instituicaoId == null) {
            response.addError("Instituição não informada");
            return false;
        }

        // Valida se SubInstituição pertence à Instituição
        Optional<SubInstituicao> subInst = subInstituicaoRepository.findById(request.getSubInstituicaoId());
        if (subInst.isEmpty()) {
            response.addError("SubInstituição não encontrada");
            return false;
        }
        if (!subInst.get().getInstituicao().getId().equals(instituicaoId)) {
            response.addError("SubInstituição não pertence à instituição logada");
            return false;
        }

        // Valida se TipoAtividade pertence à Instituição
        Optional<TipoAtividade> tipoAtv = tipoAtividadeRepository.findById(request.getTipoAtividadeId());
        if (tipoAtv.isEmpty()) {
            response.addError("Tipo de Atividade não encontrado");
            return false;
        }
        if (!tipoAtv.get().getInstituicao().getId().equals(instituicaoId)) {
            response.addError("Tipo de Atividade não pertence à instituição logada");
            return false;
        }

        return true;
    }

    /**
     * Lê apenas emails da coluna G para reversão
     */
    private List<String> lerEmailsParaReversao(MultipartFile arquivo, InscricaoMassivaResponse response) {
        List<String> emails = new ArrayList<>();

        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int linhaAtual = 0;

            for (Row row : sheet) {
                linhaAtual++;

                // Pula linha 1 (cabeçalho)
                if (linhaAtual == 1) {
                    continue;
                }

                // Pula linhas vazias
                Cell emailCell = row.getCell(6); // Coluna G
                if (emailCell == null) {
                    continue;
                }

                String email = getCellValue(emailCell);
                if (email != null && !email.trim().isEmpty()) {
                    emails.add(email.toLowerCase().trim());
                    System.out.println("Email lido linha " + linhaAtual + ": " + email);
                }
            }

        } catch (Exception e) {
            response.addError("Erro ao ler arquivo Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return emails;
    }

    /**
     * Lê registros do arquivo Excel (colunas G a O)
     */
    private List<InscricaoFormsRecord> lerRegistrosExcel(MultipartFile arquivo, InscricaoMassivaResponse response) {
        List<InscricaoFormsRecord> registros = new ArrayList<>();

        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int linhaAtual = 0;

            for (Row row : sheet) {
                linhaAtual++;

                // Pula linha 1 (cabeçalho)
                if (linhaAtual == 1) {
                    continue;
                }

                // Pula linhas vazias
                if (isLinhaVazia(row)) {
                    continue;
                }

                InscricaoFormsRecord registro = new InscricaoFormsRecord();
                registro.setLinha(linhaAtual);

                // Lê colunas G a O (índices 6 a 14)
                registro.setEmail(getCellValue(row.getCell(6)));              // G
                registro.setNome(getCellValue(row.getCell(7)));               // H
                registro.setCelular(getCellValue(row.getCell(8)));            // I
                registro.setIdentificacaoPessoaInstituicao(getCellValue(row.getCell(9)));  // J
                registro.setIdentificacaoPessoaSubInstituicao(getCellValue(row.getCell(10))); // K
                registro.setCidade(getCellValue(row.getCell(11)));            // L
                registro.setEstado(getCellValue(row.getCell(12)));            // M
                registro.setPais(getCellValue(row.getCell(13)));              // N
                registro.setComentarios(getCellValue(row.getCell(14)));       // O

                registros.add(registro);
            }

        } catch (Exception e) {
            response.addError("Erro ao ler arquivo Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return registros;
    }

    /**
     * Verifica se linha está vazia
     */
    private boolean isLinhaVazia(Row row) {
        if (row == null) return true;
        
        // Verifica colunas G, H, I (email, nome, celular)
        Cell cellG = row.getCell(6);
        Cell cellH = row.getCell(7);
        Cell cellI = row.getCell(8);
        
        return (cellG == null || cellG.getCellType() == CellType.BLANK || getCellValue(cellG).trim().isEmpty()) &&
               (cellH == null || cellH.getCellType() == CellType.BLANK || getCellValue(cellH).trim().isEmpty()) &&
               (cellI == null || cellI.getCellType() == CellType.BLANK || getCellValue(cellI).trim().isEmpty());
    }

    /**
     * Extrai valor da célula como String
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                } else {
                    // Converte número para string sem notação científica
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Valida todos os registros
     */
    private void validarRegistros(List<InscricaoFormsRecord> registros, InscricaoMassivaResponse response) {
        for (InscricaoFormsRecord registro : registros) {
            StringBuilder erros = new StringBuilder();
            StringBuilder avisos = new StringBuilder();

            // Email obrigatório e válido
            if (registro.getEmail() == null || registro.getEmail().trim().isEmpty()) {
                erros.append("Email obrigatório. ");
            } else if (!registro.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                erros.append("Email inválido. ");
            }

            // Nome obrigatório (máx 255)
            if (registro.getNome() == null || registro.getNome().trim().isEmpty()) {
                erros.append("Nome obrigatório. ");
            } else if (registro.getNome().length() > 255) {
                avisos.append("Nome será truncado (máx 255 caracteres). ");
                registro.setNome(registro.getNome().substring(0, 255));
            }

            // Celular obrigatório (máx 20)
            if (registro.getCelular() == null || registro.getCelular().trim().isEmpty()) {
                erros.append("Celular obrigatório. ");
            } else if (registro.getCelular().length() > 20) {
                avisos.append("Celular será truncado (máx 20 caracteres). ");
                registro.setCelular(registro.getCelular().substring(0, 20));
            }

            // Comentários (máx 255)
            if (registro.getComentarios() != null && registro.getComentarios().length() > 255) {
                avisos.append("Comentários serão truncados (máx 255 caracteres). ");
                registro.setComentarios(registro.getComentarios().substring(0, 255));
            }

            // Valida tamanho dos campos de Local
            if (registro.getCidade() != null && registro.getCidade().length() > 100) {
                avisos.append("Cidade será truncada (máx 100 caracteres). ");
                registro.setCidade(registro.getCidade().substring(0, 100));
            }
            if (registro.getEstado() != null && registro.getEstado().length() > 100) {
                avisos.append("Estado será truncado (máx 100 caracteres). ");
                registro.setEstado(registro.getEstado().substring(0, 100));
            }
            if (registro.getPais() != null && registro.getPais().length() > 100) {
                avisos.append("País será truncado (máx 100 caracteres). ");
                registro.setPais(registro.getPais().substring(0, 100));
            }

            // Valida Estado se País for Brasil
            if (registro.getPais() != null && !registro.getPais().trim().isEmpty()) {
                String pais = registro.getPais().trim();
                if (pais.equalsIgnoreCase("Brasil") || pais.equalsIgnoreCase("Brazil")) {
                    if (registro.getEstado() != null && !registro.getEstado().trim().isEmpty()) {
                        String estado = registro.getEstado().trim();
                        // Valida se estado brasileiro existe
                        if (!validarEstadoBrasileiro(estado)) {
                            erros.append("Estado brasileiro inválido ou com sigla (use nome completo, ex: 'São Paulo' em vez de 'SP'). ");
                        }
                    }
                }
            }

            if (erros.length() > 0) {
                registro.setMensagemErro("Linha " + registro.getLinha() + ": " + erros.toString());
                response.addWarning(registro.getMensagemErro());
            } else if (avisos.length() > 0) {
                response.addWarning("Linha " + registro.getLinha() + ": " + avisos.toString());
            }
        }
    }

    /**
     * Valida se Estado brasileiro existe na base (todos os 27 estados devem estar cadastrados)
     */
    private boolean validarEstadoBrasileiro(String nomeEstado) {
        // Busca país Brasil
        Optional<Local> brasilOpt = localRepository.findByTipoLocalAndNomeLocal(1, "Brasil");
        if (brasilOpt.isEmpty()) {
            return false; // Brasil não cadastrado
        }

        // Busca estado dentro do Brasil
        Optional<Local> estadoOpt = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(2, nomeEstado, brasilOpt.get());
        return estadoOpt.isPresent();
    }

    /**
     * Processa uma inscrição individual
     */
    @Transactional
    private void processarInscricao(InscricaoFormsRecord registro, Instituicao instituicao, 
                                   SubInstituicao subInstituicao, TipoAtividade tipoAtividade,
                                   InscricaoMassivaRequest request, InscricaoMassivaResponse response) {
        try {
            String email = registro.getEmail().toLowerCase().trim();
            System.out.println("\n=== Processando inscrição: " + email + " ===");

            // 1. Busca ou cria Pessoa
            Pessoa pessoa = pessoaRepository.findByEmailPessoa(email)
                    .orElseGet(() -> {
                        System.out.println("→ Criando nova Pessoa...");
                        Pessoa p = criarPessoa(registro);
                        System.out.println("✓ Pessoa criada: ID=" + p.getId());
                        response.addPessoaId(p.getId());
                        return p;
                    });

            // 2. Busca ou cria Usuario
            Usuario usuario = usuarioRepository.findByEmailPessoa(email)
                    .orElseGet(() -> {
                        System.out.println("→ Criando novo Usuario...");
                        Usuario u = criarUsuario(pessoa, registro);
                        System.out.println("✓ Usuario criado: ID=" + u.getId() + ", username=" + u.getUsername());
                        response.addUsuarioId(u.getId());
                        return u;
                    });

            // 3. Busca ou cria PessoaInstituicao
            pessoaInstituicaoRepository
                    .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando PessoaInstituicao...");
                        return criarPessoaInstituicao(pessoa, instituicao, registro);
                    });

            // 4. Busca ou cria PessoaSubInstituicao
            pessoaSubInstituicaoRepository
                    .findByPessoaIdAndSubInstituicaoId(pessoa.getId(), subInstituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando PessoaSubInstituicao...");
                        PessoaSubInstituicao psi = criarPessoaSubInstituicao(pessoa, instituicao, subInstituicao, registro);
                        System.out.println("✓ PessoaSubInstituicao criada: ID=" + psi.getId());
                        return psi;
                    });

            // 5. Busca ou cria UsuarioInstituicao
            usuarioInstituicaoRepository
                    .findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando UsuarioInstituicao...");
                        UsuarioInstituicao ui = criarUsuarioInstituicao(usuario, instituicao);
                        System.out.println("✓ UsuarioInstituicao criada: ID=" + ui.getId());
                        return ui;
                    });

            // 6. Busca ou cria Inscricao
            Inscricao inscricao = inscricaoRepository
                    .findByPessoaIdAndIdInstituicaoId(pessoa.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando Inscricao...");
                        Inscricao i = criarInscricao(pessoa, instituicao);
                        System.out.println("✓ Inscricao criada: ID=" + i.getId());
                        response.addInscricaoId(i.getId());
                        return i;
                    });

            // 7. Verifica se já existe InscricaoTipoAtividade
            Optional<InscricaoTipoAtividade> inscricaoTipoAtividadeExistente = 
                    inscricaoTipoAtividadeRepository.findByInscricaoIdAndTipoAtividadeId(
                            inscricao.getId(), tipoAtividade.getId());

            if (inscricaoTipoAtividadeExistente.isPresent()) {
                System.out.println("⚠ InscricaoTipoAtividade já existe");
                registro.setMensagemSucesso("Linha " + registro.getLinha() + ": Email " + email + 
                        " já está inscrito neste tipo de atividade.");
                response.incrementarExistentes();
            } else {
                System.out.println("→ Criando InscricaoTipoAtividade...");
                InscricaoTipoAtividade ita = criarInscricaoTipoAtividade(inscricao, tipoAtividade);
                System.out.println("✓ InscricaoTipoAtividade criada: ID=" + ita.getId());
                registro.setMensagemSucesso("Linha " + registro.getLinha() + ": Inscrição criada com sucesso para " + email);
                response.incrementarNovas();
            }

            response.incrementarProcessados();
            System.out.println("=== Inscrição processada com sucesso ===\n");

        } catch (Exception e) {
            System.err.println("✗ ERRO ao processar linha " + registro.getLinha() + ": " + e.getMessage());
            e.printStackTrace();
            registro.setMensagemErro("Linha " + registro.getLinha() + ": Erro ao processar - " + e.getMessage());
            response.addError(registro.getMensagemErro());
            response.incrementarErros();
        }
    }

    /**
     * Cria nova Pessoa (com Locais hierárquicos)
     */
    private Pessoa criarPessoa(InscricaoFormsRecord registro) {
        Pessoa pessoa = new Pessoa();
        pessoa.setNomePessoa(registro.getNome());
        pessoa.setEmailPessoa(registro.getEmail().toLowerCase().trim());
        pessoa.setCelularPessoa(registro.getCelular());
        pessoa.setSituacaoPessoa("A"); // Ativa
        pessoa.setComentarios(registro.getComentarios());
        pessoa.setDataInclusao(LocalDate.now());
        pessoa.setDataUltimaAtualizacao(LocalDate.now());

        // Busca ou cria Locais hierárquicos (Pais -> Estado -> Cidade)
        String nomePais = registro.getPais();
        String nomeEstado = registro.getEstado();
        String nomeCidade = registro.getCidade();

        if (nomePais != null || nomeEstado != null || nomeCidade != null) {
            Local cidade = buscarOuCriarLocalHierarquico(nomePais, nomeEstado, nomeCidade);
            
            // Define Pais, Estado e Cidade na Pessoa
            if (cidade != null) {
                if (cidade.getTipoLocal() == 3) { // Cidade
                    pessoa.setCidade(cidade);
                    if (cidade.getLocalPai() != null && cidade.getLocalPai().getTipoLocal() == 2) { // Estado
                        pessoa.setEstado(cidade.getLocalPai());
                        if (cidade.getLocalPai().getLocalPai() != null) { // País
                            pessoa.setPais(cidade.getLocalPai().getLocalPai());
                        }
                    } else if (cidade.getLocalPai() != null && cidade.getLocalPai().getTipoLocal() == 1) { // País direto
                        pessoa.setPais(cidade.getLocalPai());
                    }
                } else if (cidade.getTipoLocal() == 2) { // Estado
                    pessoa.setEstado(cidade);
                    if (cidade.getLocalPai() != null) {
                        pessoa.setPais(cidade.getLocalPai());
                    }
                } else if (cidade.getTipoLocal() == 1) { // País
                    pessoa.setPais(cidade);
                }
            }
        }

        return pessoaRepository.save(pessoa);
    }

    /**
     * Cria novo Usuario com senha padrão inicial
     */
    private Usuario criarUsuario(Pessoa pessoa, InscricaoFormsRecord registro) {
        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);
        
        // Gera username a partir do email (parte antes do @)
        String username = pessoa.getEmailPessoa().split("@")[0];
        usuario.setUsername(username);
        
        // Senha padrão inicial: Agenda@2025 (obriga troca no primeiro acesso)
        String senhaInicial = "Agenda@2025";
        usuario.setPassword(passwordEncoder.encode(senhaInicial));
        
        usuario.setSituacaoUsuario("P"); // P=Pendente (obriga troca de senha)
        usuario.setDataUltimaAtualizacao(LocalDate.now());
        
        registro.setUsuarioGerado(username);
        registro.setSenhaGerada(senhaInicial);
        
        return usuarioRepository.save(usuario);
    }

    /**
     * Cria PessoaInstituicao
     */
    private PessoaInstituicao criarPessoaInstituicao(Pessoa pessoa, Instituicao instituicao, 
                                                     InscricaoFormsRecord registro) {
        PessoaInstituicao pessoaInstituicao = new PessoaInstituicao();
        pessoaInstituicao.setPessoa(pessoa);
        pessoaInstituicao.setInstituicao(instituicao);
        
        String identificacao = registro.getIdentificacaoPessoaInstituicao();
        if (identificacao != null && !identificacao.trim().isEmpty()) {
            pessoaInstituicao.setIdentificacaoPessoaInstituicao(identificacao.trim());
        }
        
        pessoaInstituicao.setDataAfiliacao(null); // Não vem do Forms
        pessoaInstituicao.setDataUltimaAtualizacao(LocalDate.now());
        
        PessoaInstituicao saved = pessoaInstituicaoRepository.save(pessoaInstituicao);
        System.out.println("✓ PessoaInstituicao criada: ID=" + saved.getId() + ", Pessoa=" + pessoa.getId() + ", Instituicao=" + instituicao.getId());
        return saved;
    }

    /**
     * Cria PessoaSubInstituicao
     */
    private PessoaSubInstituicao criarPessoaSubInstituicao(Pessoa pessoa, Instituicao instituicao, 
                                                           SubInstituicao subInstituicao, 
                                                           InscricaoFormsRecord registro) {
        PessoaSubInstituicao pessoaSubInstituicao = new PessoaSubInstituicao();
        pessoaSubInstituicao.setPessoa(pessoa);
        pessoaSubInstituicao.setInstituicao(instituicao);
        pessoaSubInstituicao.setSubInstituicao(subInstituicao);
        pessoaSubInstituicao.setIdentificacaoPessoaSubInstituicao(registro.getIdentificacaoPessoaSubInstituicao());
        pessoaSubInstituicao.setDataAfiliacao(null); // Não vem do Forms
        pessoaSubInstituicao.setDataUltimaAtualizacao(LocalDate.now());
        
        return pessoaSubInstituicaoRepository.save(pessoaSubInstituicao);
    }

    /**
     * Cria UsuarioInstituicao
     */
    private UsuarioInstituicao criarUsuarioInstituicao(Usuario usuario, Instituicao instituicao) {
        UsuarioInstituicao usuarioInstituicao = new UsuarioInstituicao();
        usuarioInstituicao.setUsuario(usuario);
        usuarioInstituicao.setInstituicao(instituicao);
        usuarioInstituicao.setNivelAcessoUsuarioInstituicao(1); // Participante
        usuarioInstituicao.setSitAcessoUsuarioInstituicao("A"); // Ativo
        
        return usuarioInstituicaoRepository.save(usuarioInstituicao);
    }

    /**
     * Cria Inscricao
     */
    private Inscricao criarInscricao(Pessoa pessoa, Instituicao instituicao) {
        Inscricao inscricao = new Inscricao();
        inscricao.setPessoa(pessoa);
        inscricao.setIdInstituicao(instituicao);
        inscricao.setDataInclusao(LocalDate.now());
        inscricao.setDataUltimaAtualizacao(LocalDate.now());
        
        return inscricaoRepository.save(inscricao);
    }

    /**
     * Cria InscricaoTipoAtividade
     */
    private InscricaoTipoAtividade criarInscricaoTipoAtividade(Inscricao inscricao, TipoAtividade tipoAtividade) {
        InscricaoTipoAtividade inscricaoTipoAtividade = new InscricaoTipoAtividade();
        inscricaoTipoAtividade.setInscricao(inscricao);
        inscricaoTipoAtividade.setTipoAtividade(tipoAtividade);
        
        return inscricaoTipoAtividadeRepository.save(inscricaoTipoAtividade);
    }

    /**
     * Busca ou cria Local hierárquico (País -> Estado -> Cidade)
     * Normaliza nomes removendo espaços iniciais/finais
     * 
     * @param nomePais Nome do país
     * @param nomeEstado Nome do estado
     * @param nomeCidade Nome da cidade
     * @return Local da cidade (tipo 3) ou null se nenhum local informado
     */
    private Local buscarOuCriarLocalHierarquico(String nomePais, String nomeEstado, String nomeCidade) {
        // Normaliza nomes (trim)
        nomePais = normalizarNome(nomePais);
        nomeEstado = normalizarNome(nomeEstado);
        nomeCidade = normalizarNome(nomeCidade);

        // Se nenhum local informado, retorna null
        if (nomePais == null && nomeEstado == null && nomeCidade == null) {
            return null;
        }

        Local pais = null;
        Local estado = null;
        Local cidade = null;

        // 1. Busca ou cria País (tipo 1)
        if (nomePais != null) {
            Optional<Local> paisOpt = localRepository.findByTipoLocalAndNomeLocal(1, nomePais);
            if (paisOpt.isPresent()) {
                pais = paisOpt.get();
                System.out.println("✓ País encontrado: " + nomePais + " (ID=" + pais.getId() + ")");
            } else {
                pais = new Local(1, nomePais, null);
                pais = localRepository.save(pais);
                System.out.println("→ País criado: " + nomePais + " (ID=" + pais.getId() + ")");
            }
        }

        // 2. Busca ou cria Estado (tipo 2)
        if (nomeEstado != null) {
            if (pais != null) {
                // Busca estado dentro do país
                Optional<Local> estadoOpt = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(2, nomeEstado, pais);
                if (estadoOpt.isPresent()) {
                    estado = estadoOpt.get();
                    System.out.println("✓ Estado encontrado: " + nomeEstado + " (ID=" + estado.getId() + ")");
                } else {
                    // Cria novo estado
                    estado = new Local(2, nomeEstado, pais);
                    estado = localRepository.save(estado);
                    System.out.println("→ Estado criado: " + nomeEstado + " em " + nomePais + " (ID=" + estado.getId() + ")");
                }
            } else {
                // Estado sem país definido - busca ou cria sem pai
                Optional<Local> estadoOpt = localRepository.findByTipoLocalAndNomeLocal(2, nomeEstado);
                if (estadoOpt.isPresent()) {
                    estado = estadoOpt.get();
                    System.out.println("✓ Estado encontrado (sem país): " + nomeEstado + " (ID=" + estado.getId() + ")");
                } else {
                    estado = new Local(2, nomeEstado, null);
                    estado = localRepository.save(estado);
                    System.out.println("→ Estado criado (sem país): " + nomeEstado + " (ID=" + estado.getId() + ")");
                }
            }
        }

        // 3. Busca ou cria Cidade (tipo 3)
        if (nomeCidade != null) {
            Local cidadePai = estado != null ? estado : pais;
            
            if (cidadePai != null) {
                // Busca cidade dentro do estado/país
                Optional<Local> cidadeOpt = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(3, nomeCidade, cidadePai);
                if (cidadeOpt.isPresent()) {
                    cidade = cidadeOpt.get();
                    System.out.println("✓ Cidade encontrada: " + nomeCidade + " (ID=" + cidade.getId() + ")");
                } else {
                    // Cria nova cidade
                    cidade = new Local(3, nomeCidade, cidadePai);
                    cidade = localRepository.save(cidade);
                    System.out.println("→ Cidade criada: " + nomeCidade + " em " + (estado != null ? estado.getNomeLocal() : pais.getNomeLocal()) + " (ID=" + cidade.getId() + ")");
                }
            } else {
                // Cidade sem estado/país - busca ou cria sem pai
                Optional<Local> cidadeOpt = localRepository.findByTipoLocalAndNomeLocal(3, nomeCidade);
                if (cidadeOpt.isPresent()) {
                    cidade = cidadeOpt.get();
                    System.out.println("✓ Cidade encontrada (sem estado/país): " + nomeCidade + " (ID=" + cidade.getId() + ")");
                } else {
                    cidade = new Local(3, nomeCidade, null);
                    cidade = localRepository.save(cidade);
                    System.out.println("→ Cidade criada (sem estado/país): " + nomeCidade + " (ID=" + cidade.getId() + ")");
                }
            }
        }

        // Retorna a cidade (nível mais específico) ou estado ou país
        return cidade != null ? cidade : (estado != null ? estado : pais);
    }

    /**
     * Normaliza nome de local (trim e retorna null se vazio)
     */
    private String normalizarNome(String nome) {
        if (nome == null) return null;
        nome = nome.trim();
        return nome.isEmpty() ? null : nome;
    }

    /**
     * Gera senha aleatória
     */
    private String gerarSenhaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        Random random = new Random();
        StringBuilder senha = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            senha.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        
        return senha.toString();
    }

    /**
     * Reverte uma carga massiva baseada em arquivo Excel
     * Lê os emails da planilha e deleta relacionamentos específicos da SubInstituição/TipoAtividade
     * 
     * REGRAS:
     * - Deleta InscricaoTipoAtividade apenas para o TipoAtividade especificado
     * - Deleta PessoaSubInstituicao apenas da SubInstituição especificada
     * - Deleta Pessoa/Usuario APENAS se não houver outros relacionamentos com outras instituições
     * - NÃO exclui entidades Local
     * 
     * @param arquivo Planilha Excel com coluna de emails (mesma estrutura da carga)
     * @param subInstituicaoId SubInstituição para filtrar exclusões
     * @param tipoAtividadeId TipoAtividade para filtrar exclusões
     * @param instituicaoId Instituição atual
     * @return Response com resultado da reversão
     */
    @Transactional
    public InscricaoMassivaResponse reverterCargaPorArquivo(MultipartFile arquivo,
                                                            Long subInstituicaoId,
                                                            Long tipoAtividadeId,
                                                            Long instituicaoId) {
        InscricaoMassivaResponse response = new InscricaoMassivaResponse();
        response.setInicioProcessamento(LocalDateTime.now());
        
        final int[] totalDeletados = {0};
        final int[] emailsProcessados = {0};
        final int[] emailsNaoEncontrados = {0};
        final int[] pessoasNaoDeletadas = {0}; // Quando tem outros relacionamentos
        
        try {
            System.out.println("=== INICIANDO REVERSÃO DE CARGA POR ARQUIVO ===");
            
            // Valida entidades
            SubInstituicao subInstituicao = subInstituicaoRepository.findById(subInstituicaoId)
                    .orElseThrow(() -> new RuntimeException("SubInstituição não encontrada"));
            
            TipoAtividade tipoAtividade = tipoAtividadeRepository.findById(tipoAtividadeId)
                    .orElseThrow(() -> new RuntimeException("Tipo de Atividade não encontrado"));
            
            Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                    .orElseThrow(() -> new RuntimeException("Instituição não encontrada"));
            
            // Lê apenas emails da coluna G do arquivo Excel
            List<String> emails = lerEmailsParaReversao(arquivo, response);
            if (emails.isEmpty()) {
                response.addError("Nenhum email encontrado no arquivo (coluna G - Email)");
                return response;
            }
            
            System.out.println("Total de emails no arquivo: " + emails.size());
            
            // Processa cada email
            for (String email : emails) {
                emailsProcessados[0]++;
                
                System.out.println("\n--- Processando email " + emailsProcessados[0] + "/" + emails.size() + ": " + email + " ---");
                
                // Busca Pessoa pelo email
                Optional<Pessoa> pessoaOpt = pessoaRepository.findByEmailPessoa(email);
                if (pessoaOpt.isEmpty()) {
                    System.out.println("⚠ Email não encontrado na base: " + email);
                    response.addWarning("Email não encontrado - " + email);
                    emailsNaoEncontrados[0]++;
                    continue;
                }
                
                Pessoa pessoa = pessoaOpt.get();
                System.out.println("✓ Pessoa encontrada: ID=" + pessoa.getId());
                
                // Busca Usuario relacionado
                Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailPessoa(email);
                
                // 1. Deleta InscricaoTipoAtividade específica
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    Optional<Inscricao> inscricaoOpt = inscricaoRepository
                            .findByPessoaIdAndIdInstituicaoId(pessoa.getId(), instituicao.getId());
                    
                    if (inscricaoOpt.isPresent()) {
                        Inscricao inscricao = inscricaoOpt.get();
                        Optional<InscricaoTipoAtividade> itaOpt = inscricaoTipoAtividadeRepository
                                .findByInscricaoIdAndTipoAtividadeId(inscricao.getId(), tipoAtividade.getId());
                        
                        if (itaOpt.isPresent()) {
                            inscricaoTipoAtividadeRepository.delete(itaOpt.get());
                            totalDeletados[0]++;
                            System.out.println("✓ InscricaoTipoAtividade deletada");
                        }
                        
                        // Verifica se Inscricao não tem mais InscricaoTipoAtividade
                        List<InscricaoTipoAtividade> outrasItas = inscricaoTipoAtividadeRepository
                                .findByInscricaoId(inscricao.getId());
                        if (outrasItas.isEmpty()) {
                            inscricaoRepository.delete(inscricao);
                            totalDeletados[0]++;
                            System.out.println("✓ Inscricao deletada (sem mais tipos de atividade)");
                        }
                    }
                }
                
                // 2. Deleta PessoaSubInstituicao específica
                Optional<PessoaSubInstituicao> psiOpt = pessoaSubInstituicaoRepository
                        .findByPessoaIdAndSubInstituicaoId(pessoa.getId(), subInstituicao.getId());
                if (psiOpt.isPresent()) {
                    pessoaSubInstituicaoRepository.delete(psiOpt.get());
                    totalDeletados[0]++;
                    System.out.println("✓ PessoaSubInstituicao deletada");
                }
                
                // 3. Verifica se Pessoa tem outros relacionamentos antes de deletar
                List<PessoaSubInstituicao> outrasPsis = pessoaSubInstituicaoRepository.findByPessoaId(pessoa.getId());
                List<PessoaInstituicao> outrasPis = pessoaInstituicaoRepository.findByPessoaId(pessoa.getId());
                
                if (outrasPsis.isEmpty() && outrasPis.size() <= 1) {
                    // Não tem outros relacionamentos, pode deletar tudo
                    
                    // Deleta PessoaInstituicao
                    Optional<PessoaInstituicao> piOpt = pessoaInstituicaoRepository
                            .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId());
                    if (piOpt.isPresent()) {
                        pessoaInstituicaoRepository.delete(piOpt.get());
                        totalDeletados[0]++;
                        System.out.println("✓ PessoaInstituicao deletada");
                    }
                    
                    // Deleta UsuarioInstituicao e Usuario
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        List<UsuarioInstituicao> uis = usuarioInstituicaoRepository.findByUsuarioId(usuario.getId());
                        for (UsuarioInstituicao ui : uis) {
                            usuarioInstituicaoRepository.delete(ui);
                            totalDeletados[0]++;
                            System.out.println("✓ UsuarioInstituicao deletada");
                        }
                        
                        usuarioRepository.delete(usuario);
                        totalDeletados[0]++;
                        System.out.println("✓ Usuario deletado: " + usuario.getUsername());
                    }
                    
                    // Deleta Pessoa
                    pessoaRepository.delete(pessoa);
                    totalDeletados[0]++;
                    System.out.println("✓ Pessoa deletada: " + email);
                    
                    response.addWarning("Email " + email + " - Pessoa/Usuario deletados completamente");
                    
                } else {
                    // Tem outros relacionamentos, não deleta Pessoa/Usuario
                    pessoasNaoDeletadas[0]++;
                    System.out.println("⚠ Pessoa mantida (tem relacionamentos com outras instituições)");
                    response.addWarning("Email " + email + 
                            " - Relacionamentos deletados, mas Pessoa/Usuario mantidos (existem vínculos com outras instituições)");
                }
            }
            
            System.out.println("\n=== REVERSÃO CONCLUÍDA ===");
            System.out.println("Emails processados: " + emailsProcessados[0]);
            System.out.println("Emails não encontrados: " + emailsNaoEncontrados[0]);
            System.out.println("Pessoas não deletadas (com outros vínculos): " + pessoasNaoDeletadas[0]);
            System.out.println("Total de registros deletados: " + totalDeletados[0]);
            
            response.addWarning(String.format("Reversão concluída. Emails: %d processados, %d não encontrados. " +
                    "Registros deletados: %d. Pessoas mantidas (outros vínculos): %d",
                    emailsProcessados[0], emailsNaoEncontrados[0], totalDeletados[0], pessoasNaoDeletadas[0]));
            response.setTotalRegistros(emailsProcessados[0]);
            response.setRegistrosProcessados(emailsProcessados[0] - emailsNaoEncontrados[0]);
            
        } catch (Exception e) {
            System.err.println("✗ ERRO na reversão: " + e.getMessage());
            e.printStackTrace();
            response.addError("Erro durante reversão: " + e.getMessage());
        }
        
        response.setFimProcessamento(LocalDateTime.now());
        return response;
    }
}
