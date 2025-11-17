package com.agendademais.services;

import com.agendademais.entities.Local;
import com.agendademais.repositories.LocalRepository;
import com.agendademais.repositories.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LocalServiceTest {

    @Mock
    private LocalRepository localRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private LocalService localService;

    private Local sampleLocal;

    @BeforeEach
    void setup() {
        sampleLocal = new Local();
        sampleLocal.setId(42L);
        sampleLocal.setTipoLocal(2);
        sampleLocal.setNomeLocal("TesteEstado");
    }

    @Test
    void safeDeleteById_deletesWhenNoReferences() {
        when(localRepository.findById(42L)).thenReturn(Optional.of(sampleLocal));
        when(pessoaRepository.countByPais(sampleLocal)).thenReturn(0L);
        when(pessoaRepository.countByEstado(sampleLocal)).thenReturn(0L);
        when(pessoaRepository.countByCidade(sampleLocal)).thenReturn(0L);

        boolean result = localService.safeDeleteById(42L);

        assertTrue(result, "Expected deletion to succeed when there are no references");
        verify(localRepository, times(1)).delete(sampleLocal);
    }

    @Test
    void safeDeleteById_refusesWhenReferenced() {
        when(localRepository.findById(42L)).thenReturn(Optional.of(sampleLocal));
        when(pessoaRepository.countByPais(sampleLocal)).thenReturn(1L);
        when(pessoaRepository.countByEstado(sampleLocal)).thenReturn(0L);
        when(pessoaRepository.countByCidade(sampleLocal)).thenReturn(0L);

        boolean result = localService.safeDeleteById(42L);

        assertFalse(result, "Expected deletion to be refused when there are Pessoa references");
        verify(localRepository, never()).delete(any());
    }

}
