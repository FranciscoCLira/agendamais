package com.agendademais.specs;

import com.agendademais.entities.LogPostagem;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;

public class LogPostagemSpecs {
    public static Specification<LogPostagem> porInstituicao(Long instituicaoId,
            com.agendademais.repositories.OcorrenciaAtividadeRepository ocorrenciaRepo) {
        return (root, query, cb) -> {
            if (instituicaoId == null)
                return cb.conjunction();
            // Subquery para pegar IDs de ocorrências da instituição
            Subquery<Long> sub = query.subquery(Long.class);
            Root<com.agendademais.entities.OcorrenciaAtividade> oc = sub
                    .from(com.agendademais.entities.OcorrenciaAtividade.class);
            sub.select(oc.get("id"));
            sub.where(cb.equal(oc.get("idAtividade").get("instituicao").get("id"), instituicaoId));
            return root.get("ocorrenciaAtividadeId").in(sub);
        };
    }
}
