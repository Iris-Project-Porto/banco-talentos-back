package com.vilt.talentos.dto;

import java.util.List;

public class BrevoDtos{

    public record BrevoEmailRequest(
            BrevoSender sender,
            List<BrevoRecipient> to,
            String subject,
            String htmlContent
    ) { }

    public record BrevoSender(String name, String email) {}

    public record BrevoRecipient(String email) {}
}
