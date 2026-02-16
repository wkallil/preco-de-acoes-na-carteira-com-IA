package com.wkallil.tools;

import com.wkallil.model.Share;
import com.wkallil.repository.WalletRepository;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public class WalletTools {

    private final WalletRepository repository;

    public WalletTools(WalletRepository repository) {
        this.repository = repository;
    }

    @Tool(description = "Retorna a lista de ações (símbolos e quantidades) que estão na minha carteira. Use esta ferramenta PRIMEIRO para saber quais símbolos consultar.")
    public List<Share> getNumberOfShares() {
        return repository.findAll();
    }


}
