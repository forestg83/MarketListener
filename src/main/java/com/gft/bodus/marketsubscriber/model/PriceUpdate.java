package com.gft.bodus.marketsubscriber.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * unique id, instrument name, bid, ask and timestamp.
 */
public record PriceUpdate(long id, MarketPair pair, BigDecimal bid, BigDecimal ask, LocalDateTime timestamp) {
}
