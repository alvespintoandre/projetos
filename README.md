# Portfólio de projetos (API)

API REST em **Spring Boot 3** para gestão de portfólio de projetos, com JPA/Hibernate, PostgreSQL, Swagger/OpenAPI, Spring Security (HTTP Basic com usuários e **senha com hash** na tabela `app_users`), tratamento global de exceções, paginação/filtros na listagem e testes automatizados nas regras de negócio principais.

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional, para subir o PostgreSQL via `docker-compose`)

## Como executar o banco (PostgreSQL)

Na raiz do projeto:

```bash
docker compose up -d
```

Credenciais padrão alinhadas ao `application.yml`:

| Variável | Valor padrão |
|----------|----------------|
| Host | `localhost` |
| Porta | `5432` |
| Banco | `portfolio` |
| Usuário | `portfolio` |
| Senha | `portfolio` |

Você pode sobrescrever com variáveis de ambiente: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

## Como executar a aplicação

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080` (configurável com `SERVER_PORT`).

### Autenticação

Todas as rotas **sob** `/api` (exceto documentação OpenAPI) exigem **HTTP Basic**. Usuários de login ficam na tabela **`app_users`**; a senha é armazenada com **hash** (delegating password encoder do Spring).

**Primeira execução:** se não existir nenhum usuário no banco, é criado um **ADMIN** com credenciais de *bootstrap*:

| Variável | Padrão |
|----------|--------|
| `APP_USER` | `admin` |
| `APP_PASSWORD` | `admin123` |

Configuráveis em `application.yml` em `app.security.bootstrap.admin-username` / `admin-password`.

**Papéis:** `USER` (acesso às rotas gerais como projetos e relatórios) e `ADMIN` (inclui gestão de usuários).

### Admin — usuários da API

Somente **ADMIN** (HTTP Basic). Corpo JSON usa `role`: `USER` ou `ADMIN`.

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/admin/usuarios` | Lista usuários de login (sem senha) |
| `GET` | `/api/admin/usuarios/{id}` | Busca por id |
| `POST` | `/api/admin/usuarios` | Cria usuário (`username`, `password`, `role`) — senha gravada com hash |

## Swagger / OpenAPI (documentação interativa)

URL para testes: https://projetos-nmue.onrender.com/swagger-ui/index.html (user: admin, senha: admin123)

Com a aplicação rodando (porta padrão **8080**), abra no navegador:

| O quê | URL (local) |
|--------|----------------|
| **Swagger UI** (tela com os endpoints) | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **Especificação OpenAPI** (JSON) | [http://localhost:8080/api-docs](http://localhost:8080/api-docs) |

- Se mudou a porta: troque `8080` por `SERVER_PORT` (ex.: `http://localhost:9090/swagger-ui.html`).
- A **página do Swagger** em si costuma abrir **sem** login. Já as **chamadas de teste** (`Execute`) para `/api/...` exigem **HTTP Basic** (usuários cadastrados em `app_users`). No Swagger use **Authorize** com esquema **basicAuth** (`admin` / `admin123` no primeiro uso, ou outro usuário que você tenha criado). Rotas `/api/admin/...` exigem usuário com papel **ADMIN**.

## Endpoints

### Membros — API externa mockada

Cadastro e consulta de membros **somente** por esta API (conforme regra do desafio). Exige usuário de aplicação com papel **ADMIN** (mesmo critério de `/api/admin/...`).

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `POST` | `/api/externo/membros` | Cria membro (`nome`, `atribuicao`: FUNCIONARIO, GERENTE ou OUTRO) |
| `GET` | `/api/externo/membros` | Lista todos os membros |
| `GET` | `/api/externo/membros/{id}` | Busca membro por id |

### Projetos

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/projetos` | Lista paginada; filtros opcionais: `nome` (contém, ignorando maiúsculas), `status` (enum, ex.: `EM_ANALISE`). Paginação: `page`, `size`, `sort` |
| `GET` | `/api/projetos/{id}` | Detalhe do projeto (inclui **classificação de risco** calculada) |
| `POST` | `/api/projetos` | Cria projeto; status inicial fixo **EM_ANALISE**; corpo inclui `membrosAlocadosIds` (1–10 ids de **funcionários**) |
| `PUT` | `/api/projetos/{id}` | Atualiza dados cadastrais (não substitui lista de membros) |
| `PUT` | `/api/projetos/{id}/alocacoes` | Substitui membros alocados (1–10 funcionários) |
| `PATCH` | `/api/projetos/{id}/status` | Transição de status (`novoStatus`): sequência obrigatória ou `CANCELADO` a qualquer momento |
| `DELETE` | `/api/projetos/{id}` | Remove projeto **exceto** se status for `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO` |

**Valores de `status` (query ou corpo):**  
`EM_ANALISE`, `ANALISE_REALIZADA`, `ANALISE_APROVADA`, `INICIADO`, `PLANEJADO`, `EM_ANDAMENTO`, `ENCERRADO`, `CANCELADO`.

### Relatório do portfólio

| Método | Caminho | Descrição |
|--------|---------|-----------|
| `GET` | `/api/relatorios/portfolio` | Resumo: quantidade e total orçado por status, média de duração (dias) dos encerrados com datas, total de membros únicos alocados |

## Regras de negócio implementadas (resumo)

- **Risco:** calculado por orçamento e prazo (meses entre início e previsão de término): baixo / médio / alto conforme faixas do desafio.
- **Status:** ordem fixa; não permite pular etapas; `CANCELADO` permitido a qualquer momento; ao ir para `ENCERRADO`, preenche `dataRealTermino` se estiver vazia.
- **Exclusão:** bloqueada para `INICIADO`, `EM_ANDAMENTO`, `ENCERRADO`.
- **Membros:** apenas **FUNCIONARIO** nas alocações; 1 a 10 por projeto; no máximo **3 projetos ativos** (status diferentes de encerrado/cancelado) por membro.
- **Criação de projeto:** sempre inicia em `EM_ANALISE`.

## Testes

Execute:

```bash
mvn test
```

Relatório de cobertura JaCoCo (após os testes):

```bash
mvn test jacoco:report
# relatório em target/site/jacoco/index.html
```

### O que cada classe de teste cobre

| Classe | Objetivo |
|--------|----------|
| `RiskCalculationServiceTest` | Cenários de classificação de risco (faixas de orçamento e prazo, limites como 500 mil e prazo; 6 meses). |
| `ProjectStatusTest` | Transições válidas/inválidas, `CANCELADO`, bloqueio de exclusão por status. |
| `MemberLookupServiceTest` | Apenas funcionários na alocação; limite de 3 projetos ativos por membro. |
| `ProjectServiceTest` | Criação persiste `EM_ANALISE`; exclusão e transição inválida geram erros de negócio; projeto inexistente retorna não encontrado. |
| `ProjetosApplicationTests` | Sobe o contexto Spring com perfil `test` (H2 em modo PostgreSQL). |

Perfil de testes: `src/test/resources/application-test.yml` (H2 em memória, mesmo esquema conceitual).

## Estrutura em camadas

- `web` — controllers REST e `GlobalExceptionHandler`
- `service` — regras de negócio, relatório, integração com membros externos
- `repository` — Spring Data JPA + especificações para filtros
- `domain` — entidades e enums
- `dto` — contratos de API
