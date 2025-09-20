package com.agendademais.specs;

import com.agendademais.entities.OcorrenciaAtividade;
import org.springframework.data.jpa.domain.Specification;

public class OcorrenciaAtividadeSpecs {
    public static Specification<OcorrenciaAtividade> porInstituicao(Long instituicaoId) {
        return (root, query, cb) -> {
            if (instituicaoId == null)
                return cb.conjunction();
            return cb.equal(root.get("idAtividade").get("instituicao").get("id"), instituicaoId);
        };
    }
}
