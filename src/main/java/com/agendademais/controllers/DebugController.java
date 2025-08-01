package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.repositories.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de debug temporário para testar os filtros
 */
@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private LocalRepository localRepository;

    @GetMapping("/locais")
    @ResponseBody
    public String debugLocais() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<h2>DEBUG LOCAIS</h2>");

            List<Local> todos = localRepository.findAll();
            sb.append("<p>Total de locais: ").append(todos.size()).append("</p>");

            if (todos.isEmpty()) {
                sb.append("<p><strong>NENHUM LOCAL ENCONTRADO!</strong></p>");
                return sb.toString();
            }

            sb.append("<table border='1'>");
            sb.append("<tr><th>ID</th><th>Tipo</th><th>Nome</th><th>Pai</th><th>Revisado</th></tr>");

            for (Local local : todos) {
                sb.append("<tr>");
                sb.append("<td>").append(local.getId()).append("</td>");
                sb.append("<td>").append(local.getTipoLocal()).append("</td>");
                sb.append("<td>").append(local.getNomeLocal()).append("</td>");
                sb.append("<td>").append(local.getLocalPai() != null ? local.getLocalPai().getNomeLocal() : "null")
                        .append("</td>");
                sb.append("<td>").append(local.getRevisadoLocal()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");

            sb.append("<h3>Contadores:</h3>");
            sb.append("<p>Países (tipo 1): ").append(localRepository.countByTipoLocal(1)).append("</p>");
            sb.append("<p>Estados (tipo 2): ").append(localRepository.countByTipoLocal(2)).append("</p>");
            sb.append("<p>Cidades (tipo 3): ").append(localRepository.countByTipoLocal(3)).append("</p>");

            return sb.toString();
        } catch (Exception e) {
            return "<h2>ERRO:</h2><p>" + e.getMessage() + "</p>";
        }
    }

    @GetMapping("/filtro")
    @ResponseBody
    public String debugFiltro(@RequestParam(required = false) Integer tipoLocal) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<h2>DEBUG FILTRO</h2>");
            sb.append("<p>Parâmetro tipoLocal: ").append(tipoLocal).append("</p>");

            if (tipoLocal != null) {
                // Teste com findByTipoLocal (List)
                List<Local> resultado1 = localRepository.findByTipoLocal(tipoLocal);
                sb.append("<p>findByTipoLocal(").append(tipoLocal).append("): ").append(resultado1.size())
                        .append(" elementos</p>");

                // Teste com findByTipoLocal (Page)
                Pageable pageable = PageRequest.of(0, 20);
                Page<Local> resultado1Page = localRepository.findByTipoLocal(tipoLocal, pageable);
                sb.append("<p>findByTipoLocal com Page(").append(tipoLocal).append("): ")
                        .append(resultado1Page.getTotalElements()).append(" elementos</p>");

                // Teste com findByFiltros
                Page<Local> resultado2 = localRepository.findByFiltros(tipoLocal, null, null, pageable);
                sb.append("<p>findByFiltros(").append(tipoLocal).append(", null, null): ")
                        .append(resultado2.getTotalElements()).append(" elementos</p>");

                if (!resultado1.isEmpty()) {
                    sb.append("<h3>Primeiros elementos encontrados:</h3>");
                    sb.append("<ul>");
                    for (int i = 0; i < Math.min(5, resultado1.size()); i++) {
                        Local local = resultado1.get(i);
                        sb.append("<li>").append(local.getId()).append(" - ").append(local.getNomeLocal())
                                .append("</li>");
                    }
                    sb.append("</ul>");
                }
            } else {
                sb.append("<p>Nenhum filtro aplicado</p>");
            }

            return sb.toString();
        } catch (Exception e) {
            return "<h2>ERRO:</h2><p>" + e.getMessage() + "</p>";
        }
    }

    @GetMapping("/erro500")
    public String testeErro500() {
        // Força um erro 500 para testar
        throw new RuntimeException(
                "Erro de teste para verificar tratamento 500 - Dados: URL requisitada, tipo de erro, stack trace");
    }

    @GetMapping("/erro403")
    public String testeErro403() {
        // Simula erro 403
        throw new org.springframework.security.access.AccessDeniedException(
                "Teste de acesso negado - Usuário sem permissão");
    }
}
