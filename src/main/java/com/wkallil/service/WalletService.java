package com.wkallil.service;

import com.wkallil.api.WalletResponse;
import com.wkallil.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class WalletService implements Supplier<WalletResponse> {
    
    private final WalletRepository repository;

    public WalletService(WalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public WalletResponse get() {
        var shares = repository.findAll();
        return new WalletResponse(shares);
    }
}
