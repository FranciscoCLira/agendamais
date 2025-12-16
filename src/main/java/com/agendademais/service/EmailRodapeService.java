package com.agendademais.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.agendademais.entities.Instituicao;

/**
 * Serviço para gerenciar a mensagem de rodapé de emails
 * Fornece a mensagem apropriada baseado no modo de envio configurado na instituição
 */
@Service
public class EmailRodapeService {

    @Value("${app.is-production:false}")
    private boolean isProduction;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Gera a mensagem de rodapé para emails baseado no modo de envio da instituição
     * 
     * @param instituicao A instituição que está enviando o email
     * @return HTML com a mensagem de rodapé
     */
    public String gerarMensagemRodape(Instituicao instituicao) {
        // Determinar o modo de envio: padrão é Online (1) se não configurado
        Integer modoEnvio = (instituicao != null && instituicao.getModoEnvioEmail() != null) 
                            ? instituicao.getModoEnvioEmail() 
                            : 1; // Default: Online
        
        // Debug logs
        System.out.println("[EmailRodapeService] Instituição: " + 
                           (instituicao != null ? instituicao.getNomeInstituicao() : "null") +
                           ", modoEnvioEmail=" + modoEnvio + 
                           ", isProduction=" + isProduction + 
                           ", appUrl=" + appUrl);
        
        // Modo 2 = Offline (processamento em batch/fila)
        if (modoEnvio == 2) {
            System.out.println("[EmailRodapeService] ✓ Usando mensagem OFFLINE (modoEnvioEmail=2)");
            // Modo offline - mensagem simplificada
            return "<br><br><hr style='margin:16px 0'>" +
                    "<span style='font-size:12px;color:#888;'>" +
                    "*** Esta mensagem foi enviada em modo offline do sistema.<br>" +
                    "*** Por isso, se não deseja receber mais nossos emails, responda esse email preenchendo o &quot;Assunto&quot; com &quot;REMOVER&quot;."
                    +
                    "</span>";
        } else {
            System.out.println("[EmailRodapeService] ✗ Usando mensagem ONLINE (modoEnvioEmail=1)");
            // Modo 1 = Online (direto) - mensagem padrão com link para o sistema
            return "<br><br><hr style='margin:16px 0'>" +
                    "<span style='font-size:12px;color:#888;'>" +
                    "*** Não deseja receber mais nossos emails? acesse o sistema e exclua seu cadastro, ou remova esse tipo de atividade em &quot;Minhas Inscrições em Tipos de Atividades&quot;<br>"
                    +
                    "Acesse: <a href='" + appUrl + "' style='color:#0066cc;'>" + appUrl + "</a>" +
                    "</span>";
        }
    }

    /**
     * Gera a mensagem de rodapé para emails (retrocompatibilidade - usa modo Online)
     * 
     * @return HTML com a mensagem de rodapé
     * @deprecated Use gerarMensagemRodape(Instituicao) para respeitar configuração da instituição
     */
    @Deprecated
    public String gerarMensagemRodape() {
        return gerarMensagemRodape(null); // Default para modo Online
    }

    /**
     * Gera a mensagem de rodapé para disparos genéricos
     * 
     * @param instituicao A instituição que está enviando o email
     * @return HTML com a mensagem de rodapé
     */
    public String gerarMensagemRodapeGenerico(Instituicao instituicao) {
        return gerarMensagemRodape(instituicao);
    }

    /**
     * Gera a mensagem de rodapé para disparos genéricos (retrocompatibilidade)
     * 
     * @return HTML com a mensagem de rodapé
     * @deprecated Use gerarMensagemRodapeGenerico(Instituicao)
     */
    @Deprecated
    public String gerarMensagemRodapeGenerico() {
        return gerarMensagemRodape(null);
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
    public String getAmbienteStatus(Instituicao instituicao) {
        Integer modoEnvio = (instituicao != null && instituicao.getModoEnvioEmail() != null) 
                            ? instituicao.getModoEnvioEmail() 
                            : 1;
        
        String modoStr = (modoEnvio == 2) ? "OFFLINE (Batch/Fila)" : "ONLINE (Direto)";
        
        if (isProduction) {
            return "PRODUÇÃO - " + modoStr;
        } else {
            return "DESENVOLVIMENTO - " + modoStr;
        }
    }
}
