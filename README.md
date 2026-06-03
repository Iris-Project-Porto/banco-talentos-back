# ÍRIS - Banco de Talentos API

API REST responsável pelo gerenciamento do Banco de Talentos da plataforma ÍRIS.

O sistema permite o cadastro e autenticação de colaboradores, gerenciamento de perfis profissionais, avaliação automática de senioridade, administração de usuários, grupos, formulários dinâmicos e submissões, além do envio de notificações por e-mail.

---

# Tecnologias Utilizadas

- Java 21
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT Authentication
- OpenAPI / Swagger
- Lombok
- Thymeleaf
- WebFlux
- Brevo API

---

# Arquitetura

O projeto segue uma arquitetura em camadas:

Controller
↓
Service
↓
Repository
↓
Database

Principais módulos:

- Autenticação e autorização
- Gestão de usuários
- Gestão de perfis profissionais
- Avaliação automática de senioridade
- Administração de perfis
- Gestão de grupos
- Formulários dinâmicos
- Submissão de formulários
- Integração com e-mail

---

# Funcionalidades

## Autenticação

- Registro de usuários
- Verificação de e-mail por código
- Login com JWT
- Recuperação de senha
- Redefinição de senha

Regras:

- Apenas e-mails @vilt-group.com
- E-mail único
- Usuário deve pertencer a um grupo
- Acesso bloqueado até validação do e-mail

---

## Gestão de Perfis

Os colaboradores podem:

- Criar perfil profissional
- Atualizar informações
- Informar experiências
- Informar skills
- Participar do processo de avaliação automática

Ao criar ou atualizar um perfil, ele pode ser enviado para aprovação administrativa.

---

## Avaliação Automática

O sistema calcula:

- Score técnico
- Nível profissional (Júnior, Pleno ou Sênior)

A análise considera:

- Autonomia
- Stack tecnológica
- Mentoria
- Certificações
- Experiência
- Code Review
- Skills técnicas

Também existe integração opcional com IA para refinamento da avaliação.

---

## Administração

Administradores podem:

- Aprovar administradores
- Aprovar perfis
- Rejeitar perfis
- Alterar níveis manualmente
- Consultar dashboard
- Gerenciar grupos
- Gerenciar formulários

---

## Formulários Dinâmicos

O sistema permite:

- Criar formulários por grupo
- Versionar formulários
- Armazenar estrutura em JSON
- Registrar submissões dos usuários

As respostas são persistidas em JSON e vinculadas à versão utilizada.

---

## Comunicação por E-mail

Integração com Brevo para:

- Verificação de conta
- Recuperação de senha
- Aprovação de administradores
- Notificação de novos perfis
- Aprovação de perfis

---

# Estrutura do Projeto

src/main/java/com/vilt/talentos

├── controller
├── service
├── repository
├── model
├── dto
├── security
├── config
├── exception
└── util

Principais controllers:

- AuthController
- ProfileController
- AdminController
- AdminGroupController
- AdminFormController
- FormSubmissionController
- GroupController
- SkillController
- AdminSkillController
- AdminProjectController
- AdminJobPostingController
- AdminSquadController

---

# Configuração do Ambiente

## 1. Clonar o projeto

git clone <repositorio>
cd banco-talentos-back

## 2. Configurar variáveis de ambiente

Criar um arquivo .env baseado no .env.example.

DATABASE_URL=jdbc:postgresql://localhost:5432/talentos
DATABASE_USER=postgres
DATABASE_PASSWORD=password

JWT_SECRET=jwtsecret

BREVO_API_KEY=xxxxxxxx

BREVO_FROM_EMAIL=sender@vilt-group.com
BREVO_NAME=[DEV] Banco de Talentos

FRONT_URL=http://localhost:5173

ALLOWED_ORIGINS=http://localhost:5173

## 3. Executar banco de dados

Certifique-se de possuir uma instância PostgreSQL disponível.

## 4. Executar migrations

As migrations são executadas automaticamente pelo Flyway ao iniciar a aplicação.

## 5. Executar aplicação

mvn clean install

mvn spring-boot:run

ou

java -jar target/talentos-api.jar

---

# Documentação da API

Após iniciar a aplicação:

http://localhost:8080/swagger-ui.html

ou

http://localhost:8080/swagger-ui/index.html

---

# Principais Endpoints

## Autenticação

POST   /api/auth/register
POST   /api/auth/verify
POST   /api/auth/login
POST   /api/auth/forgot-password
POST   /api/auth/reset-password

## Perfil

GET    /api/profile/me
POST   /api/profile

## Administração

GET    /api/admin/dashboard
GET    /api/admin/profiles
GET    /api/admin/profiles/pendentes
GET    /api/admin/profiles/ativos
PATCH  /api/admin/profiles/{id}

## Grupos

GET    /api/v1/groups
POST   /api/admin/groups
PUT    /api/admin/groups/{id}
PATCH  /api/admin/groups/{id}/activate
PATCH  /api/admin/groups/{id}/inactivate

## Formulários

GET     /api/admin/forms
POST    /api/admin/forms
PUT     /api/admin/forms
DELETE  /api/admin/forms/{id}

GET     /api/forms/my-group
POST    /api/forms/submissions

---

# Segurança

A API utiliza:

- JWT Bearer Token
- Spring Security
- Controle de Roles:
    - ADMIN
    - RECURSO

Usuários precisam estar:

- Com e-mail validado
- Com status ACTIVE

para acessar recursos protegidos.

---

# Fluxo Básico do Sistema

1. Usuário realiza cadastro.
2. Sistema envia código de verificação por e-mail.
3. Usuário valida a conta.
4. Usuário realiza login.
5. Usuário cria ou atualiza seu perfil.
6. Perfil é enviado para aprovação administrativa.
7. Administrador revisa e aprova o perfil.
8. Perfil passa a compor o Banco de Talentos.
9. Dashboards e consultas administrativas passam a considerar o perfil aprovado.

---

# Autores

- João Delgado
- Caio Gaspar
- Caique Lima

Projeto desenvolvido para a plataforma ÍRIS - Banco de Talentos Porto.