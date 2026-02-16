package com.wkallil.tools;

import com.wkallil.api.DailyShareQuote;
import com.wkallil.api.DailyStockData;
import com.wkallil.api.StockData;
import com.wkallil.api.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.util.List;

public class StockTools {


    private static final Logger logger = LoggerFactory.getLogger(StockTools.class);

    private final RestClient restClient;

    @Value("${TWELVE_DATA_KEY}")
    private String apiKey;

    public StockTools(RestClient restClient) {
        this.restClient = restClient;
    }

    @Tool(description = "Obtém o preço mais recente de uma ação. Use APENAS os símbolos retornados pela ferramenta getNumberOfShares.")
    public StockResponse getLatestStockPrice(@ToolParam(description = "Símbolo da ação (exemplo: AAPL, PBR, NVDA). Use EXATAMENTE o valor do campo 'company' retornado por getNumberOfShares.") String company) {

        logger.info("Preços mais recente para: {}", company);

        StockData data = null;
        try {
            data = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("symbol", company)
                            .queryParam("interval", "1day")
                            .queryParam("outputsize", "1")
                            .queryParam("apikey", apiKey)
                            .build()
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> {
                                logger.error("Erro HTTP ao consultar TwelveData: {}", response.getStatusCode());
                                logger.error("URL: {}", request.getURI());
                                throw new RuntimeException("Erro HTTP ao consultar TwelveData: " + response.getStatusCode());
                            })
                    .body(StockData.class);
        } catch (Exception e) {
            logger.error("Exceção ao fazer requisição para TwelveData: ", e);
            throw new RuntimeException("Falha na requisição à API TwelveData: " + e.getMessage(), e);
        }

        if (data == null) {
            throw new RuntimeException("Resposta nula da API TwelveData para " + company);
        }

        if (data.getMessage() != null && !data.getMessage().isEmpty()) {
            logger.error("Erro da API TwelveData: {}", data.getMessage());
            throw new RuntimeException("Erro da API TwelveData: " + data.getMessage());
        }

        logger.info("Status da resposta: {}", data.getStatus());
        logger.debug("Meta: {}", data.getMeta());
        logger.debug("Valores recebidos: {}", data.getValues());

        if (data.getValues() == null || data.getValues().isEmpty()) {
            logger.warn("API retornou lista vazia para símbolo: {}", company);
            throw new RuntimeException("Nenhum dado de ação encontrado para o símbolo '" + company +
                    "'. Verifique se o símbolo é válido e se a sua chave de API está correta.");
        }

        DailyStockData latestData = data.getValues().getFirst();

        logger.info("Último preço para {}: {} USD no dia {}",
                company, latestData.getClose(), latestData.getDatetime());

        return new StockResponse(Float.parseFloat(latestData.getClose()));
    }

    @Tool(description = "Obtém os preços históricos de uma ação para um número específico de dias. Use APENAS os símbolos retornados pela ferramenta getNumberOfShares.")
    public List<DailyShareQuote> getHistoricalStockPrices(@ToolParam(description = "Símbolo da ação (exemplo: AAPL, PBR, NVDA). Use EXATAMENTE o valor do campo 'company' retornado por getNumberOfShares.") String company,
                                                          @ToolParam(description = "Procure no período de dias") int days) {

        logger.info("Obtém os preços históricos: {} por: {}", company, days);

        StockData data = null;
        try {
            data = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("symbol", company)
                            .queryParam("interval", "1day")
                            .queryParam("outputsize", days)
                            .queryParam("apikey", apiKey)
                            .build()
                    )
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> {
                                logger.error("Erro HTTP ao consultar TwelveData: {}", response.getStatusCode());
                                logger.error("URL: {}", request.getURI());
                                throw new RuntimeException("Erro HTTP ao consultar TwelveData: " + response.getStatusCode());
                            })
                    .body(StockData.class);
        } catch (Exception e) {
            logger.error("Exceção ao fazer requisição para TwelveData: ", e);
            throw new RuntimeException("Falha na requisição à API TwelveData: " + e.getMessage(), e);
        }

        if (data == null) {
            throw new RuntimeException("Resposta nula da API TwelveData para " + company);
        }

        if (data.getMessage() != null && !data.getMessage().isEmpty()) {
            logger.error("Erro da API TwelveData: {}", data.getMessage());
            throw new RuntimeException("Erro da API TwelveData: " + data.getMessage());
        }

        logger.info("Status da resposta: {}", data.getStatus());
        logger.debug("Meta: {}", data.getMeta());
        logger.debug("Valores recebidos: {}", data.getValues());

        if (data.getValues() == null || data.getValues().isEmpty()) {
            logger.warn("API retornou lista vazia para símbolo: {}", company);
            throw new RuntimeException("Nenhum dado de ação encontrado para o símbolo '" + company +
                    "'. Verifique se o símbolo é válido e se a sua chave de API está correta.");
        }

        return data.getValues().stream()
                .map(d -> new DailyShareQuote(company, Float.parseFloat(d.getClose()), d.getDatetime()))
                .toList();
    }
}
