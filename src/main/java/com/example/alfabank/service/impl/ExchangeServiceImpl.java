package com.example.alfabank.service.impl;

import com.example.alfabank.client.OpenExchangeClient;
import com.example.alfabank.model.ExchangeRates;
import com.example.alfabank.service.ExchangeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

@Service
public class ExchangeServiceImpl implements ExchangeService {

    @Value("${openexchangerates.app.id}")
    private String appId;

    @Value("${openexchangerates.base}")
    private String base;

    private ExchangeRates prevRates;
    private ExchangeRates currentRates;

    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat timeFormat;

    private final OpenExchangeClient openExchangeClient;

    public ExchangeServiceImpl(@Qualifier("date") SimpleDateFormat dateFormat,
                               @Qualifier("time") SimpleDateFormat timeFormat,
                               OpenExchangeClient openExchangeClient) {
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
        this.openExchangeClient = openExchangeClient;
    }

    @Override
    public int getKeyForTag(String currency) {
        this.refreshRates();
        Double prevCoefficient = this.getCoefficient(this.prevRates, currency);
        Double currentCoefficient = this.getCoefficient(this.currentRates, currency);
        return Double.compare(currentCoefficient,prevCoefficient);
    }

    public void refreshRates() {
        long currentTime = System.currentTimeMillis();
        this.refreshCurrentRates(currentTime);
        this.refreshPrevRates(currentTime);
    }

    private void refreshCurrentRates(long time) {
        if (
                this.currentRates == null ||
                        !timeFormat.format(Long.valueOf(this.currentRates.getTimestamp()) * 1000)
                                .equals(timeFormat.format(time))
        ) {
            this.currentRates = openExchangeClient.getLatestRates(this.appId);
        }
    }

    private void refreshPrevRates(long time) {
        Calendar prevCalendar = Calendar.getInstance();
        prevCalendar.setTimeInMillis(time);
        String currentDate = dateFormat.format(prevCalendar.getTime());
        prevCalendar.add(Calendar.DAY_OF_YEAR, -1);
        String newPrevDate = dateFormat.format(prevCalendar.getTime());
        if (
                this.prevRates == null
                        || (
                        !dateFormat.format(Long.valueOf(this.prevRates.getTimestamp()) * 1000)
                                .equals(newPrevDate)
                                && !dateFormat.format(Long.valueOf(this.prevRates.getTimestamp()) * 1000)
                                .equals(currentDate)
                )
        ) {
            this.prevRates = openExchangeClient.getHistoricalRates(newPrevDate, appId);
        }
    }

    private Double getCoefficient(ExchangeRates rates, String charCode) {
        Double result = null;
        Double targetRate = null;
        Double appBaseRate = null;
        Double defaultBaseRate = null;
        Map<String, Double> map = null;
        if (rates != null && rates.getRates() != null) {
            map = rates.getRates();
            targetRate = map.get(charCode);
            appBaseRate = map.get(this.base);
            defaultBaseRate = map.get(rates.getBase());
        }
        if (targetRate != null && appBaseRate != null && defaultBaseRate != null) {
            result = new BigDecimal(
                    (defaultBaseRate / appBaseRate) * targetRate
            )
                    .setScale(4, RoundingMode.UP)
                    .doubleValue();
        }
        return result;
    }
}
