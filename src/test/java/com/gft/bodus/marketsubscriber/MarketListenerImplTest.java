package com.gft.bodus.marketsubscriber;

import com.gft.bodus.marketsubscriber.client.HttpService;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;
import com.gft.bodus.marketsubscriber.parsers.CsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collection;

import static java.util.List.of;

import static com.gft.bodus.marketsubscriber.model.MarketPair.EUR_JPY;
import static com.gft.bodus.marketsubscriber.model.MarketPair.EUR_USD;
import static com.gft.bodus.marketsubscriber.model.MarketPair.GBP_USD;
import static java.time.LocalDateTime.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MarketListenerImplTest {

    @Spy
    HttpService httpService;

    @BeforeEach
    public void setup() {
        Mockito.reset(httpService);
    }

    @Test
    @DisplayName("Should send send adjusted prices.")
    public void shouldSendAdjustedPrices() {
        // given
        MarketListenerImpl marketListener = new MarketListenerImpl(new CsvParser(), httpService, new BigDecimal("0.001"));

        // when
        marketListener.onMessage("""
                106, EUR/USD, 1.1000, 1.2000, 01-06-2020 12:01:01:001
                107, EUR/JPY, 119.60, 119.90, 01-06-2020 12:01:02:002
                108, GBP/USD, 1.2500, 1.2560, 01-06-2020 12:01:02:002
                109, GBP/USD, 1.2499, 1.2561, 01-06-2020 12:01:02:100
                110, EUR/JPY, 119.61, 119.91, 01-06-2020 12:01:02:110
                """);

        // then
        Collection<PriceUpdate> publishedData = httpService.getAllPrices();
        assertEquals(3, publishedData.size());
        verify(httpService, times(5)).pushPriceUpdate(any(PriceUpdate.class));
        assertTrue(publishedData.containsAll(of(
                new PriceUpdate(106, EUR_USD, new BigDecimal("1.0989000"), new BigDecimal("1.2012000"),
                        of(2020, 6, 1, 12, 01, 01).plus(001, MILLIS)),

                new PriceUpdate(109, GBP_USD, new BigDecimal("1.2486501"), new BigDecimal("1.2573561"),
                        of(2020, 6, 1, 12, 01, 02).plus(100, MILLIS)),

                new PriceUpdate(110, EUR_JPY, new BigDecimal("119.49039"), new BigDecimal("120.02991"),
                        of(2020, 6, 1, 12, 01, 02).plus(110, MILLIS)))));
    }

    @Test
    @DisplayName("Should omit outdated updates")
    public void shouldOmitOutdatedUpdates() {
        // given
        MarketListenerImpl marketListener = new MarketListenerImpl(new CsvParser(), httpService, new BigDecimal("0.001"));

        // when
        marketListener.onMessage("""
                110, EUR/JPY, 119.61, 119.91,01-06-2020 12:01:02:110
                107, EUR/JPY, 119.60, 119.90,01-06-2020 12:01:02:002
                """);

        // then
        Collection<PriceUpdate> publishedData = httpService.getAllPrices();
        assertEquals(1, publishedData.size());
        verify(httpService, times(2)).pushPriceUpdate(any(PriceUpdate.class));
        assertTrue(publishedData.contains(
                new PriceUpdate(110, EUR_JPY, new BigDecimal("119.49039"), new BigDecimal("120.02991"),
                        of(2020, 6, 1, 12, 01, 02).plus(110, MILLIS))
        ));
    }

}

