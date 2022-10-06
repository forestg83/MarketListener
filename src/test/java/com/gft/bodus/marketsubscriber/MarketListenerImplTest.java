package com.gft.bodus.marketsubscriber;

import com.gft.bodus.marketsubscriber.client.HttpComponent;
import com.gft.bodus.marketsubscriber.model.PriceUpdate;
import com.gft.bodus.marketsubscriber.parsers.CsvParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.gft.bodus.marketsubscriber.model.MarketPair.*;
import static java.time.LocalDateTime.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarketListenerImplTest {

    @Mock
    private HttpComponent httpComponent;

    @Captor
    private ArgumentCaptor<PriceUpdate> priceUptadeCaptor;

    @BeforeEach
    public void setup() {
        reset(httpComponent);
    }

    @Test
    @DisplayName("Should send send the latest prices.")
    public void shouldSendTheLatestPrices() {
        // given
        doNothing().when(httpComponent).pushPriceUpdate(priceUptadeCaptor.capture());
        MarketListenerImpl marketListener = new MarketListenerImpl(new CsvParser(), httpComponent, new BigDecimal("0.001"));

        // when
        marketListener.onMessage("""
                106, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001
                107, EUR/JPY, 119.60,119.90,01-06-2020 12:01:02:002
                108, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:002
                109, GBP/USD, 1.2499,1.2561,01-06-2020 12:01:02:100
                110, EUR/JPY, 119.61,119.91,01-06-2020 12:01:02:110
                """);

        // then
        verify(httpComponent, times(3)).pushPriceUpdate(any());
        List<PriceUpdate> priceUpdates = priceUptadeCaptor.getAllValues()
                .stream()
                .sorted(comparing(PriceUpdate::id))
                .toList();

        assertEquals(new PriceUpdate(106, EUR_USD, new BigDecimal("1.0989000"), new BigDecimal("1.2012000"),
                        of(2020, 6, 1, 12, 01, 01).plus(001, MILLIS)),
                priceUpdates.get(0));

        assertEquals(new PriceUpdate(109, GBP_USD, new BigDecimal("1.2486501"), new BigDecimal("1.2573561"),
                        of(2020, 6, 1, 12, 01, 02).plus(100, MILLIS)),
                priceUpdates.get(1));

        assertEquals(new PriceUpdate(110, EUR_JPY, new BigDecimal("119.49039"), new BigDecimal("120.02991"),
                        of(2020, 6, 1, 12, 01, 02).plus(110, MILLIS)),
                priceUpdates.get(2));
    }

    @Test
    @DisplayName("Should send send the latest prices.")
    public void shouldSendTheLatestPricesIfOutOfOrder() {
        // given
        doNothing().when(httpComponent).pushPriceUpdate(priceUptadeCaptor.capture());
        MarketListenerImpl marketListener = new MarketListenerImpl(new CsvParser(), httpComponent, new BigDecimal("0.001"));

        // when
        marketListener.onMessage("""
                110, EUR/JPY, 119.61,119.91,01-06-2020 12:01:02:110
                106, EUR/JPY, 1.1000,1.2000,01-06-2020 12:01:01:001
                """);

        // then
        verify(httpComponent, times(1)).pushPriceUpdate(any());

        assertEquals(new PriceUpdate(110, EUR_JPY, new BigDecimal("119.49039"), new BigDecimal("120.02991"),
                        of(2020, 6, 1, 12, 01, 02).plus(110, MILLIS)),
                priceUptadeCaptor.getValue());
    }
}

