package com.agendademais.specs;

import com.agendademais.entities.Atividade;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.time.LocalDate;

public class AtividadeSpecs {
    public static Specification<Atividade> filtro(
            String titulo, String situacao, String forma, String alvo,
            Long subInstituicao, Long solicitante,
            LocalDate dataInicio, LocalDate dataFim,
            Long instituicaoId) {
        return (root, query, cb) -> {
            Predicate p = cb.conjunction();
            if (titulo != null && !titulo.isEmpty())
                p = cb.and(p, cb.like(cb.lower(root.get("tituloAtividade")), "%" + titulo.toLowerCase() + "%"));
            if (situacao != null && !situacao.isEmpty())
                p = cb.and(p, cb.equal(root.get("situacaoAtividade"), situacao));
            if (forma != null && !forma.isEmpty())
                p = cb.and(p, cb.equal(root.get("formaApresentacao"), Integer.valueOf(forma)));
            if (alvo != null && !alvo.isEmpty())
                p = cb.and(p, cb.equal(root.get("publicoAlvo"), Integer.valueOf(alvo)));
            if (subInstituicao != null)
                p = cb.and(p, cb.equal(root.get("subInstituicao").get("id"), subInstituicao));
            if (solicitante != null)
                p = cb.and(p, cb.equal(root.get("idSolicitante").get("id"), solicitante));
            if (dataInicio != null)
                p = cb.and(p, cb.greaterThanOrEqualTo(root.get("dataAtualizacao"), dataInicio));
            if (dataFim != null)
                p = cb.and(p, cb.lessThanOrEqualTo(root.get("dataAtualizacao"), dataFim));
            if (instituicaoId != null)
                p = cb.and(p, cb.equal(root.get("instituicao").get("id"), instituicaoId));
            return p;
        };
    }
}
