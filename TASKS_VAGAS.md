# Tasks — Módulo de Vagas (Backend)

> Contexto: o frontend já consome um `VagasContext` local. Estas tasks criam a API REST que substituirá o estado local. Após o backend pronto, o front troca `useState` por chamadas `api.createVaga(...)`, `api.getVagas()`, etc.

---

## TASK-01 — Migration: tabela `vagas`

**Arquivo:** `src/main/resources/db/migration/V9__vagas.sql`

Criar a tabela com todas as colunas necessárias:

```sql
CREATE TABLE vagas (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo          VARCHAR(255) NOT NULL,
    senioridade     VARCHAR(10)  NOT NULL CHECK (senioridade IN ('Jr', 'Pleno', 'Sr')),
    time            VARCHAR(100) NOT NULL,
    solicitante     VARCHAR(100) NOT NULL,
    tempo_contratacao VARCHAR(100),
    numero_vagas    INT NOT NULL DEFAULT 1,
    area            VARCHAR(100),
    skills          TEXT[],
    descricao       TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'Aberta'
                    CHECK (status IN ('Aberta', 'Em andamento', 'Fechada', 'Cancelada')),
    prioridade      VARCHAR(10) NOT NULL DEFAULT 'Média'
                    CHECK (prioridade IN ('Baixa', 'Média', 'Alta', 'Urgente')),
    data_abertura   DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

**Critério de aceite:** migration roda sem erro no Flyway ao subir a aplicação.

---

## TASK-02 — Entity: `Vaga.java`

**Arquivo:** `src/main/java/com/vilt/talentos/entity/Vaga.java`

```java
@Entity
@Table(name = "vagas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Senioridade senioridade;

    @Column(name = "time", nullable = false)
    private String time;

    @Column(nullable = false)
    private String solicitante;

    @Column(name = "tempo_contratacao")
    private String tempoContratacao;

    @Column(name = "numero_vagas", nullable = false)
    private Integer numeroVagas = 1;

    private String area;

    @Column(columnDefinition = "text[]")
    private String[] skills;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusVaga status = StatusVaga.ABERTA;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Prioridade prioridade = Prioridade.MEDIA;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Senioridade { Jr, Pleno, Sr }
    public enum StatusVaga  { ABERTA, EM_ANDAMENTO, FECHADA, CANCELADA }
    public enum Prioridade  { BAIXA, MEDIA, ALTA, URGENTE }
}
```

**Observação:** os valores dos enums devem ser mapeados para os labels do front (ex: `ABERTA` → `"Aberta"`) via `@JsonValue` ou `VagaResponse` DTO.

**Critério de aceite:** entidade compila e JPA valida o mapeamento sem erros de startup.

---

## TASK-03 — Repository: `VagaRepository.java`

**Arquivo:** `src/main/java/com/vilt/talentos/repository/VagaRepository.java`

```java
public interface VagaRepository extends JpaRepository<Vaga, UUID> {

    List<Vaga> findByStatus(Vaga.StatusVaga status);

    List<Vaga> findBySenioridade(Vaga.Senioridade senioridade);

    // Para o dashboard: contar vagas abertas/em andamento por nível
    long countByStatusAndSenioridade(Vaga.StatusVaga status, Vaga.Senioridade senioridade);
}
```

**Critério de aceite:** métodos derivados resolvem corretamente via Spring Data sem queries manuais.

---

## TASK-04 — DTOs: Request e Response

**Arquivos:**
- `src/main/java/com/vilt/talentos/dto/VagaRequest.java`
- `src/main/java/com/vilt/talentos/dto/VagaResponse.java`

### VagaRequest (criação e edição)

```java
public record VagaRequest(
    @NotBlank String titulo,
    @NotNull Vaga.Senioridade senioridade,
    @NotBlank String time,
    @NotBlank String solicitante,
    String tempoContratacao,
    @Min(1) int numeroVagas,
    String area,
    List<String> skills,
    String descricao,
    @NotNull Vaga.StatusVaga status,
    @NotNull Vaga.Prioridade prioridade,
    @NotNull LocalDate dataAbertura
) {}
```

### VagaResponse

Mesmo contrato do frontend. Os campos `status`, `senioridade` e `prioridade` devem ser serializados como strings legíveis (ex: `"Em andamento"`, não `"EM_ANDAMENTO"`).

```java
public record VagaResponse(
    UUID id,
    String titulo,
    String senioridade,   // "Jr" | "Pleno" | "Sr"
    String time,
    String solicitante,
    String tempoContratacao,
    int numeroVagas,
    String area,
    List<String> skills,
    String descricao,
    String status,        // "Aberta" | "Em andamento" | "Fechada" | "Cancelada"
    String prioridade,    // "Baixa" | "Média" | "Alta" | "Urgente"
    String dataAbertura   // ISO date "yyyy-MM-dd"
) {}
```

**Critério de aceite:** `VagaResponse` serializa corretamente quando chamado via Swagger.

---

## TASK-05 — Service: `VagaService.java`

**Arquivo:** `src/main/java/com/vilt/talentos/service/VagaService.java`

Métodos a implementar:

| Método | Descrição |
|--------|-----------|
| `List<VagaResponse> listar()` | Retorna todas as vagas ordenadas por `dataAbertura DESC` |
| `VagaResponse buscarPorId(UUID id)` | Busca por id ou lança `404` |
| `VagaResponse criar(VagaRequest req)` | Cria e salva nova vaga |
| `VagaResponse atualizar(UUID id, VagaRequest req)` | Atualiza vaga existente ou lança `404` |
| `void deletar(UUID id)` | Remove vaga ou lança `404` |

Regras de negócio:
- `criar`: `dataAbertura` default = hoje se não informada
- `atualizar`: não permite alterar `createdAt`
- `deletar`: soft delete é opcional (pode ser hard delete por enquanto)

**Critério de aceite:** todos os métodos cobertos por testes unitários (ver TASK-07).

---

## TASK-06 — Controller: `VagaController.java`

**Arquivo:** `src/main/java/com/vilt/talentos/controller/VagaController.java`

Endpoints sob `/api/admin/vagas` (já coberto pelo `hasAuthority("ROLE_ADMIN")` no `SecurityConfig`):

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/api/admin/vagas` | Lista todas as vagas |
| `GET` | `/api/admin/vagas/{id}` | Busca vaga por id |
| `POST` | `/api/admin/vagas` | Cria nova vaga |
| `PUT` | `/api/admin/vagas/{id}` | Atualiza vaga |
| `DELETE` | `/api/admin/vagas/{id}` | Remove vaga |

```java
@RestController
@RequestMapping("/api/admin/vagas")
@RequiredArgsConstructor
@Tag(name = "Vagas", description = "Gestão de vagas abertas")
public class VagaController {

    private final VagaService vagaService;

    @GetMapping
    public List<VagaResponse> listar() { ... }

    @GetMapping("/{id}")
    public VagaResponse buscar(@PathVariable UUID id) { ... }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VagaResponse criar(@Valid @RequestBody VagaRequest req) { ... }

    @PutMapping("/{id}")
    public VagaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody VagaRequest req) { ... }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) { ... }
}
```

**Critério de aceite:** todos os endpoints respondem corretamente via Swagger UI em `/swagger-ui.html`.

---

## TASK-07 — Testes unitários: `VagaServiceTest.java`

**Arquivo:** `src/test/java/com/vilt/talentos/service/VagaServiceTest.java`

Cenários mínimos a cobrir:

- `listar` → retorna lista mapeada corretamente
- `buscarPorId` → retorna vaga existente
- `buscarPorId` → lança `404` quando não encontrado
- `criar` → salva e retorna response com id preenchido
- `criar` → preenche `dataAbertura` com hoje quando não informada
- `atualizar` → atualiza campos corretamente
- `atualizar` → lança `404` quando vaga não existe
- `deletar` → chama `repository.deleteById`
- `deletar` → lança `404` quando vaga não existe

**Critério de aceite:** `mvn test` passa sem falhas.

---

## TASK-08 — Integração no Dashboard (`/api/admin/dashboard`)

O endpoint existente em `AdminController.getDashboard()` precisa ser estendido para incluir dados de vagas.

**Adicionar ao `DashboardResponse`:**

```java
record VagaStats(
    int total,
    int abertas,
    int emAndamento,
    int fechadas,
    int canceladas,
    Map<String, Integer> porNivel   // {"Jr": 2, "Pleno": 1, "Sr": 0}
) {}
```

**Adicionar ao response do dashboard:**
```json
{
  "total": 6,
  "ativos": 6,
  ...
  "vagas": {
    "total": 4,
    "abertas": 2,
    "emAndamento": 1,
    "fechadas": 1,
    "canceladas": 0,
    "porNivel": { "Jr": 1, "Pleno": 2, "Sr": 1 }
  }
}
```

**Critério de aceite:** o frontend pode usar `data.vagas` para alimentar os gráficos do Dashboard sem depender do `VagasContext` local.

---

## Ordem de execução sugerida

```
TASK-01 (migration)
  → TASK-02 (entity)
    → TASK-03 (repository)
      → TASK-04 (DTOs)
        → TASK-05 (service)  ← TASK-07 (testes) em paralelo
          → TASK-06 (controller)
            → TASK-08 (dashboard)
```

---

## Contrato de integração com o frontend

Após as tasks concluídas, o frontend (`src/lib/api.ts`) precisará adicionar:

```ts
getVagas: () => http.get("/admin/vagas").then(r => r.data),
createVaga: (data) => http.post("/admin/vagas", data).then(r => r.data),
updateVaga: (id, data) => http.put(`/admin/vagas/${id}`, data).then(r => r.data),
deleteVaga: (id) => http.delete(`/admin/vagas/${id}`).then(r => r.data),
```

E a página `Vagas.tsx` trocará o `useState` + `crypto.randomUUID()` por chamadas reais à API.
