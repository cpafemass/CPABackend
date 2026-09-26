# CPA Backend - Validação com Hash

Sistema backend em **Quarkus** para armazenar payloads, gerar hashes SHA-256 e validar hashes. O frontend é responsável por gerar QR codes.

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

### 1. Armazenar Payload
```bash
POST /validacao/armazenar-hash
Content-Type: text/plain

seu_payload_aqui
```

**Resposta:**
```json
{
  "message": "Payload recebido com sucesso!",
  "id": 1,
  "hash": "f5c37fda...",
  "payload": "seu_payload_aqui"
}
```

### 2. Buscar Hash
```bash
GET /validacao/buscar-hash?hash=SEU_HASH
```

### 3. Verificar Status
```bash
GET /validacao/verificar-status?hash=SEU_HASH
```

**Resposta:**
```json
{
  "hash": "f5c37fda...",
  "validado": false,
  "status": "PENDENTE",
  "mensagem": "Este hash ainda está pendente de validação"
}
```

### 4. Validar Hash (Simples)
```bash
PUT /validacao/validar-hash?hash=SEU_HASH
```

### 5. Validar Hash (Detalhado)
```bash
PUT /validacao/validar-hash/detalhado?hash=SEU_HASH
```

**Resposta:**
```json
{
  "hashValido": true,
  "id": 1,
  "hash": "f5c37fda...",
  "validado": true,
  "dataCriacao": "2026-05-15T10:30:45.123456",
  "dataValidacao": "2026-05-15T10:32:15.654321",
  "tentativasValidacao": 1,
  "tempoDecorridoMs": 90531
}
```

## 🛠️ Desenvolvimento

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
    private String hash;                    // Hash SHA-256 do payload
    private Boolean validado;               // true/false
    private LocalDateTime dataCriacao;      // Quando foi criado
    private LocalDateTime dataValidacao;    // Quando foi validado
    private Integer tentativasValidacao;    // Contador de tentativas
}
```

## 🔄 Fluxo de Uso Recomendado

```
1. Frontend armazena payload
   POST /validacao/armazenar-hash
   ← Recebe: ID + HASH

2. Frontend gera QR code com a biblioteca (ex: qrcode.js)
   Usa o HASH recebido

3. Frontend exibe QR code ao usuário

4. Usuário escaneia QR code
   ← Captura o HASH

5. Frontend valida o hash
   PUT /validacao/validar-hash?hash=HASH_CAPTURADO
   ← Hash marcado como validado
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
