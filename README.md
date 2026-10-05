# API Customer - Spring Boot CRUD

API REST para gerenciamento de clientes (customers) desenvolvida com Spring Boot 4.1.1 e Java 25.

## Tecnologias

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA
- H2 Database (persistência em arquivo)
- Gradle 9.7.1
- UUID (gerado automaticamente) para IDs
- Docker & Docker Compose
- Makefile para automação
- Swagger/OpenAPI 3.0 (SpringDoc)

## Configuração do Banco de Dados

O banco H2 está configurado para persistir dados na pasta `./data/` do projeto:
- URL: `jdbc:h2:file:./data/customerdb`
- Console H2 (local): http://localhost:9090/h2-console
- Console H2 (Docker): http://localhost:9090/h2-console
- Usuário: `sa`
- Senha: (vazia)

### Inicialização de Dados

A aplicação é configurada com um `DataInitializer` que cria automaticamente **1000 clientes** na primeira execução quando o banco está vazio. Os dados gerados incluem:
- Nomes brasileiros realistas
- Emails únicos com diferentes domínios
- Telefones com DDDs brasileiros
- Endereços de diferentes cidades do Brasil

Para reiniciar o banco e recriar os dados:
```bash
make clean-db
./gradlew bootRun
```

## Documentação da API (Swagger)

A API possui documentação interativa gerada com Swagger/OpenAPI 3.0.

### Acesso à documentação:

| Ambiente | URL |
|----------|-----|
| Local | http://localhost:9090/swagger-ui.html |
| Local (JSON) | http://localhost:9090/v3/api-docs |
| Docker | http://localhost:9090/swagger-ui.html |

A interface do Swagger UI permite testar todos os endpoints diretamente no navegador.

**Nota:** O Swagger UI está disponível após o build e execução da aplicação. O pacote `springdoc-openapi-starter-webmvc-ui` fornece a interface de documentação interativa.

## Executando a Aplicação

### Modo Desenvolvimento (Local)

```bash
./gradlew bootRun
```

A aplicação estará disponível em: http://localhost:9090

### Modo Produção (Docker)

**Pré-requisitos:**
- Docker instalado
- Docker Compose instalado

```bash
# Build e início com Docker Compose
make run

# Ou manualmente
docker-compose up -d
```

A aplicação estará disponível em: http://localhost:9090

## Makefile

O projeto inclui um Makefile com comandos automatizados:

| Comando | Descrição |
|---------|-----------|
| `make help` | Exibe todos os comandos disponíveis |
| `make build` | Build da aplicação com Gradle |
| `make test` | Executar os testes com Gradle |
| `make build-image` | Build da imagem Docker |
| `make run` | Iniciar a aplicação com Docker Compose |
| `make stop` | Parar a aplicação |
| `make clean` | Limpar builds e containers |
| `make clean-db` | Deletar banco de dados H2 (útil para reiniciar do zero) |
| `make logs` | Visualizar logs em tempo real |
| `make rebuild` | Limpar e reiniciar tudo (clean + build + run) |

Os comandos Make que usam Gradle detectam automaticamente o Java ativo do SDKMAN (`~/.sdkman/candidates/java/current`) quando `JAVA_HOME` não está definido. Para a IDE, configure o SDK do projeto e o Gradle JVM como Java 25 em **File > Project Structure > SDKs** e **Settings > Build, Execution, Deployment > Build Tools > Gradle**.

### Comando clean-db

O comando `make clean-db` é útil quando você precisa:
- Reiniciar o banco de dados do zero
- Resolver problemas de esquema ou migração
- Limpar dados de teste

```bash
# Deletar o banco H2
make clean-db

# Após deletar, a aplicação criará um novo banco na próxima execução
./gradlew bootRun
```

**Nota:** O comando verifica se os arquivos existem antes de deletá-los e exibe mensagens informativas.

## Docker

### Dockerfile

O Dockerfile utiliza multi-stage build para otimização:
- Stage 1: Build com Gradle 9.0.0 e JDK 21
- Stage 2: Runtime com Eclipse Temurin JRE 21 Alpine

### Estrutura de Imagem

- Base: `eclipse-temurin:21-jre-alpine`
- User: `app` (non-root)
- Volume: `/app/data` (persistência H2)
- Porta exposta: `9090`

### Swagger no Docker

A documentação Swagger está disponível no container na porta 9090:
- Swagger UI: http://localhost:9090/swagger-ui.html
- OpenAPI JSON: http://localhost:9090/v3/api-docs

### Comandos Docker Manuais

```bash
# Build da imagem
docker build -t apicustomer:latest .

# Executar container
docker run -p 9090:9090 --name apicustomer -v ./data:/app/data apicustomer:latest

# Parar container
docker stop apicustomer

# Remover container
docker rm apicustomer
```

## Endpoints da API

### Listar todos os clientes
```http
GET /api/customers
```

**Resposta:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "João Silva",
    "email": "joao@email.com",
    "phone": "11999999999",
    "address": "Rua A, 123",
    "createdAt": "2026-10-03T10:30:00",
    "updatedAt": "2026-10-03T10:30:00"
  }
]
```

### Buscar cliente por ID
```http
GET /api/customers/{id}
```

### Buscar cliente por email
```http
GET /api/customers/email/{email}
```

### Criar novo cliente
```http
POST /api/customers
Content-Type: application/json

{
  "name": "João Silva",
  "email": "joao@email.com",
  "phone": "11999999999",
  "address": "Rua A, 123"
}
```

**Resposta:** Status 201 (Created)

### Atualizar cliente
```http
PUT /api/customers/{id}
Content-Type: application/json

{
  "name": "João Silva Atualizado",
  "email": "joao.novo@email.com",
  "phone": "11888888888",
  "address": "Rua B, 456"
}
```

**Resposta:** Status 200 (OK)

### Deletar cliente
```http
DELETE /api/customers/{id}
```

**Resposta:** Status 204 (No Content)

## Validações

- `name`: obrigatório, máximo 100 caracteres
- `email`: obrigatório, formato válido, único no sistema
- `phone`: máximo 20 caracteres
- `address`: máximo 500 caracteres

## Estrutura do Projeto

```
src/main/java/com/example/apicustomer/
├── ApicustomerApplication.java          # Classe principal
├── config/
│   ├── OpenApiConfig.java               # Configuração do Swagger/OpenAPI
│   └── DataInitializer.java             # Inicializador de dados
├── controller/
│   └── CustomerController.java          # REST Controller
├── dto/
│   ├── CustomerRequest.java             # DTO para requisições (record)
│   └── CustomerResponse.java            # DTO para respostas (record)
├── entity/
│   └── Customer.java                    # Entidade JPA
├── repository/
│   └── CustomerRepository.java          # Repository Interface
├── service/
│   └── CustomerService.java             # Camada de serviço
└── exception/
    └── GlobalExceptionHandler.java      # Tratamento de exceções

src/test/java/com/example/apicustomer/
├── ApicustomerApplicationTests.java     # Teste de contexto da aplicação
├── config/
│   └── application-test.properties      # Configuração de testes
└── service/
    └── CustomerServiceTest.java         # Testes unitários do service
```

## Arquitetura

A API utiliza o padrão DTO (Data Transfer Object) com Java Records para separar a camada de apresentação da camada de persistência:

- **CustomerRequest**: Record utilizado para receber dados nas operações de criação e atualização
- **CustomerResponse**: Record utilizado para retornar dados nas respostas da API
- **Customer**: Entidade JPA para persistência no banco de dados

## Testando a API

Você pode usar curl, Postman ou qualquer cliente HTTP:

### Exemplo com curl:

```bash
# Criar cliente
curl -X POST http://localhost:9090/api/customers \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva","email":"joao@email.com","phone":"11999999999","address":"Rua A, 123"}'

# Listar clientes
curl http://localhost:9090/api/customers

# Buscar por ID (UUID)
curl http://localhost:9090/api/customers/550e8400-e29b-41d4-a716-446655440000

# Atualizar cliente
curl -X PUT http://localhost:9090/api/customers/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"name":"João Silva Atualizado","email":"joao@email.com","phone":"11888888888"}'

# Deletar cliente
curl -X DELETE http://localhost:9090/api/customers/550e8400-e29b-41d4-a716-446655440000
```

### Acessando Swagger UI

Abra o navegador e acesse: http://localhost:9090/swagger-ui.html

Na interface do Swagger, você pode testar todos os endpoints diretamente, visualizar requisições e respostas, e ver a documentação interativa da API.

### Console H2

Para acessar o console H2 (dentro do container):

```
http://localhost:9090/h2-console
```

Configurações:
- JDBC URL: `jdbc:h2:file:/app/data/customerdb`
- User: `sa`
- Password: (vazia)

## Build

```bash
# Compilar
./gradlew build

# Compilar sem testes
./gradlew build -x test

# Executar testes
./gradlew test

# Executar a aplicação localmente
./gradlew bootRun

# Build Docker image
make build-image

# Build e run com Docker
make rebuild

# Limpar banco de dados
make clean-db
```

## Testes

O projeto inclui testes unitários cobrindo a lógica de negócio do `CustomerService`.

### Testes Unitários

**Arquivo:** `src/test/java/com/example/apicustomer/service/CustomerServiceTest.java`

- Listar todos os clientes
- Buscar cliente por ID
- Buscar cliente por email
- Criar novo cliente (incluindo validação de email duplicado)
- Atualizar cliente (incluindo tratamento de cliente inexistente)
- Deletar cliente (incluindo tratamento de cliente inexistente)

### Estrutura de Testes

```
src/test/java/com/example/apicustomer/
├── ApicustomerApplicationTests.java     # Teste de contexto da aplicação
└── service/
    └── CustomerServiceTest.java         # Testes unitários do service
```

### Executar Testes

```bash
# Executar todos os testes
make test

# Executar apenas testes do service
./gradlew test --tests "*CustomerServiceTest"

# Ver relatório de cobertura
./gradlew test --info
```

### Banco de Dados de Teste

Os testes utilizam uma instância H2 em memória (`jdbc:h2:mem:testdb`) que é inicializada vazia e limpa após cada execução. O perfil `test` desativa o `DataInitializer` para garantir testes limpos e previsíveis.

## Solução de Problemas

### Erro de UUID ou conversão de dados

Se você encontrar erros relacionados a UUID ou conversão de dados ao iniciar a aplicação:

```bash
# Deletar o banco de dados e deixar o Hibernate recriar
make clean-db
./gradlew bootRun
```

### Porta 9090 já em uso

Se a porta 9090 estiver ocupada, você pode alterar em `src/main/resources/application.properties`:
```properties
server.port=8080
```

### Problemas com Docker

```bash
# Parar todos os containers
make stop

# Limpar completamente (containers, volumes, imagens)
make clean

# Rebuild completo
make rebuild
```

### Banco de dados corrompido

```bash
# Deletar arquivos do H2
make clean-db

# Ou manualmente
rm -f data/customerdb.mv.db data/customerdb.trace.db
```

## Licença

Este projeto é open source e está disponível sob a licença MIT.
