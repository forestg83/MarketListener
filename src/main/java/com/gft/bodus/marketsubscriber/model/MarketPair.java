package com.gft.bodus.marketsubscriber.model;

/**
 * Only three pairs were specified therefore I decided to go with Enum.
 */
public enum MarketPair {
    EUR_JPY("EUR_JPY"), EUR_USD("EUR_USD"), GBP_USD("EUR_USD");

    public final String name;

    MarketPair(String name) {
        this.name = name;
    }
}
