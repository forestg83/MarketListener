package com.gft.bodus.marketsubscriber;

import com.gft.bodus.marketsubscriber.client.HttpComponent;
import com.gft.bodus.marketsubscriber.model.MarketPair;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;
import com.gft.bodus.marketsubscriber.parsers.CsvParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.stream.Collectors.groupingBy;

/**
 * Assumptions:
 * 1) this code will not be multithreaded,
 * 2) price updates can arrive out of order,
 * 3) clients are interested only in the latest price updates by timestamp in sent batch.
 */
public class MarketListenerImpl implements MarketListener {

    // used to keep latest PriceUpdates by MarketPair
    private final HashMap<MarketPair, LocalDateTime> latestPricesMap = new HashMap<>();

    private final CsvParser csvParser;
    private final HttpComponent httpComponent;
    private final BigDecimal commisionRate;

    public MarketListenerImpl(CsvParser csvParser, HttpComponent httpComponent, BigDecimal commisionRate) {
        this.csvParser = csvParser;
        this.httpComponent = httpComponent;
        this.commisionRate = commisionRate;
    }

    @Override
    public void onMessage(String message) {
        // parse incoming CSV file
        List<PriceUpdate> priceUpdateList = csvParser.parseString(message);

        // parse check if timestamp is latest
        List<PriceUpdate> latestPriceUpdates = getLastPriceUpdatesByMarketPair(priceUpdateList);

        latestPriceUpdates.stream()
                // push update to latest PriceUpdates map
                .filter(x -> checkAndUpdateLatestTimestamp(x.pair(), x.timestamp()))

                // calculate commision
                .map(x -> new PriceUpdate(
                        x.id(),
                        x.pair(),
                        substractBidCommision(x.bid()),
                        addAskCommision(x.ask()),
                        x.timestamp()))

                // push update to HTTP clients
                .forEach(httpComponent::pushPriceUpdate);
    }

    private BigDecimal substractBidCommision(BigDecimal input) {
        BigDecimal calculatedCommision = input.multiply(commisionRate);
        return input.subtract(calculatedCommision);
    }

    private BigDecimal addAskCommision(BigDecimal input) {
        BigDecimal calculatedCommision = input.multiply(commisionRate);
        return input.add(calculatedCommision);
    }

    /**
     * Returns latest PriceUpdate by MarketPair
     *
     * @param priceUpdates - list of PriceUpdates to be filtered.
     * @return
     */
    private List<PriceUpdate> getLastPriceUpdatesByMarketPair(List<PriceUpdate> priceUpdates) {
        // group PriceUpdates by MarketPair
        Map<MarketPair, List<PriceUpdate>> grouppedPriceUpdates = priceUpdates.stream()
                .collect(groupingBy(PriceUpdate::pair));

        // return list of latest price updates from input list
        List<PriceUpdate> filteredPriceUpdates = grouppedPriceUpdates.values()
                .stream()
                // get the latest PriceUpdates
                .map(pairs -> pairs.stream()
                        .reduce(maxBy(comparing(PriceUpdate::timestamp))))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        return filteredPriceUpdates;
    }

    private boolean checkAndUpdateLatestTimestamp(MarketPair pair, LocalDateTime newTimestamp) {
        LocalDateTime currentTimestamp = latestPricesMap.get(pair);
        if (currentTimestamp == null || newTimestamp.isAfter(currentTimestamp)) {
            latestPricesMap.put(pair, newTimestamp);
        }
        return true;
    }
}
