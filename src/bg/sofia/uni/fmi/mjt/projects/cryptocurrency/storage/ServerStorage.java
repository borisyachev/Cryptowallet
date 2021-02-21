package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.CryptoData;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.CryptoInfo;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user.UserData;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user.Wallet;

import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.nio.channels.SocketChannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ServerStorage {

    private static final String NOT_LOGGED = "You are not logged in." + System.lineSeparator();
    private static final String FILEPATH = "userdata.bin";

    private UserData userData;
    private CryptoData cryptoData;
    private Set<SocketChannel> usersLogged = new HashSet<>();
    private Map<SocketChannel, String> userSockets = new HashMap<>();


    public ServerStorage() {
        cryptoData = new CryptoData();
        userData = new UserData();
    }

    public ServerStorage(UserData userData, CryptoData cryptoData) {
        this.userData = userData;
        this.cryptoData = cryptoData;
    }

    public ServerStorage(FileInputStream fileInputStream) throws IOException, ClassNotFoundException {
        cryptoData = new CryptoData();
        userData = readUserDataFromFile(fileInputStream);
    }


    public String sell(SocketChannel clientChannel, String assetId) {
        if (!isUserLogged(clientChannel)) {
            return NOT_LOGGED;
        }

        String user = userSockets.get(clientChannel);
        Wallet userWallet = userData.getUserWallet(user);

        if (!userWallet.contains(assetId)) {
            return "Insufficient asset amount." + System.lineSeparator();
        }

        double assetPrice = cryptoData.getAssetsInfo().get(assetId).getPrice();
        double assetsAmount = userWallet.sell(assetId, assetPrice);
        double profit = assetPrice * assetsAmount;

        return assetsAmount + " assets of " + assetId
                + " successfully sold for " + profit
                + "$." + System.lineSeparator();
    }

    public String sell(SocketChannel clientChannel, String assetId, double amount) {
        if (!isUserLogged(clientChannel)) {
            return NOT_LOGGED;
        }

        String user = userSockets.get(clientChannel);
        Wallet userWallet = userData.getUserWallet(user);

        if (!userWallet.contains(assetId) || userWallet.getAssetAmount(assetId) < amount) {
            return "Insufficient asset amount." + System.lineSeparator();
        }

        double assetPrice = cryptoData.getAssetsInfo().get(assetId).getPrice();
        double profit = assetPrice * amount;
        userWallet.sell(assetId, amount, profit);

        return amount + " " + assetId + " successfully sold." + System.lineSeparator();
    }

    public String buy(SocketChannel clientChannel, String assetId, double moneyPaid) {

        if (!isUserLogged(clientChannel)) {
            return NOT_LOGGED;
        }

        String user = userSockets.get(clientChannel);
        Wallet userWallet = userData.getUserWallet(user);

        if (userWallet.getMoney() < moneyPaid) {
            return "Insufficient amount of money in your wallet." + System.lineSeparator();
        }
        double price = cryptoData.getAssetsInfo().get(assetId).getPrice();
        userWallet.buy(assetId, price, moneyPaid);
        double amount = moneyPaid / price;
        return amount + " " + assetId + " successfully purchased." + System.lineSeparator();
    }

    public String registerUser(String acc, String pass, SocketChannel clientChannel) {
        if (userData.nameAlreadyTaken(acc)) {
            return "Username already taken." + System.lineSeparator();
        }
        userData.register(acc, pass);
        userSockets.put(clientChannel, acc);
        usersLogged.add(clientChannel);

        return "User successfully registered." + System.lineSeparator();
    }

    public String depositMoney(SocketChannel clientChannel, double amount) {
        if (!isUserLogged(clientChannel)) {
            return NOT_LOGGED;
        }
        String user = userSockets.get(clientChannel);
        userData.depositMoney(user, amount);

        return "Money successfully transferred to your account." + System.lineSeparator();
    }

    public double getWalletMoney(SocketChannel clientSocket) {
        String user = userSockets.get(clientSocket);
        Wallet userWallet = userData.getUserWallet(user);

        return userWallet.getMoney();
    }

    public double getWalletOverallInfo(SocketChannel clientSocket) {
        String user = userSockets.get(clientSocket);
        Wallet wallet = userData.getUserWallet(user);

        return calculateAssetProfit(wallet);
    }

    public Map<CryptoInfo, Double> listOffers(SocketChannel clientChannel, int clientCountRequest) {
        Map<String, Asset> assetsInfo = cryptoData.getAssetsInfo();
        var it = assetsInfo.entrySet().iterator();
        Map<CryptoInfo, Double> toReturn = new HashMap<>();

        int cnt = 0;
        while (it.hasNext() && cnt < clientCountRequest) {

            String currCryptoId = it.next().getKey();
            String currCryptoName = assetsInfo.get(currCryptoId).getName();
            CryptoInfo currCryptoInfo = new CryptoInfo(currCryptoId, currCryptoName);
            double currCryptoPrice = assetsInfo.get(currCryptoId).getPrice();
            toReturn.put(currCryptoInfo, currCryptoPrice);
            ++cnt;
        }

        return toReturn;
    }

    private double calculateAssetProfit(Wallet wallet) {
        Map<String, Asset> assetInfo = cryptoData.getAssetsInfo();
        Set<String> assetIds = wallet.getPresentAssetIds();
        double totalSum = 0.0;

        for (String id : assetIds) {

            double coinsAmount = 0.0;
            double amountSpent = 0.0;
            Map<Double, Double> assetPurchases = wallet.getAssetPurchases(id);

            for (double price : assetPurchases.keySet()) {
                coinsAmount += assetPurchases.get(price);
                amountSpent += price * coinsAmount;
            }

            double currentAssetProfit = coinsAmount * assetInfo.get(id).getPrice();
            totalSum += currentAssetProfit - amountSpent;
        }

        return totalSum;
    }

    public Map<CryptoInfo, Double> getWalletInfo(SocketChannel clientChannel) {
        String user = userSockets.get(clientChannel);
        Wallet userWallet = userData.getUserWallet(user);
        Map<String, Asset> assetsInfo = cryptoData.getAssetsInfo();

        Map<CryptoInfo, Double> toReturn = new HashMap<>();

        for (String assetId : userWallet.getAssets().keySet()) {
            double currAssetAmount = userWallet.getAssetAmount(assetId);
            String currAssetName = assetsInfo.get(assetId).getName();
            CryptoInfo cryptoInfo = new CryptoInfo(assetId, currAssetName);

            toReturn.put(cryptoInfo, currAssetAmount);
        }

        return toReturn;
    }

    public boolean existingAsset(String assetId) {
        return cryptoData.getAssetsInfo().containsKey(assetId);
    }

    public String login(SocketChannel clientChannel, String user, String pass) {
        if (!userData.isUserRegistered(user) || userData.isPasswordWrong(user, pass)) {
            return "Wrong username/password given." + System.lineSeparator();
        }
        if (isUserLogged(clientChannel)) {
            return "You are already logged in." + System.lineSeparator();
        }

        userSockets.put(clientChannel, user);
        usersLogged.add(clientChannel);

        return "Successfully logged in." + System.lineSeparator();
    }

    public String logout(SocketChannel clientChannel) {
        if (!isUserLogged(clientChannel)) {
            return NOT_LOGGED;
        }

        userSockets.remove(clientChannel);
        usersLogged.remove(clientChannel);

        return "Successfully logged out." + System.lineSeparator();
    }

    public boolean isUserLogged(SocketChannel clientChannel) {
        return usersLogged.contains(clientChannel);
    }

    public Set<SocketChannel> closeServer() {
        cryptoData.closeServer();
        writeUserDataToFile();
        return usersLogged;
    }

    private UserData readUserDataFromFile(FileInputStream fileInputStream) throws IOException, ClassNotFoundException {
        UserData toReturn;
        ObjectInputStream ois = new ObjectInputStream(fileInputStream);
        toReturn = (UserData) ois.readObject();
        return toReturn;
    }

    private void writeUserDataToFile() {
        try (FileOutputStream fileOut = new FileOutputStream(FILEPATH);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            objectOut.writeObject(userData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
