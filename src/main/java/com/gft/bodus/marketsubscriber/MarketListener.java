package com.gft.bodus.marketsubscriber;

public interface MarketListener {
    /**
     * Receives message in String format and processes it further.
     *
     * @param message - String representation of CSV file
     */
    void onMessage(String message);
}
