package com.wkallil.repository;

import com.wkallil.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Share, Long> {
}
