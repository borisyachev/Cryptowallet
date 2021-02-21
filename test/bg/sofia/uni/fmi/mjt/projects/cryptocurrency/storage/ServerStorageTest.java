package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.CryptoData;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.CryptoInfo;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.dto.Asset;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user.UserData;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user.Wallet;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ServerStorageTest {

    private static final String NOT_LOGGED = "You are not logged in." + System.lineSeparator();

    public static ServerStorage serverStorage = new ServerStorage();
    public static SocketChannel testSocketChannel;


    @BeforeClass
    public static void registerTestUser() throws IOException {
        testSocketChannel = SocketChannel.open();
        serverStorage.registerUser("test", "test", testSocketChannel);

    }

    @Test
    public void testRegisterUserUsernameTaken() throws IOException {
        try (SocketChannel sc1 = SocketChannel.open()) {
            String reply = serverStorage.registerUser("test", "test", sc1);
            assertEquals("Username already taken." + System.lineSeparator(), reply);
        }
    }

    @Test
    public void testRegisterUserSuccessful() throws IOException {
        try (SocketChannel sc1 = SocketChannel.open()) {
            String reply = serverStorage.registerUser("registerUser2", "registerUser2", sc1);
            assertEquals("User successfully registered." + System.lineSeparator(), reply);
        }
    }

    @Test
    public void testLogoutNotLogged() throws IOException {
        try (SocketChannel clientChannel = SocketChannel.open()) {
            String reply = serverStorage.logout(clientChannel);
            assertEquals(NOT_LOGGED, reply);
        }
    }

    @Test
    public void testLogoutSuccessful() throws IOException {
        try (SocketChannel clientChannel = SocketChannel.open()) {
            serverStorage.registerUser("user", "user", clientChannel);
            String reply = serverStorage.logout(clientChannel);
            String expectedReply = "Successfully logged out." + System.lineSeparator();
            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testDepositMoneyNotLogged() throws IOException {
        try (SocketChannel sc1 = SocketChannel.open()) {
            String reply = serverStorage.depositMoney(sc1, 1000);
            assertEquals(NOT_LOGGED, reply);
        }
    }

    @Test
    public void testDepositMoneySuccessful() {
        String reply = serverStorage.depositMoney(testSocketChannel, 1000);
        assertEquals("Money successfully transferred to your account." + System.lineSeparator(), reply);
    }

    @Test
    public void testSellNotLogged() throws IOException {
        try (SocketChannel sc = SocketChannel.open()) {
            String reply1 = serverStorage.sell(sc, "sellUser1");
            String reply2 = serverStorage.sell(sc, "sellUser1", 10);
            assertEquals(NOT_LOGGED, reply1);
            assertEquals(NOT_LOGGED, reply2);
        }
    }


    @Test
    public void testSellUnsufficientAssetAmount() throws IOException {
        Asset asset1 = new Asset();
        asset1.setAsset_id("COIN1");
        asset1.setPrice_usd(1.0);
        Asset[] testAssets = {asset1};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();

        ServerStorage serverStorageSell = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userSell", "userSell");
            userData.depositMoney("userSell", 10);

            serverStorageSell.login(clientChannel, "userSell", "userSell");
            serverStorageSell.buy(clientChannel, "COIN1", 5);

            String reply = serverStorageSell.sell(clientChannel, "COIN1", 10);
            String expectedReply = "Insufficient asset amount." + System.lineSeparator();

            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testSellSuccessful() throws IOException {
        Asset asset1 = new Asset();
        asset1.setAsset_id("COIN1");
        asset1.setPrice_usd(1.0);
        Asset[] testAssets = {asset1};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();

        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userSell", "userSell");
            userData.depositMoney("userSell", 10);

            double buyAmount = 5.0;
            serverStorageSample.login(clientChannel, "userSell", "userSell");
            serverStorageSample.buy(clientChannel, "COIN1", buyAmount);

            String reply = serverStorageSample.sell(clientChannel, "COIN1", buyAmount);
            String expectedReply = buyAmount + " " + "COIN1" + " successfully sold." + System.lineSeparator();

            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testSellNoAmountArgument() throws IOException {
        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        initAssets(asset1, asset2);
        Asset[] testAssets = {asset1, asset2};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();

        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userSell", "userSell");
            userData.depositMoney("userSell", 1000);

            double buyAmount = 200;
            double assetAmountBought = buyAmount / asset1.getPrice();
            double profit = assetAmountBought * asset1.getPrice();
            serverStorageSample.login(clientChannel, "userSell", "userSell");
            serverStorageSample.buy(clientChannel, "COIN1", buyAmount);

            String reply = serverStorageSample.sell(clientChannel, "COIN1");
            String expectedReply = assetAmountBought + " assets of " + "COIN1"
                    + " successfully sold for " + profit
                    + "$." + System.lineSeparator();

            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testBuySuccessful() throws IOException {
        Asset asset1 = new Asset();
        asset1.setAsset_id("COIN1");
        asset1.setPrice_usd(1.0);
        Asset[] testAssets = {asset1};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();

        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userBuy", "userBuy");
            userData.depositMoney("userBuy", 100);
            serverStorageSample.login(clientChannel, "userBuy", "userBuy");

            String reply = serverStorageSample.buy(clientChannel, "COIN1", 10);
            String expectedReply = 10.0 + " " + "COIN1"
                    + " successfully purchased."
                    + System.lineSeparator();

            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testBuyNoMoney() throws IOException {
        Asset asset1 = new Asset();
        asset1.setAsset_id("COIN1");
        asset1.setPrice_usd(100.0);
        Asset[] testAssets = {asset1};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();

        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userBuy", "userBuy");
            userData.depositMoney("userBuy", 1);
            serverStorageSample.login(clientChannel, "userBuy", "userBuy");

            String reply = serverStorageSample.buy(clientChannel, "COIN1", 10);
            String expectedReply = "Insufficient amount of money in your wallet." + System.lineSeparator();

            assertEquals(expectedReply, reply);
        }
    }

    @Test
    public void testBuyNotLogged() throws IOException {
        try (SocketChannel clientChannel = SocketChannel.open()) {
            String reply = serverStorage.buy(clientChannel, "COIN", 10);
            assertEquals(NOT_LOGGED, reply);
        }
    }

    @Test
    public void testGetWalletMoneySuccessful() throws IOException {
        Asset asset1 = new Asset();
        asset1.setAsset_id("COIN1");
        asset1.setName("JAVACOIN1");
        asset1.setPrice_usd(100.0);
        Asset[] testAssets = {asset1};

        CryptoData cryptoData = new CryptoData();
        cryptoData.updateCryptoData(testAssets);

        UserData userData = new UserData();
        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userGetMoney", "userGetMoney");
            userData.depositMoney("userGetMoney", 50.0);
            serverStorageSample.login(clientChannel, "userGetMoney", "userGetMoney");
            double reply = serverStorageSample.getWalletMoney(clientChannel);

            assertEquals(50.0, reply, 0.01);
        }
    }

    @Test
    public void testGetWalletInfo() throws IOException {
        UserData userData = new UserData();
        CryptoData cryptoData = new CryptoData();

        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        initAssets(asset1, asset2);

        Asset[] testAssets = {asset1, asset2};
        cryptoData.updateCryptoData(testAssets);
        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userWalletInfo", "userWalletInfo");
            userData.depositMoney("userWalletInfo", 1000);

            serverStorageSample.login(clientChannel, "userWalletInfo", "userWalletInfo");
            serverStorageSample.buy(clientChannel, "COIN1", 200);

            Map<CryptoInfo, Double> reply = serverStorageSample.getWalletInfo(clientChannel);

            assertTrue(reply.containsKey(new CryptoInfo("COIN1", "JAVACOIN1")));
            assertEquals(2.0, reply.get(new CryptoInfo("COIN1", "JAVACOIN1")), 0.01);
        }
    }

    @Test
    public void testGetWalletOverallInfo() throws IOException {
        UserData userData = new UserData();
        CryptoData cryptoData = new CryptoData();

        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        initAssets(asset1, asset2);

        Asset[] testAssets = {asset1, asset2};
        cryptoData.updateCryptoData(testAssets);
        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userOverall", "userOverall");
            userData.depositMoney("userOverall", 1000);

            serverStorageSample.login(clientChannel, "userOverall", "userOverall");
            serverStorageSample.buy(clientChannel, "COIN1", 200);
            serverStorageSample.buy(clientChannel, "COIN2", 100);

            asset1.setPrice_usd(200);
            asset2.setPrice_usd(100);

            double reply = serverStorageSample.getWalletOverallInfo(clientChannel);
            double expectedReply = 2 * 100.0 + 2 * 50.0;
            assertEquals(expectedReply, reply, 0.01);
        }
    }


    @Test
    public void testListOffers() throws IOException {
        UserData userData = new UserData();
        CryptoData cryptoData = new CryptoData();

        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        initAssets(asset1, asset2);

        Asset[] testAssets = {asset1, asset2};
        cryptoData.updateCryptoData(testAssets);
        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel = SocketChannel.open()) {
            userData.register("userBuy", "userBuy");
            serverStorageSample.login(clientChannel, "userBuy", "userBuy");
            Map<CryptoInfo, Double> reply = serverStorageSample.listOffers(clientChannel, 2);

            assertTrue(reply.containsKey(new CryptoInfo("COIN1", "JAVACOIN1")));
            assertEquals(100.0, reply.get(new CryptoInfo("COIN1", "JAVACOIN1")), 0.01);

            assertTrue(reply.containsKey(new CryptoInfo("COIN2", "JAVACOIN2")));
            assertEquals(50.0, reply.get(new CryptoInfo("COIN2", "JAVACOIN2")), 0.01);
        }
    }


    @Test
    public void testCloseServer() throws IOException, ClassNotFoundException {
        UserData userData = new UserData();
        CryptoData cryptoData = new CryptoData();

        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        initAssets(asset1, asset2);

        Asset[] testAssets = {asset1, asset2};
        cryptoData.updateCryptoData(testAssets);
        ServerStorage serverStorageSample = new ServerStorage(userData, cryptoData);

        try (SocketChannel clientChannel1 = SocketChannel.open();
                SocketChannel clientChannel2 = SocketChannel.open()) {

            userData.register("userInFile1", "userInFile1");
            userData.register("userInFile2", "userInFile2");

            serverStorageSample.login(clientChannel1, "userInFile1", "userInFile1");
            serverStorageSample.login(clientChannel2, "userInFile2", "userInFile2");

            serverStorageSample.depositMoney(clientChannel1, 100.0);
            serverStorageSample.depositMoney(clientChannel2, 99.9);

            Set<SocketChannel> socketsToDisconnect = serverStorageSample.closeServer();

            // check the data from the file :

            assertTrue(socketsToDisconnect.contains(clientChannel1));
            assertTrue(socketsToDisconnect.contains(clientChannel2));
            assertEquals("Returned set should be with this size.", 2, socketsToDisconnect.size());

            ServerStorage serverFromFile = new ServerStorage(new FileInputStream(new File("userdata.bin")));

            UserData dataFromFile = null;
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("userdata.bin")));
            dataFromFile = (UserData) ois.readObject();


            assertTrue("Such user should exists.", dataFromFile.isUserRegistered("userInFile1"));
            assertTrue("Such user should exists.", dataFromFile.isUserRegistered("userInFile2"));

            Wallet wallet1 = dataFromFile.getUserWallet("userInFile1");
            Wallet wallet2 = dataFromFile.getUserWallet("userInFile2");

            assertEquals(100.0, wallet1.getMoney(), 0.01);
            assertEquals(99.9, wallet2.getMoney(), 0.01);
        }
    }



    private void initAssets(Asset asset1, Asset asset2) {
        asset1.setAsset_id("COIN1");
        asset1.setName("JAVACOIN1");
        asset1.setPrice_usd(100.0);

        asset2.setAsset_id("COIN2");
        asset2.setName("JAVACOIN2");
        asset2.setPrice_usd(50.0);
    }

}
