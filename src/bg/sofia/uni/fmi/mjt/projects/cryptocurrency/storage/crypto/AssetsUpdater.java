package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto;


import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.errorhandling.CryptoLogger;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.errorhandling.exceptions.CryptoCurrencyClientException;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.dto.Asset;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


import java.util.Arrays;


public class AssetsUpdater implements Runnable {

    private static final Gson GSON = new Gson();
    private static final String URL = "https://rest.coinapi.io/v1/assets";
    private static final String API_KEY = "YOUR-TEST-KEY-HERE";  // get a free test key from the CoinAPI - https://www.coinapi.io/pricing?apikey
    private static final String FILE_TO_LOG = "serverRequestLogs.txt";
    private final OkHttpClient client = new OkHttpClient();
    private CryptoData cryptoData;
    private boolean serverClosed = false;

    public AssetsUpdater(CryptoData cryptoData) {
        this.cryptoData = cryptoData;
    }

    @Override
    public void run() {
        while (!serverClosed) {

            Asset[] newAssets;
            String json = null;
            try {
                json = getJsonFromRequest();
            } catch (Exception e) {
                CryptoLogger.logServerError(e, "Problem with the JSON.", FILE_TO_LOG);
            }
            newAssets = GSON.fromJson(json, Asset[].class);
            newAssets = Arrays.stream(newAssets)
                    .filter(Asset::isCrypto)
                    .filter(a -> a.getPrice() > Double.MIN_NORMAL)
                    .toArray(Asset[]::new);
            cryptoData.updateCryptoData(newAssets);
            try {
                Thread.sleep(30 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String getJsonFromRequest() throws Exception {

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("X-CoinAPI-Key", API_KEY)
                .build();
        String logMsg = null;
        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();    // no need to null check the result of body(): it is annotated @Nullable but its javadoc states that "Returns a non-null value if this response was ... returned from Call.execute()" which is the case above; see https://square.github.io/okhttp/3.x/okhttp/okhttp3/Response.html#body--

            if (response.code() == 200) {
                return body.string();

            } else if (response.code() >= 400) {
                logMsg = "Error occured when requesting the server (Error code >=400).";
                throw new CryptoCurrencyClientException(logMsg);
            } else {
                logMsg = "Unexpected response code " + response.code()
                        + " from coinapi server."
                        + System.lineSeparator();
                throw new CryptoCurrencyClientException(logMsg);
            }
        }
    }

    public void closeServer() {
        serverClosed = true;
    }
}
