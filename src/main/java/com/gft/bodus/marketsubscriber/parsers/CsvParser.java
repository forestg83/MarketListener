package com.gft.bodus.marketsubscriber.parsers;

import com.gft.bodus.marketsubscriber.model.MarketPair;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;

import static com.gft.bodus.marketsubscriber.model.MarketPair.*;
import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.from;
import static java.util.Collections.EMPTY_LIST;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Assumptions:
 *  1) No data validation is happening on incoming CSV strings.
 */
public class CsvParser {

    private final DateTimeFormatter formatter;

    public CsvParser() {
        this.formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss:SSS");
    }

    /**
     * 106, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001
     *
     * @param input
     * @return
     */
    public List<PriceUpdate> parseString(String input) {
        if (isBlank(input)) {
            return EMPTY_LIST;
        }

        List<PriceUpdate> priceUpdates = input.lines()
                // additional filtering for correct rows could go here
                .map(x -> x.split(","))
                .map(inputArray -> {
                    // split input line and map it PriceUpdate
                    long id = parseLong(inputArray[0].trim());
                    MarketPair pair = parseMarketPairString(inputArray[1].trim());
                    BigDecimal bid = new BigDecimal(inputArray[2].trim());
                    BigDecimal ask = new BigDecimal(inputArray[3].trim());
                    LocalDateTime timestamp = parseTimestampString(inputArray[4].trim());

                    return new PriceUpdate(id, pair, bid, ask, timestamp);
                })
                .toList();

        return priceUpdates;
    }

    private MarketPair parseMarketPairString(String input) {
        return switch (input) {
            case "EUR/JPY" -> EUR_JPY;
            case "GBP/USD" -> GBP_USD;
            case "EUR/USD" -> EUR_USD;
            default -> EUR_JPY;
        };
    }

    private LocalDateTime parseTimestampString(String inputString) {

        return from(formatter.parse(inputString));
    }
}
