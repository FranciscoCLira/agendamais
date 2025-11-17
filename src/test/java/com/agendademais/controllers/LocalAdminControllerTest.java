package com.agendademais.controllers;

import com.agendademais.entities.Local;
import com.agendademais.entities.Usuario;
import com.agendademais.repositories.LocalRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.services.LocalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class LocalAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalService localService;

    @MockBean
    private LocalRepository localRepository;

    @MockBean
    private PessoaRepository pessoaRepository;

    private MockHttpSession makeAdminSession() {
        MockHttpSession session = new MockHttpSession();
        Usuario u = new Usuario();
        u.setUsername("superu");
        session.setAttribute("usuarioLogado", u);
        session.setAttribute("nivelAcessoAtual", 9);
        return session;
    }

    @Test
    public void deletarLocal_success_showsSuccessFlash() throws Exception {
        // Arrange
        when(localRepository.findById(1L)).thenReturn(java.util.Optional.of(new Local()));
        when(localService.safeDeleteById(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/gestao/locais/deletar/1").session(makeAdminSession()))
                .andExpect(redirectedUrl("/gestao/locais"))
                .andExpect(flash().attributeExists("mensagemSucesso"));

        verify(localService, times(1)).safeDeleteById(1L);
    }

    @Test
    public void deletarLocal_referenced_showsErrorFlash() throws Exception {
        // Arrange
        Local local = new Local();
        local.setId(2L);
        when(localRepository.findById(2L)).thenReturn(java.util.Optional.of(local));
        when(localService.safeDeleteById(2L)).thenReturn(false);
        when(pessoaRepository.countByPais(local)).thenReturn(1L);
        when(pessoaRepository.countByEstado(local)).thenReturn(0L);
        when(pessoaRepository.countByCidade(local)).thenReturn(0L);

        // Act & Assert
        mockMvc.perform(post("/gestao/locais/deletar/2").session(makeAdminSession()))
                .andExpect(redirectedUrl("/gestao/locais"))
                .andExpect(flash().attributeExists("mensagemErro"));

        verify(localService, times(1)).safeDeleteById(2L);
    }
}
