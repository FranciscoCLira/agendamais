package com.agendademais.services;

import com.agendademais.dto.InscricaoForm;
import com.agendademais.entities.*;
import com.agendademais.exceptions.BusinessException;
import com.agendademais.repositories.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InscricaoServiceTest {

    @InjectMocks
    private InscricaoService inscricaoService;

    @Mock
    private InscricaoRepository inscricaoRepository;
    @Mock
    private TipoAtividadeRepository tipoAtividadeRepository;

    private Pessoa pessoa;
    private Instituicao instituicao;
    private TipoAtividade atividade1;
    private TipoAtividade atividade2;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        pessoa = new Pessoa();
        pessoa.setId(1L);
        instituicao = new Instituicao();
        instituicao.setId(1L);

        atividade1 = new TipoAtividade();
        atividade1.setId(10L);
        atividade1.setInstituicao(instituicao);
        atividade2 = new TipoAtividade();
        atividade2.setId(20L);
        atividade2.setInstituicao(instituicao);
    }

    @Test
    void deveLancarExceptionQuandoNenhumaAtividadeSelecionada() {
        InscricaoForm form = new InscricaoForm();
        when(inscricaoRepository.findByIdPessoaAndIdInstituicao(pessoa, instituicao)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            inscricaoService.processarInscricao(pessoa, instituicao, form);
        });
        assertEquals("Selecione uma ou mais atividades para se inscrever.", ex.getMessage());
    }

    @Test
    void deveLancarExceptionSeAtividadeNaoPertencerInstituicao() {
        InscricaoForm form = new InscricaoForm();
        form.setTiposAtividadeIds(List.of(10L));

        TipoAtividade outraInst = new TipoAtividade();
        outraInst.setId(10L);
        Instituicao instFalsa = new Instituicao();
        instFalsa.setId(99L); // um ID diferente do esperado
        outraInst.setInstituicao(instFalsa);
        when(tipoAtividadeRepository.findById(10L)).thenReturn(Optional.of(outraInst));

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            inscricaoService.processarInscricao(pessoa, instituicao, form);
        });
        assertEquals("Uma ou mais atividades não pertencem à sua instituição.", ex.getMessage());
    }

    @Test
    void deveCriarNovaInscricaoComAtividades() {
        InscricaoForm form = new InscricaoForm();
        form.setTiposAtividadeIds(List.of(10L, 20L));
        form.setComentarios("teste");
        when(inscricaoRepository.findByIdPessoaAndIdInstituicao(pessoa, instituicao)).thenReturn(Optional.empty());
        when(tipoAtividadeRepository.findById(10L)).thenReturn(Optional.of(atividade1));
        when(tipoAtividadeRepository.findById(20L)).thenReturn(Optional.of(atividade2));
        when(inscricaoRepository.save(any())).thenReturn(new Inscricao());

        assertDoesNotThrow(() -> inscricaoService.processarInscricao(pessoa, instituicao, form));
        verify(inscricaoRepository, times(1)).save(any());
    }

    @Test
    void deveAtualizarInscricaoRemovendoAtividade() {
        InscricaoForm form = new InscricaoForm();
        form.setTiposAtividadeIds(List.of(10L));

        Inscricao inscricao = new Inscricao();
        inscricao.setId(1L);
        inscricao.setIdPessoa(pessoa);
        inscricao.setIdInstituicao(instituicao);

        InscricaoTipoAtividade ita1 = new InscricaoTipoAtividade();
        ita1.setTipoAtividade(atividade1);
        ita1.setInscricao(inscricao);

        InscricaoTipoAtividade ita2 = new InscricaoTipoAtividade();
        ita2.setTipoAtividade(atividade2);
        ita2.setInscricao(inscricao);

        Set<InscricaoTipoAtividade> set = new HashSet<>();
        set.add(ita1);
        set.add(ita2);
        inscricao.setTiposAtividade(set);

        when(inscricaoRepository.findByIdPessoaAndIdInstituicao(pessoa, instituicao)).thenReturn(Optional.of(inscricao));
        when(tipoAtividadeRepository.findById(10L)).thenReturn(Optional.of(atividade1));
        when(inscricaoRepository.save(any())).thenReturn(inscricao);

        assertDoesNotThrow(() -> inscricaoService.processarInscricao(pessoa, instituicao, form));
        verify(inscricaoRepository, times(1)).save(any());
    }
}

