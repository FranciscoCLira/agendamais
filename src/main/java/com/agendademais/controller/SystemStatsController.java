package com.agendademais.controller;

import com.agendademais.repositories.UsuarioRepository;
import com.agendademais.repositories.PessoaRepository;
import com.agendademais.repositories.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller para estatísticas do sistema
 */
@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_USER')")
public class SystemStatsController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PessoaRepository pessoaRepository;
    
    @Autowired
    private LocalRepository localRepository;
    
    /**
     * Estatísticas gerais do sistema
     */
    @GetMapping
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Contadores básicos
            stats.put("totalUsuarios", usuarioRepository.count());
            stats.put("totalPessoas", pessoaRepository.count());
            stats.put("totalLocais", localRepository.count());
            
            // Contadores por tipo de local
            stats.put("totalPaises", localRepository.countByTipoLocal(1));
            stats.put("totalEstados", localRepository.countByTipoLocal(2));
            stats.put("totalCidades", localRepository.countByTipoLocal(3));
            
            // Timestamp
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("status", "success");
            
        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("message", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Estatísticas específicas para validação de carga massiva
     */
    @GetMapping("/validation")
    public Map<String, Object> getValidationStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Informações úteis para validação
            stats.put("emailsExistentes", pessoaRepository.count());
            stats.put("usuariosExistentes", usuarioRepository.count());
            stats.put("locaisDisponiveis", localRepository.count());
            
            // Sugestões para próximos IDs
            Long maxUsuarioId = usuarioRepository.count();
            stats.put("proximoUsuarioSugerido", String.format("U%05d", maxUsuarioId + 1));
            stats.put("proximoTesteSugerido", String.format("X%05d", maxUsuarioId + 1));
            
            stats.put("status", "success");
            
        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("message", e.getMessage());
        }
        
        return stats;
    }
}
