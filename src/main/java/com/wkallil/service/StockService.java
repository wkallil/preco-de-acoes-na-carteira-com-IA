package com.wkallil.service;

import com.wkallil.api.DailyStockData;
import com.wkallil.api.StockData;
import com.wkallil.api.StockRequest;
import com.wkallil.api.StockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Service
public class StockService implements Function<StockRequest, StockResponse> {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final RestClient restClient;

    public StockService(RestClient restClient) {
        this.restClient = restClient;
    }

    @Value("${TWELVE_DATA_KEY}")
    private String apiKey;

    @Override
    public StockResponse apply(StockRequest stockRequest) {

        logger.info("Buscando dados da ação: {}", stockRequest.company());
        logger.debug("Chave API configurada: {}", apiKey != null ? "SIM" : "NÃO");

        StockData data = null;
        try {
            data = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("symbol", stockRequest.company())
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
            throw new RuntimeException("Resposta nula da API TwelveData para " + stockRequest.company());
        }

        if (data.getMessage() != null && !data.getMessage().isEmpty()) {
            logger.error("Erro da API TwelveData: {}", data.getMessage());
            throw new RuntimeException("Erro da API TwelveData: " + data.getMessage());
        }

        logger.info("Status da resposta: {}", data.getStatus());
        logger.debug("Meta: {}", data.getMeta());
        logger.debug("Valores recebidos: {}", data.getValues());

        if (data.getValues() == null || data.getValues().isEmpty()) {
            logger.warn("API retornou lista vazia para símbolo: {}", stockRequest.company());
            throw new RuntimeException("Nenhum dado de ação encontrado para o símbolo '" + stockRequest.company() +
                    "'. Verifique se o símbolo é válido e se a sua chave de API está correta.");
        }

        DailyStockData latestData = data.getValues().getFirst();

        logger.info("Último preço para {}: {} USD no dia {}",
                stockRequest.company(), latestData.getClose(), latestData.getDatetime());

        return new StockResponse(Float.parseFloat(latestData.getClose()));
    }
}
