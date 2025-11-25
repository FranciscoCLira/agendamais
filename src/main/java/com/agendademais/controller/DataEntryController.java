package com.agendademais.controller;

import com.agendademais.dto.DataEntryRequest;
import com.agendademais.dto.DataEntryResponse;
import com.agendademais.service.DataEntryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Controller para funcionalidades de Data Entry (Carga Massiva)
 * Access control is handled by session checks (nivelAcessoAtual >= 9)
 */
@Controller
@RequestMapping("/admin/dataentry")
public class DataEntryController {

    @Autowired
    private DataEntryService dataEntryService;

    /**
     * Página principal do Data Entry
     */
    @GetMapping
    public String dataEntryPage(Model model, jakarta.servlet.http.HttpSession session) {
        // Verifica se usuário está no modo Controle Total (nivelAcessoAtual = 0)
        // Esta funcionalidade só é acessível via menu Controle Total
        Integer nivelAcesso = (Integer) session.getAttribute("nivelAcessoAtual");
        if (nivelAcesso == null || nivelAcesso != 0) {
            return "redirect:/acesso";
        }
        model.addAttribute("pageTitle", "Carga Massiva de Dados");
        return "admin/dataentry";
    }

    /**
     * Processa upload e carga massiva de usuários
     */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<DataEntryResponse> uploadFile(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "tipoCarga", defaultValue = "teste") String tipoCarga,
            @RequestParam(value = "formatoUsuario", defaultValue = "incremental") String formatoUsuario,
            @RequestParam(value = "separadorCsv", defaultValue = ";") String separadorCsv,
            @RequestParam(value = "validarCelular", defaultValue = "true") boolean validarCelular,
            @RequestParam(value = "gerarSenhaAutomatica", defaultValue = "true") boolean gerarSenhaAutomatica,
            @RequestParam(value = "gerarArquivoResultado", defaultValue = "true") boolean gerarArquivoResultado,
            @RequestParam(value = "instituicaoId", required = false) Long instituicaoId,
            @RequestParam(value = "subInstituicaoId", required = false) Long subInstituicaoId) {

        try {
            // Valida arquivo
            if (arquivo.isEmpty()) {
                DataEntryResponse errorResponse = new DataEntryResponse();
                errorResponse.addError("Arquivo não informado");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Cria request
            DataEntryRequest request = new DataEntryRequest();
            request.setArquivo(arquivo);
            request.setTipoCarga(tipoCarga);
            request.setFormatoUsuario(formatoUsuario);
            request.setSeparadorCsv(separadorCsv);
            request.setValidarCelular(validarCelular);
            request.setGerarSenhaAutomatica(gerarSenhaAutomatica);
            request.setGerarArquivoResultado(gerarArquivoResultado);
            request.setInstituicaoId(instituicaoId);
            request.setSubInstituicaoId(subInstituicaoId);

            // Determina tipo do arquivo
            String nomeArquivo = arquivo.getOriginalFilename();
            if (nomeArquivo != null) {
                if (nomeArquivo.toLowerCase().endsWith(".csv")) {
                    request.setTipoArquivo("csv");
                } else if (nomeArquivo.toLowerCase().endsWith(".xlsx") ||
                        nomeArquivo.toLowerCase().endsWith(".xls")) {
                    request.setTipoArquivo("excel");
                }
            }

            // Processa a carga
            DataEntryResponse response = dataEntryService.processarCargaUsuarios(request);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }

        } catch (Exception e) {
            DataEntryResponse errorResponse = new DataEntryResponse();
            errorResponse.addError("Erro inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Download do arquivo de resultado
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // Por segurança, assume que arquivos estão em diretório temporário específico
            String filePath = System.getProperty("java.io.tmpdir") + File.separator + filename;
            File file = new File(filePath);

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API REST para validar arquivo antes do upload
     */
    @PostMapping("/validate")
    @ResponseBody
    public ResponseEntity<DataEntryResponse> validateFile(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "separadorCsv", defaultValue = ";") String separadorCsv) {
        DataEntryResponse response = new DataEntryResponse();

        try {
            if (arquivo.isEmpty()) {
                response.addError("Arquivo está vazio");
                return ResponseEntity.badRequest().body(response);
            }

            String nomeArquivo = arquivo.getOriginalFilename();
            if (nomeArquivo == null) {
                response.addError("Nome do arquivo não informado");
                return ResponseEntity.badRequest().body(response);
            }

            String extensao = nomeArquivo.toLowerCase();
            if (!extensao.endsWith(".csv") && !extensao.endsWith(".xlsx") && !extensao.endsWith(".xls")) {
                response.addError("Tipo de arquivo não suportado. Use .csv, .xlsx ou .xls");
                return ResponseEntity.badRequest().body(response);
            }

            // Validações básicas do arquivo
            long tamanho = arquivo.getSize();
            if (tamanho > 10 * 1024 * 1024) { // 10MB
                response.addWarning(
                        "Arquivo muito grande (" + (tamanho / 1024 / 1024) + "MB). Pode demorar para processar.");
            }

            // Validar conteúdo do arquivo
            int totalRegistros = dataEntryService.validarConteudoArquivo(arquivo, separadorCsv, response);

            response.addInfo("Arquivo válido: " + nomeArquivo);
            response.addInfo("Tamanho: " + (tamanho / 1024) + "KB");
            response.addInfo("Tipo: " + (extensao.endsWith(".csv") ? "CSV" : "Excel"));

            // Atualizar contadores
            response.setRegistrosLidos(totalRegistros);
            if (totalRegistros > 0) {
                response.addInfo("Total de registros encontrados: " + totalRegistros);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.addError("Erro ao validar arquivo: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtém informações sobre formatos suportados
     */
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<DataEntryInfo> getInfo() {
        DataEntryInfo info = new DataEntryInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Download do arquivo de exemplo CSV básico
     */
    @GetMapping("/exemplo-csv")
    public ResponseEntity<Resource> downloadExemploCSV() {
        try {
            String staticPath = System.getProperty("user.dir") + "/src/main/resources/static/exemplo-usuarios.csv";
            File file = new File(staticPath);
            if (file.exists()) {
                Resource resource = new FileSystemResource(file);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exemplo-usuarios.csv\"");
                headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
                headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                headers.add(HttpHeaders.PRAGMA, "no-cache");
                headers.add(HttpHeaders.EXPIRES, "0");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download do arquivo de exemplo CSV UTF-8
     */
    @GetMapping("/exemplo-csv-utf8")
    public ResponseEntity<Resource> downloadExemploCSVUTF8() {
        try {
            String staticPath = System.getProperty("user.dir") + "/src/main/resources/static/exemplo-usuarios-utf8.csv";
            File file = new File(staticPath);
            if (file.exists()) {
                Resource resource = new FileSystemResource(file);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exemplo-usuarios-utf8.csv\"");
                headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
                headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
                headers.add(HttpHeaders.PRAGMA, "no-cache");
                headers.add(HttpHeaders.EXPIRES, "0");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download do arquivo de exemplo Excel (.xlsx)
     */
    @GetMapping("/exemplo-excel")
    public ResponseEntity<Resource> downloadExemploExcel() {
        try {
            String staticPath = System.getProperty("user.dir") + "/src/main/resources/static/exemplo-usuarios.xlsx";
            File file = new File(staticPath);
            if (file.exists()) {
                Resource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exemplo-usuarios.xlsx\"")
                        .contentType(MediaType
                                .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Classe para informações do DataEntry
     */
    public static class DataEntryInfo {
        public String[] formatosSuportados = { ".csv", ".xlsx", ".xls" };
        public String[] tiposCarga = { "teste", "real" };
        public String[] formatosUsuario = { "incremental" };
        public String[] separadoresCSV = { ";", ",", "\\t" };
        public String descricaoTeste = "Carga de teste gera usuários com prefixo 'X' e senha com sufixo '$'";
        public String descricaoReal = "Carga real gera usuários com prefixo 'U' e senha sem sufixo";
        public String[] colunasObrigatorias = {
                "email", "nome", "celular", "pais", "estado", "cidade"
        };
        public String[] colunasOpcionais = {
                "comentarios", "instituicaoId", "identificacaoPessoaInstituicao",
                "subInstituicaoId", "identificacaoPessoaSubInstituicao",
                "username", "password"
        };
    }

    /**
     * Endpoint para exclusão em massa (desfazimento)
     * Remove usuários baseado no mesmo arquivo de importação
     */
    @PostMapping("/delete-bulk")
    @ResponseBody
    public ResponseEntity<DataEntryResponse> deleteBulk(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "separadorCsv", defaultValue = ";") String separadorCsv,
            @RequestParam(value = "confirmacao", defaultValue = "false") boolean confirmacao) {
        DataEntryResponse response = new DataEntryResponse();

        try {
            // Valida arquivo
            if (arquivo.isEmpty()) {
                response.addError("Arquivo não informado");
                return ResponseEntity.badRequest().body(response);
            }

            // Processa exclusão em massa
            response = dataEntryService.processarExclusaoMassa(arquivo, separadorCsv, confirmacao);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
            }

        } catch (Exception e) {
            response.addError("Erro ao processar exclusão em massa: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
