package com.gft.bodus.marketsubscriber.client;

import com.gft.bodus.marketsubscriber.model.MarketPair;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * Service that can be used by REST Controller that receives data from MarketListener.
 * <p>
 */
public class HttpService {

    private ConcurrentHashMap<MarketPair, PriceUpdate> pricesMap = new ConcurrentHashMap<>(MarketPair.values().length);

    public void pushPriceUpdate(PriceUpdate priceUpdate) {
        pricesMap.put(priceUpdate.pair(), priceUpdate);
    }

    public Optional<PriceUpdate> getPriceUpdateByMarketPair(MarketPair pair) {
        return ofNullable(pricesMap.get(pair));
    }

    public Collection<PriceUpdate> getAllPrices() {
        return pricesMap.values();
    }
}
