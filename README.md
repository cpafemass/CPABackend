# CPA Backend - Códigos de validação

Sistema backend em **Quarkus** que gera códigos opacos de validação para QR Codes.
O código entregue ao cliente é um segredo aleatório de 128 bits, gerado com `SecureRandom`.
O banco armazena somente o digest SHA-256 e nunca o código original ou dados pessoais.

## 🚀 Início Rápido

### Opção 1: Com Docker Compose (Recomendado)

```bash
docker compose up --build
```

O backend estará disponível em `http://localhost:8080`
O PostgreSQL estará em `localhost:5433`

### Opção 2: Local com Postgres do Docker

1. **Inicie apenas o Postgres**
```bash
docker compose up postgres
```

2. **Rode o backend em modo dev**
```bash
./mvnw quarkus:dev
```

O backend estará em `http://localhost:8080`

### Opção 3: Totalmente Local

Se você tiver PostgreSQL instalado localmente na porta padrão `5432`, apenas:

```bash
./mvnw quarkus:dev
```

## 📋 Endpoints Disponíveis

### 1. Gerar código de validação
```bash
POST /qrcode/gerar
Content-Type: application/json

{"aceiteTermosCondicoesServico":true}
```

**Resposta:**
```json
{
  "qrCode": "codigo-opaco-base64url",
  "codigoValidacao": "codigo-opaco-base64url",
  "hash": "codigo-opaco-base64url"
}
```

O campo `hash` é mantido apenas como alias legado do segredo entregue. Ele não é o digest persistido.

### 2. Validar código
```bash
PUT /validacao/validar-hash?hash=CODIGO_VALIDACAO
```

O parâmetro `hash` é um alias legado; novos clientes devem usar `codigoValidacao` no próprio contrato.
O código é de uso único e expira após `validacao.codigo-expiracao`, configurável pela variável
`VALIDACAO_CODIGO_EXPIRACAO` (padrão: 14 dias / `PT336H`).

O endpoint `/qrcode/decodificar` não decodifica payload: códigos são opacos e não carregam CPF,
matrícula, curso ou disciplinas.

### 3. Buscar código (compatibilidade)
```bash
GET /validacao/buscar-hash?hash=SEU_HASH
```

### 4. Verificar Status
```bash
GET /validacao/verificar-status?hash=SEU_HASH
```

**Resposta:**
```json
{
  "codigoValidacao": "codigo-opaco-base64url",
  "validado": false,
  "status": "PENDENTE",
  "mensagem": "Este codigo ainda está pendente de validação"
}
```

### 5. Validar código (Detalhado)
```bash
PUT /validacao/validar-hash/detalhado?hash=SEU_HASH
```

**Resposta:**
```json
{
  "codigoValido": true,
  "id": 1,
  "codigoValidacao": "codigo-opaco-base64url",
  "validado": true,
  "dataCriacao": "2026-05-15T10:30:45.123456",
  "dataValidacao": "2026-05-15T10:32:15.654321",
  "tentativasValidacao": 1,
  "tempoDecorridoMs": 90531
}
```

## 🛠️ Desenvolvimento

### Convenção de acesso a dados

Os repositories em `src/main/java/org/femass/repository` são responsáveis por consultas e operações de persistência usando Panache. Os services mantêm as validações, regras de negócio e a orquestração dos casos de uso, além de definir os limites transacionais com `@Transactional`. Novas entidades devem seguir essa separação: consultas específicas ficam no repository correspondente e não devem ser executadas diretamente pelos services.

### Modo Dev com Live Reload
```bash
./mvnw quarkus:dev
```

Qualquer alteração no código é recarregada automaticamente.

Dev UI disponível em: `http://localhost:8080/q/dev/`

### Compilação e Packaging

**Build JAR:**
```bash
./mvnw package
```

**Rodar JAR:**
```bash
java -jar target/quarkus-app/quarkus-run.jar
```

**Build Nativo (GraalVM):**
```bash
./mvnw package -Dnative
```

## 🐘 Configuração PostgreSQL

### Via Docker Compose
Automático com credenciais:
- **Host**: `postgres` (interno) / `localhost:5433` (externo)
- **BD**: `cpa-femass`
- **User**: `postgres`
- **Password**: `Cp2af0em2as6s`

### Variáveis de Ambiente
Você pode sobrescrever as credenciais via variáveis:

```bash
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://seu-host:5432/seu-bd
export QUARKUS_DATASOURCE_USERNAME=seu-user
export QUARKUS_DATASOURCE_PASSWORD=sua-senha
./mvnw quarkus:dev
```

## 📊 Estrutura de Dados

### Entidade: Validacao

```java
@Entity
public class Validacao extends PanacheEntity {
    private String codigoDigest;             // SHA-256 do código, nunca o segredo
    private Boolean validado;               // true/false
    private LocalDateTime dataCriacao;      // Quando foi criado
    private LocalDateTime expiraEm;         // Prazo configurável
    private LocalDateTime dataValidacao;    // Quando foi validado
    private Integer tentativasValidacao;    // Contador de tentativas
}
```

## 🔄 Fluxo de Uso Recomendado

```
1. Frontend solicita um código opaco
   POST /qrcode/gerar
   ← Recebe: codigoValidacao

2. Frontend gera QR code com a biblioteca (ex: qrcode.js)
   Usa o codigoValidacao recebido

3. Frontend exibe QR code ao usuário

4. Usuário escaneia QR code
   ← Captura o codigoValidacao

5. Frontend valida o código
   PUT /validacao/validar-hash?hash=CODIGO_CAPTURADO
   ← Código marcado como validado e inutilizado
```

## 📦 Dependências Principais

- **Quarkus 3.35.3**
- **Hibernate ORM Panache**
- **PostgreSQL JDBC Driver**
- **REST Jackson** (JSON)

## ✅ Testes

Rodar a suíte de testes:

```bash
./mvnw test
```

Testes incluem validação do endpoint `/validacao`.

## 📝 Notas Importantes

### Conexão Recusada (Docker)
Se o backend rodar antes do Postgres subir completamente, espere e reexecute. O `docker-compose.yml` inclui `healthcheck` para evitar isso.

### Porta 5433 vs 5432
- **5433**: Porta mapeada no host (usada por aplicações locais)
- **5432**: Porta interna do container (usada entre serviços no Compose)

## 🚢 Deployment

### Com Docker Compose
```bash
docker compose up -d
```

Logs:
```bash
docker compose logs -f backend
```

Parar:
```bash
docker compose down
```

### Em Produção
1. Crie build nativo:
```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

2. Configure variáveis de ambiente com credenciais do BD

3. Suba a imagem Docker

## 📞 Suporte Rápido

| Problema | Solução |
|----------|---------|
| "Connection refused" | Certifique-se de que Postgres está rodando na porta 5433 (local) ou via Compose |
| "No such service" | Use `docker compose up postgres` ou `docker compose up` (sem nome do serviço) |
| "Bad source file" | Rode `./mvnw clean compile` para limpar a cache |
| Porta 8080 em uso | Mude a porta no `docker-compose.yml` ou encerre o processo que usa 8080 |

## 📄 Estrutura de Diretórios

```
cpa-backend/
├── src/
│   ├── main/
│   │   ├── java/org/femass/
│   │   │   ├── entity/Validacao.java
│   │   │   ├── service/ValidacaoService.java
│   │   │   ├── resource/ValidacaoResource.java
│   │   │   └── dto/
│   │   │       ├── ValidacaoResponseDTO.java
│   │   │       ├── ValidacaoDetailResponseDTO.java
│   │   │       ├── ValidacaoStatusDTO.java
│   │   │       └── ErrorResponseDTO.java
│   │   ├── resources/application.properties
│   │   └── docker/
│   │       └── Dockerfile.jvm
│   └── test/
├── docker-compose.yml
├── Dockerfile.compose
├── pom.xml
└── README.md
```

## 📚 Referências

- [Quarkus Documentation](https://quarkus.io/guides/)
- [Quarkus REST](https://quarkus.io/guides/rest)
- [Hibernate ORM Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [PostgreSQL JDBC](https://jdbc.postgresql.org/)

---

**Desenvolvido com ❤️ em Quarkus**
# Códigos de validação

Os códigos entregues pelo QR Code são segredos opacos de 128 bits, gerados por `SecureRandom`.
O código não contém CPF, matrícula, curso ou disciplinas. O banco armazena somente o digest
SHA-256 do segredo; por isso não existe endpoint de decodificação nem recuperação do código.
Cada código é de uso único. Códigos criados antes da migração são invalidados pela migration V4.

### Migração da chave primária de `VALIDACAO`

A migration V6 adiciona `ID` como chave primária técnica identity e mantém
`CODIGO_DIGEST` como chave de negócio com índice `UNIQUE`. Ela é compatível com
bancos que já executaram V1–V5 e não deve ser editada após aplicada.

Em caso de rollback, faça backup e confirme que não existem FKs dependentes;
depois aplique uma migration corretiva que remova a PK técnica e restaure
temporariamente a PK em `CODIGO_DIGEST`. O tratamento de códigos legados
continua sendo responsabilidade da migration V4, conforme a issue #2.
