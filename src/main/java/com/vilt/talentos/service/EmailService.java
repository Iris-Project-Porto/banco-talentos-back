package com.vilt.talentos.service;

import com.vilt.talentos.config.BrevoProperties;
import com.vilt.talentos.dto.BrevoDtos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final WebClient brevoWebClient;
    private final BrevoProperties brevoProperties;
    private final TemplateEngine templateEngine;

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendHtmlEmail(List.of(to), subject, htmlContent);
    }

    @Async
    public void sendTemplatedEmail(List<String> recipients, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);
        sendHtmlEmail(recipients, subject, htmlContent);
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
