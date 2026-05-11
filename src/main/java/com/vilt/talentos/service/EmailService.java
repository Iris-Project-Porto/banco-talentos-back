package com.vilt.talentos.service;

import com.vilt.talentos.config.BrevoProperties;
import com.vilt.talentos.dto.BrevoDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final WebClient brevoWebClient;
    private final BrevoProperties brevoProperties;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendHtmlEmail(List.of(to), subject, htmlContent);
    }

    @Async
    public void sendHtmlEmail(List<String> recipients, String subject, String htmlContent) {
        log.info("Enviando e-mail para: {} com assunto: {}", recipients, subject);

        List<BrevoDtos.BrevoRecipient> toList = recipients.stream()
                .map(BrevoDtos.BrevoRecipient::new)
                .toList();

        BrevoDtos.BrevoEmailRequest request = new BrevoDtos.BrevoEmailRequest(
                new BrevoDtos.BrevoSender(brevoProperties.fromName(), brevoProperties.fromEmail()),
                toList,
                subject,
                htmlContent
        );

        brevoWebClient.post()
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(response -> log.info("E-mail enviado com sucesso para: {}", recipients))
                .doOnError(error -> log.error("Erro ao enviar e-mail para: {}. Erro: {}", recipients, error.getMessage()))
                .subscribe();
    }
}
