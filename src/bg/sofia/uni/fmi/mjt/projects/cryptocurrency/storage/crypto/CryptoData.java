package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.dto.Asset;

import java.util.HashMap;
import java.util.Map;

public class CryptoData  {

    private Map<String, Asset> assetsInfo;
    private AssetsUpdater assetsUpdater;
    private boolean assetsAssigned = false;

    public CryptoData() {
        assetsUpdater = new AssetsUpdater(this);
        Thread assetsUpdaterThread = new Thread(assetsUpdater);
        assetsUpdaterThread.start();
    }

    public void updateCryptoData(Asset[] newAssets) {
        if (assetsInfo == null) {
            assetsInfo = new HashMap<>();
        }
        synchronized (assetsInfo) {
            for (Asset a : newAssets) {
                assetsInfo.put(a.getId(), a);
            }
        }
    }

    public Map<String, Asset> getAssetsInfo() {
        synchronized (assetsInfo) {
            return assetsInfo;
        }
    }

    public void closeServer() {
        assetsUpdater.closeServer();
    }
}
