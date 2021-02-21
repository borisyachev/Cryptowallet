package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Asset {

    @SerializedName("asset_id")
    private String assetId;

    private String name;

    @SerializedName("type_is_crypto")
    private int typeIsCrypto;

    @SerializedName("data_start")
    private Date dataStart;

    @SerializedName("data_end")
    private Date dataEnd;

    @SerializedName("data_quote_start")
    private Date dataQuoteStart;

    @SerializedName("data_quote_end")
    private Date dataQuoteEnd;

    @SerializedName("data_orderbook_start")
    private Date dataOrderbookStart;

    @SerializedName("data_orderbook_end")
    private Date dataOrderbookEnd;

    @SerializedName("data_trade_start")
    private Date dataTradeStart;

    @SerializedName("data_trade_end")
    private  Date dataTradeEnd;

    @SerializedName("data_symbols_count")
    private int dataSymbolsCount;

    @SerializedName("volume_1hrs_usd")
    private double volume1hrsUsd;

    @SerializedName("volume_1day_usd")
    private double volume1dayUsd;

    @SerializedName("volume_1mth_usd")
    private  double volume1mthUsd;

    @SerializedName("price_usd")
    private double priceUsd;

    public Asset(String assetId, String name,
                 int typeIsCrypto, Date dataStart,
                 Date dataEnd, Date dataQuoteStart,
                 Date dataQuoteEnd, Date dataOrderbookStart,
                 Date dataOrderbookEnd, Date dataTradeStart,
                 Date dataTradeEnd, int dataSymbolsCount,
                 double volume1hrsUsd, double volume1dayUsd,
                 double volume1mthUsd, double priceUsd) {

        this.assetId = assetId;
        this.name = name;
        this.typeIsCrypto = typeIsCrypto;
        this.dataStart = dataStart;
        this.dataEnd = dataEnd;
        this.dataQuoteStart = dataQuoteStart;
        this.dataQuoteEnd = dataQuoteEnd;
        this.dataOrderbookStart = dataOrderbookStart;
        this.dataOrderbookEnd = dataOrderbookEnd;
        this.dataTradeStart = dataTradeStart;
        this.dataTradeEnd = dataTradeEnd;
        this.dataSymbolsCount = dataSymbolsCount;
        this.volume1hrsUsd = volume1hrsUsd;
        this.volume1dayUsd = volume1dayUsd;
        this.volume1mthUsd = volume1mthUsd;
        this.priceUsd = priceUsd;
    }

    public Asset() {

    }

    public boolean isCrypto() {
        return typeIsCrypto == 1;
    }

    public String getId() {
        return assetId;
    }

    public double getPrice() {
        return priceUsd;
    }

    public String getName() {
        return name;
    }

    public void setPrice_usd(double priceUsd) {
        this.priceUsd = priceUsd;
    }

    public void setAsset_id(String assetId) {
        this.assetId = assetId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
