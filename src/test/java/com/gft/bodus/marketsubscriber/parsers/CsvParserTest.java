package com.gft.bodus.marketsubscriber.parsers;

import com.gft.bodus.marketsubscriber.MarketListenerImpl;
import com.gft.bodus.marketsubscriber.model.MarketPair;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;

import static com.gft.bodus.marketsubscriber.model.MarketPair.EUR_JPY;
import static com.gft.bodus.marketsubscriber.model.MarketPair.EUR_USD;
import static java.time.LocalDateTime.of;
import static java.time.temporal.ChronoUnit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CsvParserTest {

    @Test
    @DisplayName("Should handle correct non-empty data.")
    public void shouldSendTheLatestPrices() {

        // given
        CsvParser parser = new CsvParser();

        String csvString = """
                106, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001
                107, EUR/JPY, 119.60,119.90,01-06-2020 12:01:02:002
                108, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:002
                109, GBP/USD, 1.2499,1.2561,01-06-2020 12:01:02:100
                110, EUR/JPY, 119.61,119.91,01-06-2020 12:01:02:110
                """;

        // when
        List<PriceUpdate> parsedData = parser.parseString(csvString);

        // then
        assertEquals(5, parsedData.size());
        assertEquals(new PriceUpdate(106, EUR_USD, new BigDecimal("1.1000"), new BigDecimal("1.2000"),
                        of(2020, 6, 1, 12, 01, 01).plus(1, MILLIS)),
                parsedData.get(0));

        assertEquals(new PriceUpdate(110, EUR_JPY, new BigDecimal("119.61"), new BigDecimal("119.91"),
                        of(2020, 6, 1, 12, 01, 02).plus(110, MILLIS)),
                parsedData.get(4));
    }

    @Test
    @DisplayName("Should handle empty CSV")
    public void shouldhandleEmptyCSV() {

        // given
        CsvParser parser = new CsvParser();

        String csvString = StringUtils.EMPTY;

        // when
        List<PriceUpdate> parsedData = parser.parseString(csvString);

        // then
        assertEquals(0, parsedData.size());
    }
}
