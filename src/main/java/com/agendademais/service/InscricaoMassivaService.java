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
 * Serviço para processamento de carga massiva de inscrições em tipos de
 * atividade
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
    private AutorRepository autorRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Valida arquivo sem processar (apenas retorna estatísticas e erros)
     */
    public InscricaoMassivaResponse validarArquivo(InscricaoMassivaRequest request, Long instituicaoId) {
        InscricaoMassivaResponse response = new InscricaoMassivaResponse();
        response.setInicioProcessamento(LocalDateTime.now());

        try {
            // Valida se não é a opção "TODOS OS TIPOS" (-1) que só funciona para reversão
            if (request.getTipoAtividadeId() != null && request.getTipoAtividadeId() == -1) {
                response.addError(
                        "A opção 'TODOS OS TIPOS' só funciona para 'Excluir/Reverter Carga', não para validação ou processamento");
                return response;
            }

            // Validações iniciais
            if (!validarRequest(request, instituicaoId, response)) {
                return response;
            }

            // Verifica se entidade existe
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
            // Valida se não é a opção "TODOS OS TIPOS" (-1) que só funciona para reversão
            if (request.getTipoAtividadeId() != null && request.getTipoAtividadeId() == -1) {
                response.addError(
                        "A opção 'TODOS OS TIPOS' só funciona para 'Excluir/Reverter Carga', não para processamento de carga");
                return response;
            }

            // Validações iniciais
            if (!validarRequest(request, instituicaoId, response)) {
                return response;
            }

            // Carrega entidades
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
                    processarInscricao(registro, instituicao, tipoAtividade, request, response);
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

        if (request.getTipoAtividadeId() == null) {
            response.addError("Tipo de Atividade não informado");
            return false;
        }

        if (instituicaoId == null) {
            response.addError("Instituição não informada");
            return false;
        }

        // Pula validação se for -1 (será validado no método reverterCargaPorArquivo)
        if (request.getTipoAtividadeId() == -1) {
            return true;
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

                // Lê coluna B - Data de inclusão (índice 1)
                Cell dataCell = row.getCell(1); // Coluna B
                if (dataCell != null) {
                    LocalDateTime dataInclusao = null;

                    // Tenta ler como data formatada primeiro
                    if (dataCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dataCell)) {
                        dataInclusao = dataCell.getLocalDateTimeCellValue();
                        System.out.println("📅 Data lida como NUMERIC/DATE: " + dataInclusao);
                    } else {
                        // Tenta como string (formato texto)
                        String dataStr = getCellValue(dataCell);
                        dataInclusao = parseDateTimeFromForms(dataStr);
                        System.out.println("📅 Data lida como STRING: " + dataStr + " -> " + dataInclusao);
                    }

                    if (dataInclusao != null) {
                        registro.setDataInclusaoForms(dataInclusao);
                    } else {
                        // Se não conseguir ler, usa data atual
                        System.out.println("⚠️ Não conseguiu ler data da coluna B, usando data atual");
                        registro.setDataInclusaoForms(LocalDateTime.now());
                    }
                } else {
                    // Se coluna B vazia, usa data atual
                    System.out.println("⚠️ Coluna B vazia, usando data atual");
                    registro.setDataInclusaoForms(LocalDateTime.now());
                }

                // Lê colunas G a O (índices 6 a 14)
                registro.setEmail(getCellValue(row.getCell(6))); // G
                registro.setNome(getCellValue(row.getCell(7))); // H
                registro.setCelular(getCellValue(row.getCell(8))); // I
                registro.setIdentificacaoPessoaInstituicao(getCellValue(row.getCell(9))); // J
                registro.setIdentificacaoPessoaSubInstituicao(getCellValue(row.getCell(10))); // K
                registro.setCidade(getCellValue(row.getCell(11))); // L
                registro.setEstado(getCellValue(row.getCell(12))); // M
                registro.setPais(getCellValue(row.getCell(13))); // N
                registro.setComentarios(getCellValue(row.getCell(14))); // O

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
        if (row == null)
            return true;

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
     * Converte string de data do Microsoft Forms para LocalDateTime
     * Formato esperado: "25/05/2025 06:10:41" ou "5/25/2025 6:10:41 AM"
     */
    private LocalDateTime parseDateTimeFromForms(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove espaços extras
            dateStr = dateStr.trim();

            // Formato brasileiro: dd/MM/yyyy HH:mm:ss
            java.time.format.DateTimeFormatter formatterBR = java.time.format.DateTimeFormatter
                    .ofPattern("d/M/yyyy HH:mm:ss");

            // Tenta com formato brasileiro
            try {
                return LocalDateTime.parse(dateStr, formatterBR);
            } catch (Exception e1) {
                // Tenta formato com zero à esquerda
                java.time.format.DateTimeFormatter formatterBR2 = java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm:ss");
                try {
                    return LocalDateTime.parse(dateStr, formatterBR2);
                } catch (Exception e2) {
                    // Tenta formato americano com AM/PM
                    java.time.format.DateTimeFormatter formatterUS = java.time.format.DateTimeFormatter
                            .ofPattern("M/d/yyyy h:mm:ss a", java.util.Locale.US);
                    return LocalDateTime.parse(dateStr, formatterUS);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Erro ao converter data do Forms: " + dateStr + " - " + e.getMessage());
            return null;
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
                            erros.append(
                                    "Estado brasileiro inválido ou com sigla (use nome completo, ex: 'São Paulo' em vez de 'SP'). ");
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
     * Valida se Estado brasileiro existe na base (todos os 27 estados devem estar
     * cadastrados)
     */
    private boolean validarEstadoBrasileiro(String nomeEstado) {
        // Busca país Brasil
        Optional<Local> brasilOpt = localRepository.findByTipoLocalAndNomeLocal(1, "Brasil");
        if (brasilOpt.isEmpty()) {
            return false; // Brasil não cadastrado
        }

        // Busca estado dentro do Brasil
        Optional<Local> estadoOpt = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(2, nomeEstado,
                brasilOpt.get());
        return estadoOpt.isPresent();
    }

    /**
     * Processa uma inscrição individual
     */
    @Transactional
    private void processarInscricao(InscricaoFormsRecord registro, Instituicao instituicao,
            TipoAtividade tipoAtividade,
            InscricaoMassivaRequest request, InscricaoMassivaResponse response) {
        try {
            String email = registro.getEmail().toLowerCase().trim();
            System.out.println("\n=== Processando inscrição: " + email + " ===");

            // 1. Busca ou cria Pessoa (se existir, atualiza os dados)
            Optional<Pessoa> pessoaExistente = pessoaRepository.findByEmailPessoa(email);
            Pessoa pessoa;
            boolean pessoaAtualizada = false;

            if (pessoaExistente.isPresent()) {
                System.out.println("→ Pessoa já existe - Atualizando dados...");
                pessoa = pessoaExistente.get();
                atualizarPessoa(pessoa, registro);
                System.out.println("✓ Pessoa atualizada: ID=" + pessoa.getId());
                pessoaAtualizada = true;
            } else {
                System.out.println("→ Criando nova Pessoa...");
                pessoa = criarPessoa(registro);
                System.out.println("✓ Pessoa criada: ID=" + pessoa.getId());
                response.addPessoaId(pessoa.getId());
            }

            // 2. Busca ou cria Usuario
            Usuario usuario = usuarioRepository.findByEmailPessoa(email)
                    .orElseGet(() -> {
                        System.out.println("→ Criando novo Usuario...");
                        Usuario u = criarUsuario(pessoa, registro);
                        System.out.println("✓ Usuario criado: ID=" + u.getId() + ", username=" + u.getUsername());
                        response.addUsuarioId(u.getId());
                        return u;
                    });

            // 3. Busca ou cria/atualiza PessoaInstituicao
            Optional<PessoaInstituicao> pessoaInstituicaoExistente = pessoaInstituicaoRepository
                    .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId());

            if (pessoaInstituicaoExistente.isPresent()) {
                System.out.println("→ PessoaInstituicao já existe - Atualizando...");
                PessoaInstituicao pi = pessoaInstituicaoExistente.get();
                atualizarPessoaInstituicao(pi, registro);
                System.out.println("✓ PessoaInstituicao atualizada: ID=" + pi.getId());
            } else {
                System.out.println("→ Criando PessoaInstituicao...");
                criarPessoaInstituicao(pessoa, instituicao, registro);
            }

            // 4. Busca ou cria UsuarioInstituicao
            usuarioInstituicaoRepository
                    .findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando UsuarioInstituicao...");
                        UsuarioInstituicao ui = criarUsuarioInstituicao(usuario, instituicao);
                        System.out.println("✓ UsuarioInstituicao criada: ID=" + ui.getId());
                        return ui;
                    });

            // 5. Busca ou cria Inscricao
            Inscricao inscricao = inscricaoRepository
                    .findByPessoaIdAndIdInstituicaoId(pessoa.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando Inscricao...");
                        Inscricao i = criarInscricao(pessoa, instituicao, registro);
                        System.out.println("✓ Inscricao criada: ID=" + i.getId());
                        response.addInscricaoId(i.getId());
                        return i;
                    });

            // 6. Verifica se já existe InscricaoTipoAtividade
            Optional<InscricaoTipoAtividade> inscricaoTipoAtividadeExistente = inscricaoTipoAtividadeRepository
                    .findByInscricaoIdAndTipoAtividadeId(
                            inscricao.getId(), tipoAtividade.getId());

            if (inscricaoTipoAtividadeExistente.isPresent()) {
                System.out.println("⚠ InscricaoTipoAtividade já existe");
                if (pessoaAtualizada) {
                    registro.setMensagemSucesso("Linha " + registro.getLinha() + ": Email " + email +
                            " já inscrito - dados pessoais foram atualizados.");
                } else {
                    registro.setMensagemSucesso("Linha " + registro.getLinha() + ": Email " + email +
                            " já está inscrito neste tipo de atividade.");
                }
                response.incrementarExistentes();
            } else {
                System.out.println("→ Criando InscricaoTipoAtividade...");
                InscricaoTipoAtividade ita = criarInscricaoTipoAtividade(inscricao, tipoAtividade);
                System.out.println("✓ InscricaoTipoAtividade criada: ID=" + ita.getId());
                if (pessoaAtualizada) {
                    registro.setMensagemSucesso("Linha " + registro.getLinha() +
                            ": Inscrição criada e dados pessoais atualizados para " + email);
                } else {
                    registro.setMensagemSucesso("Linha " + registro.getLinha() +
                            ": Inscrição criada com sucesso para " + email);
                }
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

        // Usa data do Forms (coluna B) se disponível, senão usa data atual
        LocalDate dataInclusao = (registro.getDataInclusaoForms() != null)
                ? registro.getDataInclusaoForms().toLocalDate()
                : LocalDate.now();
        pessoa.setDataInclusao(dataInclusao);
        pessoa.setDataUltimaAtualizacao(dataInclusao);

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
                    } else if (cidade.getLocalPai() != null && cidade.getLocalPai().getTipoLocal() == 1) { // País
                                                                                                           // direto
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
     * Atualiza dados de uma Pessoa existente com dados da carga massiva
     */
    private void atualizarPessoa(Pessoa pessoa, InscricaoFormsRecord registro) {
        // Atualiza campos básicos
        pessoa.setNomePessoa(registro.getNome());
        pessoa.setCelularPessoa(registro.getCelular());

        // Atualiza comentários se fornecido
        if (registro.getComentarios() != null && !registro.getComentarios().trim().isEmpty()) {
            pessoa.setComentarios(registro.getComentarios());
        }

        // Usa data do Forms (coluna B) para dataUltimaAtualizacao se disponível
        LocalDate dataAtualizacao = (registro.getDataInclusaoForms() != null)
                ? registro.getDataInclusaoForms().toLocalDate()
                : LocalDate.now();
        pessoa.setDataUltimaAtualizacao(dataAtualizacao);

        // Atualiza Locais hierárquicos (Pais -> Estado -> Cidade)
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
                    } else if (cidade.getLocalPai() != null && cidade.getLocalPai().getTipoLocal() == 1) { // País
                                                                                                           // direto
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

        pessoaRepository.save(pessoa);
    }

    /**
     * Cria novo Usuario com senha padrão inicial
     */
    private Usuario criarUsuario(Pessoa pessoa, InscricaoFormsRecord registro) {
        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoa);

        // Gera username único a partir do email
        String username = gerarUsernameUnico(pessoa.getEmailPessoa());
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
     * Gera username único a partir do email, evitando duplicidades
     * 
     * Estratégia:
     * 1. Tenta: parte_antes_do_@ (ex: "testando" de testando@gmail.com)
     * 2. Se duplicado: parte_antes_do_@.provedor (ex: "testando.gmail")
     * 3. Se ainda duplicado: parte_antes_do_@.provedor.numero (ex:
     * "testando.gmail.2")
     * 
     * @param email Email completo
     * @return Username único
     */
    private String gerarUsernameUnico(String email) {
        String[] partes = email.split("@");
        String localPart = partes[0];
        String domain = partes.length > 1 ? partes[1] : "";

        // Extrai provedor (primeira parte do domínio)
        String provedor = domain.contains(".") ? domain.split("\\.")[0] : domain;

        // Tentativa 1: apenas local part
        String username = localPart;
        if (!usuarioRepository.existsByUsername(username)) {
            System.out.println("→ Username gerado: " + username);
            return username;
        }

        System.out.println("⚠ Username '" + username + "' já existe, tentando com provedor...");

        // Tentativa 2: local part + provedor
        username = localPart + "." + provedor;
        if (!usuarioRepository.existsByUsername(username)) {
            System.out.println("→ Username gerado: " + username);
            return username;
        }

        System.out.println("⚠ Username '" + username + "' já existe, adicionando número...");

        // Tentativa 3: local part + provedor + número sequencial
        int contador = 2;
        while (contador < 1000) { // Limite de segurança
            username = localPart + "." + provedor + "." + contador;
            if (!usuarioRepository.existsByUsername(username)) {
                System.out.println("→ Username gerado: " + username);
                return username;
            }
            contador++;
        }

        // Fallback final (improvável): usa email completo com timestamp
        username = email.replace("@", ".").replace(".", "_") + "_" + System.currentTimeMillis();
        System.out.println("⚠ Fallback: Username gerado: " + username);
        return username;
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

        String pessoaAfiliada = registro.getIdentificacaoPessoaSubInstituicao();
        if (pessoaAfiliada != null && !pessoaAfiliada.trim().isEmpty()) {
            pessoaInstituicao.setIndicaPessoaAfiliadaInstituicao(pessoaAfiliada.trim());
        }

        pessoaInstituicao.setDataAfiliacao(null); // Não vem do Forms
        pessoaInstituicao.setDataUltimaAtualizacao(LocalDate.now());

        PessoaInstituicao saved = pessoaInstituicaoRepository.save(pessoaInstituicao);
        System.out.println("✓ PessoaInstituicao criada: ID=" + saved.getId() + ", Pessoa=" + pessoa.getId()
                + ", Instituicao=" + instituicao.getId());
        return saved;
    }

    /**
     * Atualiza PessoaInstituicao existente
     */
    private void atualizarPessoaInstituicao(PessoaInstituicao pessoaInstituicao, InscricaoFormsRecord registro) {
        String identificacao = registro.getIdentificacaoPessoaInstituicao();
        if (identificacao != null && !identificacao.trim().isEmpty()) {
            pessoaInstituicao.setIdentificacaoPessoaInstituicao(identificacao.trim());
        }

        String pessoaAfiliada = registro.getIdentificacaoPessoaSubInstituicao();
        if (pessoaAfiliada != null && !pessoaAfiliada.trim().isEmpty()) {
            pessoaInstituicao.setIndicaPessoaAfiliadaInstituicao(pessoaAfiliada.trim());
        }

        pessoaInstituicao.setDataUltimaAtualizacao(LocalDate.now());
        pessoaInstituicaoRepository.save(pessoaInstituicao);
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
    private Inscricao criarInscricao(Pessoa pessoa, Instituicao instituicao, InscricaoFormsRecord registro) {
        Inscricao inscricao = new Inscricao();
        inscricao.setPessoa(pessoa);
        inscricao.setIdInstituicao(instituicao);

        // Usa data do Forms (coluna B) se disponível, senão usa data atual
        LocalDate dataInclusao = (registro.getDataInclusaoForms() != null)
                ? registro.getDataInclusaoForms().toLocalDate()
                : LocalDate.now();
        inscricao.setDataInclusao(dataInclusao);
        inscricao.setDataUltimaAtualizacao(dataInclusao);

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
     * @param nomePais   Nome do país
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
                    System.out.println(
                            "→ Estado criado: " + nomeEstado + " em " + nomePais + " (ID=" + estado.getId() + ")");
                }
            } else {
                // Estado sem país definido - busca ou cria sem pai
                Optional<Local> estadoOpt = localRepository.findByTipoLocalAndNomeLocal(2, nomeEstado);
                if (estadoOpt.isPresent()) {
                    estado = estadoOpt.get();
                    System.out
                            .println("✓ Estado encontrado (sem país): " + nomeEstado + " (ID=" + estado.getId() + ")");
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
                Optional<Local> cidadeOpt = localRepository.findByTipoLocalAndNomeLocalAndLocalPai(3, nomeCidade,
                        cidadePai);
                if (cidadeOpt.isPresent()) {
                    cidade = cidadeOpt.get();
                    System.out.println("✓ Cidade encontrada: " + nomeCidade + " (ID=" + cidade.getId() + ")");
                } else {
                    // Cria nova cidade
                    cidade = new Local(3, nomeCidade, cidadePai);
                    cidade = localRepository.save(cidade);
                    System.out.println("→ Cidade criada: " + nomeCidade + " em "
                            + (estado != null ? estado.getNomeLocal() : pais.getNomeLocal()) + " (ID=" + cidade.getId()
                            + ")");
                }
            } else {
                // Cidade sem estado/país - busca ou cria sem pai
                Optional<Local> cidadeOpt = localRepository.findByTipoLocalAndNomeLocal(3, nomeCidade);
                if (cidadeOpt.isPresent()) {
                    cidade = cidadeOpt.get();
                    System.out.println(
                            "✓ Cidade encontrada (sem estado/país): " + nomeCidade + " (ID=" + cidade.getId() + ")");
                } else {
                    cidade = new Local(3, nomeCidade, null);
                    cidade = localRepository.save(cidade);
                    System.out.println(
                            "→ Cidade criada (sem estado/país): " + nomeCidade + " (ID=" + cidade.getId() + ")");
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
        if (nome == null)
            return null;
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
     * Lê os emails da planilha e deleta relacionamentos específicos da
     * SubInstituição/TipoAtividade
     * 
     * REGRAS:
     * - Deleta InscricaoTipoAtividade apenas para o TipoAtividade especificado
     * - Deleta PessoaSubInstituicao apenas da SubInstituição especificada
     * - Deleta Pessoa/Usuario APENAS se não houver outros relacionamentos com
     * outras instituições
     * - NÃO exclui entidades Local
     * 
     * @param arquivo          Planilha Excel com coluna de emails (mesma estrutura
     *                         da carga)
     * @param subInstituicaoId SubInstituição para filtrar exclusões
     * @param tipoAtividadeId  TipoAtividade para filtrar exclusões
     * @param instituicaoId    Instituição atual
     * @return Response com resultado da reversão
     */
    @Transactional
    public InscricaoMassivaResponse reverterCargaPorArquivo(MultipartFile arquivo,
            Long tipoAtividadeId,
            Long instituicaoId) {
        InscricaoMassivaResponse response = new InscricaoMassivaResponse();
        response.setInicioProcessamento(LocalDateTime.now());

        final int[] totalDeletados = { 0 };
        final int[] emailsProcessados = { 0 };
        final int[] emailsNaoEncontrados = { 0 };
        final int[] pessoasNaoDeletadas = { 0 }; // Quando tem outros relacionamentos

        try {
            System.out.println("=== INICIANDO REVERSÃO DE CARGA POR ARQUIVO ===");

            // Detecta modo "Excluir Completamente" (tipoAtividadeId == -1)
            boolean excluirCompletamente = (tipoAtividadeId == -1);
            TipoAtividade tipoAtividade = null;

            if (excluirCompletamente) {
                System.out.println("🗑️ MODO: EXCLUSÃO COMPLETA - Todos os tipos de atividade");
            } else {
                // Valida TipoAtividade específico
                Optional<TipoAtividade> tipoAtividadeOpt = tipoAtividadeRepository.findById(tipoAtividadeId);
                if (tipoAtividadeOpt.isEmpty()) {
                    response.addError("Tipo de Atividade não encontrado");
                    return response;
                }
                tipoAtividade = tipoAtividadeOpt.get();
                System.out.println("🔍 MODO: EXCLUSÃO PARCIAL - Tipo: " + tipoAtividade.getTituloTipoAtividade());
            }

            Optional<Instituicao> instituicaoOpt = instituicaoRepository.findById(instituicaoId);
            if (instituicaoOpt.isEmpty()) {
                response.addError("Instituição não encontrada");
                return response;
            }
            Instituicao instituicao = instituicaoOpt.get();

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

                System.out.println("\n--- Processando email " + emailsProcessados[0] + "/" + emails.size() + ": "
                        + email + " ---");

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

                // *** VERIFICAÇÃO PRÉVIA: Existe algum relacionamento com esta instituição? ***
                Optional<Inscricao> inscricaoCheck = inscricaoRepository
                        .findByPessoaIdAndIdInstituicaoId(pessoa.getId(), instituicao.getId());
                Optional<PessoaInstituicao> piCheck = pessoaInstituicaoRepository
                        .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId());
                Optional<UsuarioInstituicao> uiCheck = Optional.empty();
                if (usuarioOpt.isPresent()) {
                    uiCheck = usuarioInstituicaoRepository
                            .findByUsuarioIdAndInstituicaoId(usuarioOpt.get().getId(), instituicao.getId());
                }

                // Se não existe NENHUM relacionamento com esta instituição
                if (inscricaoCheck.isEmpty() && piCheck.isEmpty() && uiCheck.isEmpty()) {
                    System.out.println("⚠ Nenhum relacionamento encontrado com esta instituição para: " + email);

                    if (excluirCompletamente) {
                        // Tentar remoção completa se não houver vínculos em nenhuma instituição ou
                        // entidades
                        List<Inscricao> inscricoesTodas = inscricaoRepository.findByPessoaId(pessoa.getId());
                        List<PessoaInstituicao> pisTodas = pessoaInstituicaoRepository.findByPessoaId(pessoa.getId());
                        List<UsuarioInstituicao> uisTodas = usuarioOpt.isPresent()
                                ? usuarioInstituicaoRepository.findByUsuarioId(usuarioOpt.get().getId())
                                : java.util.Collections.emptyList();

                        Optional<Autor> autorGlobalOpt = autorRepository.findByPessoa(pessoa);
                        boolean temOcorrencias = autorGlobalOpt.isPresent()
                                && ocorrenciaAtividadeRepository.existsByIdAutorId(autorGlobalOpt.get().getId());
                        boolean temAtividadesComoSolicitante = atividadeRepository.existsByIdSolicitante(pessoa);

                        if (inscricoesTodas.isEmpty() && pisTodas.isEmpty() && uisTodas.isEmpty()
                                && !temOcorrencias && !temAtividadesComoSolicitante) {
                            // Sem vínculos em qualquer lugar: remover completamente
                            if (autorGlobalOpt.isPresent()) {
                                autorRepository.delete(autorGlobalOpt.get());
                                totalDeletados[0]++;
                                System.out.println("✓ Autor deletado (global)");
                            }
                            if (usuarioOpt.isPresent()) {
                                Usuario usuario = usuarioOpt.get();
                                usuarioRepository.delete(usuario);
                                totalDeletados[0]++;
                                System.out.println("✓ Usuario deletado (global)");
                            }
                            pessoaRepository.delete(pessoa);
                            totalDeletados[0]++;
                            System.out.println("✓ Pessoa deletada (global)");

                            response.addWarning("Email " + email
                                    + " - Usuário completamente removido (sem vínculos em qualquer instituição)");
                        } else {
                            // Há vínculos fora desta instituição: manter
                            emailsNaoEncontrados[0]++;
                            StringBuilder motivo = new StringBuilder();
                            if (!inscricoesTodas.isEmpty())
                                motivo.append("inscrições em outras instituições; ");
                            if (!pisTodas.isEmpty())
                                motivo.append("pessoa_instituicao em outras instituições; ");
                            if (!uisTodas.isEmpty())
                                motivo.append("usuario_instituicao em outras instituições; ");
                            if (temAtividadesComoSolicitante)
                                motivo.append("atividade com solicitante; ");
                            if (temOcorrencias)
                                motivo.append("ocorrência com autor; ");
                            response.addWarning("Email " + email
                                    + " - Relacionamentos encontrados fora desta instituição. Usuário mantido. Motivo: "
                                    + motivo.toString());
                        }
                    } else {
                        response.addWarning(
                                "Email " + email + " - Nenhum relacionamento encontrado com esta instituição");
                        emailsNaoEncontrados[0]++;
                    }
                    continue; // Pula para o próximo email
                }

                if (excluirCompletamente) {
                    // MODO EXCLUSÃO COMPLETA: Remove todos os relacionamentos da instituição
                    System.out.println("🗑️ Exclusão completa - Removendo TODOS os relacionamentos da instituição");

                    // 1. Deleta TODAS as InscricaoTipoAtividade da instituição
                    if (inscricaoCheck.isPresent()) {
                        Inscricao inscricao = inscricaoCheck.get();
                        List<InscricaoTipoAtividade> todasItas = inscricaoTipoAtividadeRepository
                                .findByInscricaoId(inscricao.getId());

                        for (InscricaoTipoAtividade ita : todasItas) {
                            inscricaoTipoAtividadeRepository.delete(ita);
                            totalDeletados[0]++;
                            System.out.println("✓ InscricaoTipoAtividade deletada: "
                                    + ita.getTipoAtividade().getTituloTipoAtividade());
                        }

                        // Deleta Inscricao
                        inscricaoRepository.delete(inscricao);
                        totalDeletados[0]++;
                        System.out.println("✓ Inscricao deletada");
                    }

                } else {
                    // MODO EXCLUSÃO PARCIAL: Remove apenas InscricaoTipoAtividade específica
                    System.out.println(
                            "🔍 Exclusão parcial - Removendo apenas tipo: " + tipoAtividade.getTituloTipoAtividade());

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
                }

                // 2. Verifica se Pessoa ainda tem Inscricoes na INSTITUIÇÃO ATUAL
                List<Inscricao> inscricoesNaInstituicao = inscricaoRepository
                        .findByPessoaIdAndIdInstituicaoId(pessoa.getId(), instituicao.getId())
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());

                // 3. Verifica se Pessoa tem outros relacionamentos (TODAS as instituições)
                List<PessoaInstituicao> todasPis = pessoaInstituicaoRepository.findByPessoaId(pessoa.getId());
                boolean temOutrasInstituicoes = todasPis.size() > 1;

                System.out.println("📊 Análise: inscricoesNaInstituicao=" + inscricoesNaInstituicao.size() +
                        ", todasPis=" + todasPis.size() +
                        ", temOutrasInstituicoes=" + temOutrasInstituicoes);

                // DECISÃO 1: Não há mais inscrições nesta instituição?
                if (inscricoesNaInstituicao.isEmpty()) {
                    System.out.println("🗑️ Sem inscrições nesta instituição - Removendo relacionamentos");

                    // Deleta PessoaInstituicao desta instituição
                    Optional<PessoaInstituicao> piOpt = pessoaInstituicaoRepository
                            .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId());
                    if (piOpt.isPresent()) {
                        pessoaInstituicaoRepository.delete(piOpt.get());
                        totalDeletados[0]++;
                        System.out.println("✓ PessoaInstituicao deletada (instituicao=" + instituicao.getId() + ")");
                    }

                    // Deleta UsuarioInstituicao desta instituição
                    if (usuarioOpt.isPresent()) {
                        Usuario usuario = usuarioOpt.get();
                        Optional<UsuarioInstituicao> uiOpt = usuarioInstituicaoRepository
                                .findByUsuarioIdAndInstituicaoId(usuario.getId(), instituicao.getId());
                        if (uiOpt.isPresent()) {
                            usuarioInstituicaoRepository.delete(uiOpt.get());
                            totalDeletados[0]++;
                            System.out
                                    .println("✓ UsuarioInstituicao deletada (instituicao=" + instituicao.getId() + ")");
                        }
                    }

                    // DECISÃO 2: Tem vínculos com outras instituições?
                    if (temOutrasInstituicoes) {
                        // TEM outros vínculos - Mantém Pessoa e Usuario
                        pessoasNaoDeletadas[0]++;
                        System.out.println("⚠ Pessoa/Usuario mantidos (tem vínculos com outras " + (todasPis.size() - 1)
                                + " instituição(ões))");

                        if (excluirCompletamente) {
                            response.addWarning("Email " + email +
                                    " - Relacionamentos com esta instituição removidos. Usuário mantido (vinculado a outras "
                                    +
                                    (todasPis.size() - 1) + " instituição(ões))");
                        } else {
                            response.addWarning("Email " + email +
                                    " - Relacionamentos removidos. Usuário mantido (vinculado a outras instituições)");
                        }
                    } else {
                        // NÃO tem outros vínculos - Pode deletar Pessoa e Usuario
                        System.out.println("🗑️ Sem vínculos com outras instituições - Deletando Pessoa/Usuario");

                        boolean hasAtividadeSolicitante = atividadeRepository.existsByIdSolicitante(pessoa);
                        Optional<Autor> autorOpt = autorRepository.findByPessoa(pessoa);
                        boolean hasOcorrenciasComoAutor = autorOpt.isPresent()
                                && ocorrenciaAtividadeRepository.existsByIdAutorId(autorOpt.get().getId());

                        if (hasAtividadeSolicitante || hasOcorrenciasComoAutor) {
                            // Há referências externas (Atividade.solicitante ou OcorrenciaAtividade.autor)
                            // -> não deletar Pessoa/Usuario
                            pessoasNaoDeletadas[0]++;
                            StringBuilder motivo = new StringBuilder();
                            if (hasAtividadeSolicitante) {
                                motivo.append("Atividade com solicitante");
                            }
                            if (hasOcorrenciasComoAutor) {
                                if (motivo.length() > 0)
                                    motivo.append(" e ");
                                motivo.append("Ocorrencia de Atividade com Autor");
                            }
                            response.addWarning("Email " + email + " - Usuário mantido: existem vínculos em " + motivo);
                            System.out.println("⚠ Pessoa mantida por vínculos externos: " + motivo);
                        } else {
                            // Deleta Autor se existir (relacionamento com Pessoa)
                            if (autorOpt.isPresent()) {
                                autorRepository.delete(autorOpt.get());
                                totalDeletados[0]++;
                                System.out.println("✓ Autor deletado");
                            }

                            if (usuarioOpt.isPresent()) {
                                Usuario usuario = usuarioOpt.get();
                                usuarioRepository.delete(usuario);
                                totalDeletados[0]++;
                                System.out.println("✓ Usuario deletado: " + usuario.getUsername());
                            }

                            pessoaRepository.delete(pessoa);
                            totalDeletados[0]++;
                            System.out.println("✓ Pessoa deletada: " + email);

                            response.addWarning(
                                    "Email " + email + " - Usuário COMPLETAMENTE removido (sem vínculos restantes)");
                        }
                    }
                } else {
                    // AINDA há inscrições nesta instituição - Mantém tudo
                    pessoasNaoDeletadas[0]++;
                    System.out.println("⚠ Mantendo relacionamentos (ainda existem " + inscricoesNaInstituicao.size()
                            + " inscrição(ões) ativa(s))");
                    response.addWarning("Email " + email +
                            " - Relacionamentos mantidos (ainda existem inscrições ativas nesta instituição)");
                }
            }

            System.out.println("\n=== REVERSÃO CONCLUÍDA ===");
            System.out.println("Emails processados: " + emailsProcessados[0]);
            System.out.println("Emails não encontrados: " + emailsNaoEncontrados[0]);
            System.out.println("Pessoas não deletadas (com outros vínculos): " + pessoasNaoDeletadas[0]);
            System.out.println("Total de registros deletados: " + totalDeletados[0]);

            // Mensagem especial quando nada foi deletado
            if (totalDeletados[0] == 0) {
                response.addWarning("⚠ Nenhum relacionamento encontrado para reversão. " +
                        "Os emails processados não possuem vínculos com esta instituição ou já foram removidos anteriormente.");
            } else {
                response.addWarning(String.format("Reversão concluída. Emails: %d processados, %d não encontrados. " +
                        "Registros deletados: %d. Pessoas mantidas (outros vínculos): %d",
                        emailsProcessados[0], emailsNaoEncontrados[0], totalDeletados[0], pessoasNaoDeletadas[0]));
            }

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
