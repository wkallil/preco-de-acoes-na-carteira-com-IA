package com.wkallil.controller;

import com.wkallil.tools.StockTools;
import com.wkallil.tools.WalletTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class WalletController {

    private final ChatClient chatClient;

    private final StockTools stockTools;

    private final WalletTools walletTools;

    public WalletController(ChatClient.Builder chatClientBuilder,
                            StockTools stockTools,
                            WalletTools walletTools) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
        this.stockTools = stockTools;
        this.walletTools = walletTools;
    }

    @GetMapping("/wallet")
    String calculateWalletValue() {
        PromptTemplate template = new PromptTemplate("""
                IMPORTANTE: Primeiro chame a ferramenta 'numberOfShares' para obter os símbolos das ações na minha carteira.
                Depois use a ferramenta 'latestStockPrice' APENAS para consultar os símbolos que estão na carteira.
                
                Qual é o valor atual em dólares da minha carteira com base nos preços diários mais recentes das ações?
                
                Instruções adicionais:
                - Para cada ação na carteira, obtenha o preço mais recente
                - Calcule: valor total = (quantidade × preço) para cada ação
                - Soma o valor de todas as ações
                - Apresente em formato de tabela com colunas: Símbolo, Quantidade, Preço USD, Valor Total
                - Inclua o valor total da carteira no final
                """);

        return this.chatClient.prompt(
                        template.create(
                                ToolCallingChatOptions.builder()
                                        .toolNames("numberOfShares", "latestStockPrice")
                                        .build()
                        )
                )
                .call().content();
    }

    @GetMapping("/wallet-tool")
    String calculateWalletValueWithTools() {
        PromptTemplate template = new PromptTemplate("""
                Qual é o valor atual em dólares da minha carteira com base nos preços diários mais recentes das ações?
                
                INSTRUÇÕES IMPORTANTES:
                1. PRIMEIRO: Chame getNumberOfShares() para obter as ações na carteira
                2. Para cada ação retornada, use o valor EXATO do campo 'company' (que contém o símbolo da ação)
                3. Chame getLatestStockPrice(company) passando cada símbolo obtido no passo 1
                4. Calcule: valor total = (quantity × preço) para cada ação
                5. Some os valores de todas as ações
                
                Apresente em formato de tabela com colunas:
                - Símbolo
                - Quantidade
                - Preço (USD)
                - Valor Total (USD)
                
                Inclua o valor total da carteira no final.
                """);

        return this.chatClient.prompt(template.create())
                .tools(stockTools, walletTools)
                .call()
                .content();
    }

    @GetMapping("/wallet/highest-day/{days}")
    String calculateHighestValue(@PathVariable int days) {
        PromptTemplate template = new PromptTemplate("""
                Qual é o valor mais alto da minha carteira nos últimos {days} dias com base nos preços diários das ações?
                
                INSTRUÇÕES IMPORTANTES:
                1. PRIMEIRO: Chame getNumberOfShares() para obter as ações na carteira
                2. Para cada ação retornada, use o valor EXATO do campo 'company' (que contém o símbolo da ação)
                3. Chame getHistoricalStockPrices(company, days) passando cada símbolo obtido no passo 1, mas configure para obter os preços dos últimos {days} dias
                4. Para cada dia, calcule: valor total = (quantity × preço) para cada ação
                5. Compare os valores totais de cada dia e identifique o valor mais alto
                6. Apresente em formato de tabela com colunas o histórico dos valores nos últimos {days} dias por companhia (símbolo):
                - Data
                - Valor Total (USD)
                
                Apresente o valor mais alto encontrado, a data correspondente no final e o nome da companhia em negrito e por páragrafo.
                (Formate a data no formato dd/MM/yyyy, o valor em dólar em $ com 2 casas decimais e o nome da companhia usando o símbolo da ação)
                """);

        return this.chatClient.prompt(template.create(Map.of("days", days)))
                .tools(stockTools, walletTools)
                .call()
                .content();
    }
}
