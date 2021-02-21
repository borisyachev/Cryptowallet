package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Wallet implements Serializable {

    private double money;
    private Map<String, Map<Double, Double>> assets; // assetId -> (boughtOnPrice -> amount)

    public Wallet() {
        money = 0.0;
        assets = new HashMap<>();
    }

    public double getMoney() {
        return money;
    }

    public Map<String, Map<Double, Double>> getAssets() {
        return assets;
    }

    public Set<String> getPresentAssetIds() {
        return assets.keySet();
    }

    public Map<Double, Double> getAssetPurchases(String assetID) {
        return assets.get(assetID);
    }

    public void deposit(double amount) {
        money += amount;
    }

    public double getAssetAmount(String assetId) {
        double assetAmount = 0;
        Map<Double, Double> assetPurchases = getAssetPurchases(assetId);
        for (double price : assetPurchases.keySet()) {
            assetAmount += assetPurchases.get(price);
        }
        return assetAmount;
    }


    public double sell(String assetId, double price) {
        Map<Double, Double> currentAssetPurchases = assets.get(assetId);
        double assetsAmount = 0.0;

        for (double currentPrice : currentAssetPurchases.keySet()) {
            assetsAmount += currentAssetPurchases.get(currentPrice);
        }

        double profit = assetsAmount * price;
        money += profit;
        assets.remove(assetId);

        return assetsAmount;
    }

    public void sell(String assetId, double amount, double profit) {
        money += profit;
        Map<Double, Double> currentAssetPurchases = assets.get(assetId);
        var it = currentAssetPurchases.entrySet().iterator();
        while (amount > 0) {
            double currPrice = it.next().getKey();
            double amountBoughtOnThisPrice = currentAssetPurchases.get(currPrice);
            if (amountBoughtOnThisPrice > amount) {
                amountBoughtOnThisPrice -= amount;
                currentAssetPurchases.put(currPrice, amountBoughtOnThisPrice);
                amount = 0;
            } else {
                amount = amount - amountBoughtOnThisPrice;
                currentAssetPurchases.remove(currPrice);
            }
        }
    }

    public void buy(String assetId, double price, double moneyPaid) {
        double amount = moneyPaid / price;
        if (!assets.containsKey(assetId)) {
            Map<Double, Double> priceAmount = new HashMap<>();
            priceAmount.put(price, amount);
            assets.put(assetId, priceAmount);
            money -= moneyPaid;
            return;
        }
        Map<Double, Double> priceAmount = assets.get(assetId);
        if (priceAmount.containsKey(price)) {
            double previousAmount = priceAmount.get(price);
            double updatedAmount = previousAmount + amount;
            priceAmount.put(price, updatedAmount);
        } else {
            priceAmount.put(price, amount);
        }
        money -= moneyPaid;
    }

    public boolean contains(String assetId) {
        return assets.containsKey(assetId);
    }

}
