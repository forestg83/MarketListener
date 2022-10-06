package com.gft.bodus.marketsubscriber.client;

import com.gft.bodus.marketsubscriber.model.PriceUpdate;

/**
 * Dummy HTTP component that receives data from MarketListener.
 *
 * Assumptions:
 *  1) HttpComponent receives updates and stores them in key/value map.
 * */
public class HttpComponent {

    public void pushPriceUpdate(PriceUpdate priceUpdate) {
        System.out.println("Received price update: " + priceUpdate);
    }
}
