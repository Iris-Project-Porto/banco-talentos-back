package com.vilt.talentos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vilt.talentos.dto.ProfileRequest;
import com.vilt.talentos.entity.ExperienceLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TalentEvaluationService {

    @Value("${anthropic.api-key:}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    public record Evaluation(ExperienceLevel nivel, int score, String justificativa) {}

    // ── Keywords da Matriz de Conhecimentos Porto Seguro ──────────────────────

    private static final List<String> PLENO_KEYWORDS = List.of(
        "kafka", "sqs", "sns", "redis", "flyway", "liquibase", "typescript",
        "module federation", "react query", "swr", "zustand", "redux",
        "openfeign", "virtual threads", "webflux", "reactor", "cqrs", "saga",
        "outbox", "resilience4j", "circuit breaker", "kotlin", "jest",
        "testing library", "storybook", "web vitals", "context api"
    );

    private static final List<String> SR_KEYWORDS = List.of(
        "ddd", "domain-driven", "terraform", "eks", "opentelemetry",
        "oauth2", "oidc", "keycloak", "cognito", "event-driven", "event driven",
        "cloudfront", "lambda@edge", "jmeter", "k6", "prometheus", "grafana",
        "owasp", "playwright", "spring security", "open telemetry",
        "step functions", "hpa", "autoscaling", "feature flag",
        "design system", "adr", "consumer-driven contracts", "cdc"
    );

    // ── Scoring behavioral dimensions ─────────────────────────────────────────

    private int scoreAutonomia(String v) {
        if (v == null) return 0;
        if (v.startsWith("Proativa")) return 3;
        if (v.startsWith("Alta"))     return 2;
        if (v.startsWith("Média"))    return 1;
        return 0;
    }

    private int scoreProntidao(String v) {
        if (v == null) return 0;
        if (v.startsWith("Poliglota"))    return 3;
        if (v.startsWith("Especialista")) return 2;
        if (v.startsWith("Adaptável"))    return 1;
        return 0;
    }

    private int scoreAcompanhamento(String v) {
        if (v == null) return 0;
        if (v.startsWith("Independente")) return 2;
        if (v.startsWith("Padrão"))       return 1;
        return 0;
    }

    private int scoreMentoria(Integer v) {
        if (v == null) return 0;
        if (v >= 3) return 2;
        if (v == 2) return 1;
        return 0;
    }

    private int scoreCertificacoes(String v) {
        if (v == null) return 0;
        if (v.startsWith("Mais de 5")) return 2;
        if (v.startsWith("3 a 5"))     return 1;
        return 0;
    }

    private int scoreExperiencia(Integer years) {
        if (years == null) return 0;
        if (years >= 6) return 2;
        if (years >= 3) return 1;
        return 0;
    }

    // Code Review — dimensão explícita da tabela de evolução da matriz
    private int scoreCodeReview(String v) {
        if (v == null) return 0;
        if (v.startsWith("Defino padrões")) return 2; // Sr
        if (v.startsWith("Reviso PRs"))     return 1; // Pleno
        return 0; // Jr — recebe feedback
    }

    // Skill-based scoring usando keywords da Matriz Porto Seguro
    private int scoreSkills(List<ProfileRequest.SkillEntry> skills) {
        if (skills == null || skills.isEmpty()) return 0;
        String allSkills = skills.stream()
            .map(s -> s.name() == null ? "" : s.name().toLowerCase())
            .reduce("", (a, b) -> a + " " + b);

        boolean hasSrSkill    = SR_KEYWORDS.stream().anyMatch(allSkills::contains);
        boolean hasPlenoSkill = PLENO_KEYWORDS.stream().anyMatch(allSkills::contains);

        if (hasSrSkill) return 2;
        if (hasPlenoSkill) return 1;
        return 0;
    }

    // ── Avaliação completa pela matriz ────────────────────────────────────────
    // Dimensões: autonomia(3) + prontidão(3) + acompanhamento(2) + mentoria(2)
    //           + certificações(2) + experiência(2) + codeReview(2) + skills(2) = max 18
    // Jr < 6 | Pleno 6–12 | Sr ≥ 13

    private Evaluation avaliarPorMatriz(ProfileRequest req) {
        int pontos = 0;
        var detalhes = new ArrayList<String>();

        int pAut = scoreAutonomia(req.autonomia());
        pontos += pAut;
        if (pAut >= 2) detalhes.add("autonomia " + (pAut == 3 ? "proativa" : "alta") + " (+" + pAut + ")");

        int pPron = scoreProntidao(req.prontidaoStack());
        pontos += pPron;
        if (pPron >= 2) detalhes.add("stack " + (pPron == 3 ? "poliglota" : "especialista") + " (+" + pPron + ")");

        int pAcomp = scoreAcompanhamento(req.nivelAcompanhamento());
        pontos += pAcomp;
        if (pAcomp == 2) detalhes.add("independente (+" + pAcomp + ")");

        int pMent = scoreMentoria(req.nivelMentoria());
        pontos += pMent;
        if (pMent >= 1) detalhes.add("mentoria +" + pMent);

        int pCert = scoreCertificacoes(req.certificacoesCount());
        pontos += pCert;
        if (pCert >= 1) detalhes.add("certificações +" + pCert);

        int pExp = scoreExperiencia(req.experienceYears());
        pontos += pExp;
        if (pExp == 2) detalhes.add("6+ anos de experiência (+2)");
        else if (pExp == 1) detalhes.add("3–5 anos (+1)");

        int pCR = scoreCodeReview(req.codeReviewAtuacao());
        pontos += pCR;
        if (pCR == 2) detalhes.add("define padrões de code review (+2)");
        else if (pCR == 1) detalhes.add("revisa PRs do squad (+1)");

        int pSkills = scoreSkills(req.skills());
        pontos += pSkills;
        if (pSkills == 2) detalhes.add("skills Sênior detectadas na matriz (+2)");
        else if (pSkills == 1) detalhes.add("skills Pleno detectadas na matriz (+1)");

        ExperienceLevel nivel;
        if (pontos >= 13)     nivel = ExperienceLevel.SENIOR;
        else if (pontos >= 6) nivel = ExperienceLevel.PLENO;
        else                  nivel = ExperienceLevel.JUNIOR;

        String resumo = detalhes.isEmpty()
            ? "Perfil iniciante — poucos indicadores de senioridade preenchidos."
            : "Destaques: " + String.join(", ", detalhes) + ".";

        return new Evaluation(nivel, pontos, resumo);
    }

    public Evaluation evaluate(ProfileRequest req) {
        return avaliarPorMatriz(req);
    }

    @SuppressWarnings("unused")
    private Evaluation evaluateWithAI(ProfileRequest req) {
        if (apiKey == null || apiKey.isBlank()) {
            return avaliarPorMatriz(req);
        }
        try {
            String skills = req.skills() == null ? "não informadas" :
                String.join(", ", req.skills().stream()
                    .map(s -> s.name() + " (" + s.level() + ")").toList());

            var base = avaliarPorMatriz(req);

            String prompt = """
                Você é um avaliador técnico sênior da VILT / Porto Seguro. Analise o perfil abaixo
                e classifique como Jr, Pleno ou Sr conforme a Matriz de Conhecimentos Porto Seguro.

                CRITÉRIOS DA MATRIZ:
                - Jr: executa tarefas com orientação, skills básicas (React básico, Java/Spring básico,
                  Git, Docker). Recebe code review. Pouca autonomia.
                - Pleno: features completas com autonomia. Conhece padrões como Kafka, CQRS, WebFlux,
                  Module Federation, React Query, Redis. Revisa PRs do squad. 3+ anos.
                - Sr: projeta arquitetura, define padrões de code review, usa Terraform/EKS avançado,
                  DDD, Event-driven, OpenTelemetry, OAuth2/OIDC. Referência técnica. 6+ anos.

                PERFIL:
                - Área: %s | Anos: %s | Skills: %s
                - Autonomia: %s
                - Prontidão de stack: %s
                - Mentoria (1–4): %s
                - Acompanhamento: %s
                - Certificações: %s
                - Code review: %s
                - Trilha: %s

                Score calculado pela Matriz (0–18): %d  [Jr<6 | Pleno 6–12 | Sr≥13]

                Responda APENAS com JSON válido, sem markdown:
                {"nivel":"SENIOR","score":%d,"justificativa":"texto curto explicando o nível"}
                """.formatted(
                    req.area(), req.experienceYears(), skills,
                    req.autonomia(), req.prontidaoStack(), req.nivelMentoria(),
                    req.nivelAcompanhamento(), req.certificacoesCount(),
                    req.codeReviewAtuacao(), req.trilhaCarreira(),
                    base.score(), base.score());

            var body = mapper.writeValueAsString(Map.of(
                "model", "claude-haiku-4-5-20251001",
                "max_tokens", 300,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            ));

            var request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            var response = http.send(request, HttpResponse.BodyHandlers.ofString());
            var root = mapper.readTree(response.body());
            String text = root.at("/content/0/text").asText();
            text = text.replaceAll("```json\\n?", "").replaceAll("```", "").trim();
            var result = mapper.readTree(text);
            return new Evaluation(
                ExperienceLevel.fromValue(result.get("nivel").asText(base.nivel().name())),
                result.get("score").asInt(base.score()),
                result.get("justificativa").asText(base.justificativa())
            );
        } catch (Exception e) {
            return avaliarPorMatriz(req);
        }
    }
}
