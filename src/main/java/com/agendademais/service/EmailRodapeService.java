package com.agendademais.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço para gerenciar a mensagem de rodapé de emails
 * Fornece a mensagem apropriada baseado no ambiente (desenvolvimento, produção,
 * offline)
 */
@Service
public class EmailRodapeService {

    @Value("${app.is-production:false}")
    private boolean isProduction;

    @Value("${app.is-offline-mode:false}")
    private boolean isOfflineMode;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Gera a mensagem de rodapé para emails
     * 
     * @return HTML com a mensagem de rodapé
     */
    public String gerarMensagemRodape() {
        // Detecta se é localhost (considerando tanto localhost:8080 quanto 127.0.0.1:8080)
        boolean isLocalhost = appUrl != null && (
            appUrl.contains("localhost") || 
            appUrl.contains("127.0.0.1")
        );
        
        // Debug logs
        System.out.println("[EmailRodapeService] isProduction=" + isProduction + 
                           ", isOfflineMode=" + isOfflineMode + 
                           ", isLocalhost=" + isLocalhost + 
                           ", appUrl=" + appUrl);
        
        // Se é localhost E produção, ou modo offline, exibir mensagem offline
        if (isOfflineMode || (isProduction && isLocalhost)) {
            System.out.println("[EmailRodapeService] ✓ Usando mensagem OFFLINE");
            // Modo offline em produção - mensagem simplificada
            return "<br><br><hr style='margin:16px 0'>" +
                    "<span style='font-size:12px;color:#888;'>" +
                    "*** Esta mensagem foi enviada em modo offline do sistema.<br>" +
                    "*** Por isso, se não deseja receber mais nossos emails, responda esse email preenchendo o &quot;Assunto&quot; com &quot;REMOVER&quot;."
                    +
                    "</span>";
        } else {
            System.out.println("[EmailRodapeService] ✗ Usando mensagem NORMAL");
            // Modo normal - mensagem padrão com link para o sistema
            return "<br><br><hr style='margin:16px 0'>" +
                    "<span style='font-size:12px;color:#888;'>" +
                    "*** Não deseja receber mais nossos emails? acesse o sistema e exclua seu cadastro, ou remova esse tipo de atividade em &quot;Minhas Inscrições em Tipos de Atividades&quot;<br>"
                    +
                    "Acesse: <a href='" + appUrl + "' style='color:#0066cc;'>" + appUrl + "</a>" +
                    "</span>";
        }
    }

    /**
     * Gera a mensagem de rodapé para disparos genéricos
     * 
     * @return HTML com a mensagem de rodapé
     */
    public String gerarMensagemRodapeGenerico() {
        // A lógica é a mesma para disparos genéricos
        return gerarMensagemRodape();
    }

    /**
     * Verifica se está em modo offline
     */
    public boolean isOfflineMode() {
        return isOfflineMode;
    }

    /**
     * Verifica se está em produção
     */
    public boolean isProduction() {
        return isProduction;
    }

    /**
     * Obtém o status do ambiente como string
     */
    public String getAmbienteStatus() {
        if (isOfflineMode) {
            return "OFFLINE (Modo Offline)";
        } else if (isProduction) {
            return "PRODUÇÃO";
        } else {
            return "DESENVOLVIMENTO";
        }
    }
}
