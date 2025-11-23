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

            // Email obrigatório e válido
            if (registro.getEmail() == null || registro.getEmail().trim().isEmpty()) {
                erros.append("Email obrigatório. ");
            } else if (!registro.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                erros.append("Email inválido. ");
            }

            // Nome obrigatório
            if (registro.getNome() == null || registro.getNome().trim().isEmpty()) {
                erros.append("Nome obrigatório. ");
            }

            // Celular obrigatório
            if (registro.getCelular() == null || registro.getCelular().trim().isEmpty()) {
                erros.append("Celular obrigatório. ");
            }

            if (erros.length() > 0) {
                registro.setMensagemErro("Linha " + registro.getLinha() + ": " + erros.toString());
                response.addWarning(registro.getMensagemErro());
            }
        }
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
                        return p;
                    });

            // 2. Busca ou cria Usuario
            Usuario usuario = usuarioRepository.findByEmailPessoa(email)
                    .orElseGet(() -> {
                        System.out.println("→ Criando novo Usuario...");
                        Usuario u = criarUsuario(pessoa, registro);
                        System.out.println("✓ Usuario criado: ID=" + u.getId() + ", username=" + u.getUsername());
                        return u;
                    });

            // 3. Busca ou cria PessoaInstituicao
            PessoaInstituicao pessoaInstituicao = pessoaInstituicaoRepository
                    .findByPessoaIdAndInstituicaoId(pessoa.getId(), instituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando PessoaInstituicao...");
                        return criarPessoaInstituicao(pessoa, instituicao, registro);
                    });

            // 4. Busca ou cria PessoaSubInstituicao
            PessoaSubInstituicao pessoaSubInstituicao = pessoaSubInstituicaoRepository
                    .findByPessoaIdAndSubInstituicaoId(pessoa.getId(), subInstituicao.getId())
                    .orElseGet(() -> {
                        System.out.println("→ Criando PessoaSubInstituicao...");
                        PessoaSubInstituicao psi = criarPessoaSubInstituicao(pessoa, instituicao, subInstituicao, registro);
                        System.out.println("✓ PessoaSubInstituicao criada: ID=" + psi.getId());
                        return psi;
                    });

            // 5. Busca ou cria UsuarioInstituicao
            UsuarioInstituicao usuarioInstituicao = usuarioInstituicaoRepository
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
     * Cria nova Pessoa
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
        
        pessoaInstituicao.setDataAfiliacao(LocalDate.now());
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
        pessoaSubInstituicao.setDataAfiliacao(LocalDate.now());
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
     * Busca ou cria Local (método removido - Local usa estrutura hierárquica)
     * PessoaInstituicao não tem relacionamento direto com Local
     */
    @SuppressWarnings("unused")
    private Local buscarOuCriarLocal(String cidade, String estado, String pais) {
        // Método mantido para compatibilidade futura
        // Atualmente PessoaInstituicao não armazena Local diretamente
        return null;
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
}
