package com.wkallil.config;

import com.wkallil.api.StockRequest;
import com.wkallil.api.StockResponse;
import com.wkallil.api.WalletResponse;
import com.wkallil.repository.WalletRepository;
import com.wkallil.service.StockService;
import com.wkallil.service.WalletService;
import com.wkallil.setting.ApiSettings;
import com.wkallil.tools.StockTools;
import com.wkallil.tools.WalletTools;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestClient;

import java.util.function.Function;
import java.util.function.Supplier;

@Configuration
public class WalletConfig {

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(ApiSettings.TWELVE_DATA_BASE_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Description("Número de ações por cada empresa na minha carteira")
    public Supplier<WalletResponse> numberOfShares(WalletRepository repository) {
        return new WalletService(repository);
    }

    @Bean
    @Description("Preços mais recentes na carteira")
    public Function<StockRequest, StockResponse> latestStockPrice(RestClient restClient) {
        return new StockService(restClient);
    }

    @Bean
    public WalletTools walletTools(WalletRepository repository) {
        return new WalletTools(repository);
    }

    @Bean
    public StockTools stockTools() {
        return new StockTools(restClient());
    }
}
