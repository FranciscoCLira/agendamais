package com.agendademais.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.agendademais.entities.LogPostagem;
import com.agendademais.entities.OcorrenciaAtividade;
import com.agendademais.repositories.LogPostagemRepository;
import com.agendademais.repositories.OcorrenciaAtividadeRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DisparoEmailService {
    @Autowired
    private LogPostagemRepository logPostagemRepository;
    @Autowired
    private OcorrenciaAtividadeRepository ocorrenciaAtividadeRepository;

    public static class ProgressoDisparo {
        public int total;
        public int enviados;
        public int falhas;
        public List<String> erros = new ArrayList<>();
        public boolean concluido;
        public String fatalError; // New field for fatal errors
    }

    // Simulação de progresso por ocorrenciaId
    private final Map<Long, ProgressoDisparo> progressoMap = new ConcurrentHashMap<>();

    public void iniciarDisparo(Long ocorrenciaId, int totalDestinatarios) {
        ProgressoDisparo progresso = new ProgressoDisparo();
        progresso.total = totalDestinatarios;
        progresso.enviados = 0;
        progresso.falhas = 0;
        progresso.concluido = false;
        progressoMap.put(ocorrenciaId, progresso);
        // Simular envio em background (em produção, usar @Async ou fila)
        new Thread(() -> {
            for (int i = 1; i <= totalDestinatarios; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                progresso.enviados = i;
                if (i % 15 == 0) {
                    progresso.falhas++;
                    progresso.erros.add("email" + i + "@exemplo.com falhou");
                }
            }
            progresso.concluido = true;

            // Salvar log ao final do disparo
            try {
                OcorrenciaAtividade ocorrencia = ocorrenciaAtividadeRepository.findById(ocorrenciaId).orElse(null);
                if (ocorrencia == null) {
                    System.err.println("[LogPostagem] OcorrenciaAtividade não encontrada para ID: " + ocorrenciaId);
                    progresso.fatalError = "OcorrenciaAtividade não encontrada para ID: " + ocorrenciaId;
                } else {
                    LogPostagem log = new LogPostagem();
                    log.setDataHoraPostagem(LocalDateTime.now());
                    log.setOcorrenciaAtividadeId(ocorrencia.getId());
                    log.setTituloAtividade(
                            ocorrencia.getIdAtividade() != null
                                    && ocorrencia.getIdAtividade().getTituloAtividade() != null
                                            ? ocorrencia.getIdAtividade().getTituloAtividade()
                                            : "");
                    log.setAssuntoDivulgacao(ocorrencia.getAssuntoDivulgacao());
                    log.setTextoDetalheDivulgacao(ocorrencia.getDetalheDivulgacao());
                    // AutorId pode ser null se não houver autor logado
                    log.setAutorId(ocorrencia.getIdAutor() != null ? ocorrencia.getIdAutor().getId() : null);
                    log.setQtEnviados(totalDestinatarios);
                    log.setQtFalhas(progresso.falhas);
                    // Mensagem de log
                    if (progresso.falhas > 0) {
                        StringBuilder msg = new StringBuilder();
                        msg.append(progresso.falhas).append(" e-mails rejeitados. ");
                        msg.append("Detalhes: ");
                        for (String erro : progresso.erros) {
                            msg.append(erro).append("; ");
                        }
                        log.setMensagemLogPostagem(msg.toString());
                    } else {
                        log.setMensagemLogPostagem("Nenhuma rejeição registrada.");
                    }
                    try {
                        logPostagemRepository.save(log);
                    } catch (Exception saveEx) {
                        System.err.println("[LogPostagem] Falha ao salvar log para ocorrenciaId=" + ocorrenciaId);
                        saveEx.printStackTrace();
                        progresso.fatalError = "Erro ao salvar log: " + saveEx.getMessage();
                    }
                }
            } catch (Exception ex) {
                System.err
                        .println("[LogPostagem] Erro inesperado ao criar/salvar log para ocorrenciaId=" + ocorrenciaId);
                ex.printStackTrace();
                progresso.fatalError = "Erro inesperado: " + ex.getMessage();
            }
        }).start();
    }

    public ProgressoDisparo getProgresso(Long ocorrenciaId) {
        return progressoMap.getOrDefault(ocorrenciaId, null);
    }
}
