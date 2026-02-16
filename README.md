# Preços de ações na carteira usando Spring AI e TwelveData

Projeto demo com Spring Boot + Spring AI para calcular o valor de uma carteira de acoes usando dados da TwelveData e um banco Postgres.

## Tecnologias usadas
- Java 21
- Spring Boot (Web MVC, Data JPA)
- Spring AI (modelo Mistral)
- PostgreSQL + Flyway
- TwelveData API (series temporais de ações)
- Flyway para migrações
- Maven

## Como o projeto funciona
A API expõe endpoints em `/ai/*` que chamam ferramentas (tools) do Spring AI. Essas tools buscam os dados no banco (carteira) e consultam a TwelveData para obter precos de acoes.

### Fluxo dos endpoints
- `GET /ai/wallet`
  - O LLM recebe um prompt com instruções.
  - Ele chama a tool `numberOfShares` para buscar as ações da carteira no banco.
  - Em seguida chama `latestStockPrice` para cada símbolo.
  - Monta a tabela com o valor total da carteira.

- `GET /ai/wallet-tool`
  - Fluxo parecido com o endpoint acima, mas registrando as tools via `.tools(stockTools, walletTools)`.
  - O LLM chama `getNumberOfShares()`.
  - Para cada item, usa o campo `company` como símbolo.
  - Chama `getLatestStockPrice(company)` e calcula o total.

- `GET /ai/wallet/highest-day/{days}`
  - O LLM chama `getNumberOfShares()`.
  - Para cada símbolo, chama `getHistoricalStockPrices(company, days)`.
  - Calcula o valor total da carteira por dia e retorna o maior valor e a data.

## Configuração
Variáveis de ambiente usadas no `application.yaml`:
- `MISTRAL_AI_KEY` (chave da Mistral)
- `TWELVE_DATA_KEY` (chave da TwelveData)
- `DB_USERNAME` / `DB_PASSWORD` (Postgres)

O banco esperado e `ai_tools_db`. O `docker-compose.yml` já sobe o Postgres com esse nome.

## Como rodar (básico)
1. Suba o Postgres (opcional): `docker compose up -d`
2. Configure as variáveis de ambiente.
3. Rode o app com Maven/IDE.

## Observações
- As migrações ficam em `src/main/resources/db/migration`.
- O símbolo da ação vem do campo `company` salvo no banco.

