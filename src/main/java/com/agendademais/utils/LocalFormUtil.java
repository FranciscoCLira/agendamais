package com.agendademais.utils;

import com.agendademais.entities.Local;
import com.agendademais.entities.Pessoa;
import com.agendademais.services.LocalService;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

public class LocalFormUtil {

    private LocalFormUtil() {
    } // construtor privado para utilitário

    public static void preencherListasLocais(Model model, LocalService localService, Pessoa pessoa) {
        List<Local> paises = localService.listarPorTipo(1);
        List<Local> estados = Collections.emptyList();

        if (pessoa.getPais() != null) {
            Long paisId = pessoa.getPais().getId();
            if (paisId != null)
                estados = localService.listarPorTipoAndPai(2, paisId);
        }

        List<Local> cidades = Collections.emptyList();
        if (pessoa.getEstado() != null) {
            Long estadoId = pessoa.getEstado().getId();
            if (estadoId != null)
                cidades = localService.listarPorTipoAndPai(3, estadoId);
        }

        model.addAttribute("paises", paises);
        model.addAttribute("estados", estados);
        model.addAttribute("cidades", cidades);
    }
}