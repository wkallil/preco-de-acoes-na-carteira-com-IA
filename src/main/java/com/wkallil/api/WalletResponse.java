package com.wkallil.api;

import com.wkallil.model.Share;

import java.util.List;

public record WalletResponse(List<Share> shares) {
}
