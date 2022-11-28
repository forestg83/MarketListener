package com.gft.bodus.marketsubscriber;

import com.gft.bodus.marketsubscriber.client.HttpService;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;
import com.gft.bodus.marketsubscriber.parsers.CsvParser;

import java.math.BigDecimal;
import java.util.List;

/**
 * Assumptions:
 * 1) price updates can arrive out of order,
 */
public class MarketListenerImpl implements MarketListener {

    private final CsvParser csvParser;
    private final HttpService httpComponent;
    private final BigDecimal commisionRate;

    public MarketListenerImpl(CsvParser csvParser, HttpService httpComponent, BigDecimal commisionRate) {
        this.csvParser = csvParser;
        this.httpComponent = httpComponent;
        this.commisionRate = commisionRate;
    }

    @Override
    public void onMessage(String message) {
        // parse incoming CSV file
        List<PriceUpdate> priceUpdateList = csvParser.parseString(message);

        // sprawdzić czy wykonują się po kolei czy najpierw map wszystkich a potem forEach
        for (PriceUpdate pu : priceUpdateList) {
            httpComponent.pushPriceUpdate(new PriceUpdate(
                    pu.id(),
                    pu.pair(),
                    substractBidCommision(pu.bid()),
                    addAskCommision(pu.ask()),
                    pu.timestamp()));
        }
    }

    private BigDecimal substractBidCommision(BigDecimal input) {
        BigDecimal calculatedCommision = input.multiply(commisionRate);
        return input.subtract(calculatedCommision);
    }

    private BigDecimal addAskCommision(BigDecimal input) {
        BigDecimal calculatedCommision = input.multiply(commisionRate);
        return input.add(calculatedCommision);
    }
}
