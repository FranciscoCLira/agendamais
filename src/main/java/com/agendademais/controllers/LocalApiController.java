package com.agendademais.controllers;

import com.agendademais.dtos.LocalDTO;
import com.agendademais.entities.Local;
import com.agendademais.services.LocalService;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/locais")
public class LocalApiController {

    @Autowired
    private LocalService localService;

    // Exemplo: GET /api/locais/cidades/auto?estado=SP&query=São
    @GetMapping("/cidades/auto")
    public List<Local> autocompleteCidades(
            @RequestParam String estado,
            @RequestParam String query) {

        // Busca o objeto do estado pelo nome (você pode melhorar, p.ex. cacheando)
        Optional<Local> estadoObj = localService.buscarPorNomeETipo(estado, 2);
        if (estadoObj.isEmpty())
            return List.of();

        // Filtra cidades que comecem com o texto digitado (case insensitive)
        return localService.autocompleteCidades(estadoObj.get(), query);
    }

    // Listar todos os países
    @GetMapping("/paises")
    public List<LocalDTO> listarPaises() {
        return localService.listarPorTipo(1).stream().map(LocalDTO::new).toList();
    }

    // Listar estados de um país pelo nome do país
    @GetMapping("/estados")
    public List<LocalDTO> listarEstados(@RequestParam String paisNome) {
        return localService.listarEstadosPorPaisNome(paisNome).stream().map(LocalDTO::new).toList();
    }

    // Listar cidades de um estado (com suporte a país e estado para maior precisão)
    @GetMapping("/cidades")
    public List<LocalDTO> listarCidades(
            @RequestParam(required = false) String paisNome,
            @RequestParam String estadoNome) {
        // Se o país for informado, podemos fazer uma busca mais específica
        // Por enquanto, vamos usar apenas o estado como antes
        return localService.listarCidadesPorEstadoNome(estadoNome).stream().map(LocalDTO::new).toList();
    }

    // Autocomplete cidade
    @GetMapping("/autocomplete")
    public List<LocalDTO> autocompleteCidade(@RequestParam int tipoLocal, @RequestParam String termo) {
        return localService.buscarPorNomeParcial(tipoLocal, termo)
                .stream().map(LocalDTO::new).toList();
    }

    // 5. Criar Local por API (para testes ou admin)

    @PostMapping("/novo")
    public Local criarLocal(
            @RequestParam int tipoLocal,
            @RequestParam String nomeLocal,
            @RequestParam(required = false) Long idPai // se houver
    ) {
        Local localPai = null;
        if (idPai != null) {
            localPai = localService.buscarPorId(idPai).orElse(null);
        }
        return localService.buscarOuCriar(tipoLocal, nomeLocal, localPai);
    }

    // Debug endpoint to test API
    @GetMapping("/debug")
    public Map<String, Object> debug(@RequestParam(required = false) String pais) {
        Map<String, Object> result = new HashMap<>();

        // Lista todos os países
        List<Local> paises = localService.listarPorTipo(1);
        result.put("paises", paises.stream().map(p -> p.getNomeLocal()).toList());

        if (pais != null) {
            // Lista estados do país informado
            List<Local> estados = localService.listarEstadosPorPaisNome(pais);
            result.put("estados_" + pais, estados.stream().map(e -> e.getNomeLocal()).toList());
        }

        return result;
    }
}