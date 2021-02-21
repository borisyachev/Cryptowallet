package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.command;

import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.ServerStorage;
import bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.crypto.CryptoInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

public class CommandHandler {


    private static final String LINE_SEP = System.lineSeparator();
    private static final String DOTTED_LINE = "------------------------";
    private static final String DOTTED_SEPARATOR = LINE_SEP + DOTTED_LINE;

    private static final String HELP = "help";
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String DEPOSIT_MONEY = "deposit-money";
    private static final String LIST_OFFERS = "list-offers";
    private static final String BUY = "buy";
    private static final String SELL = "sell";
    private static final String GET_WALLET_INFO = "get-wallet-info";
    private static final String GET_WALLET_OVERALL_INFO = "get-wallet-overall-info";


    private static final String INVALID_ARGS_COUNT = "Invalid count of arguments given.(expected: %s)"
                                                    + System.lineSeparator();
    private static final String INVALID_ARGUMENT = "Invalid argument given.(expected: %s)"
                                                    + System.lineSeparator();
    private ServerStorage storage;

    public CommandHandler() {
        storage = new ServerStorage();
    }

    public CommandHandler(FileInputStream fileInputStream) throws IOException, ClassNotFoundException {
        storage = new ServerStorage(fileInputStream);
    }

    public String getServerMsg(Command command, SocketChannel clientChannel) {
        return switch (command.command()) {
            case HELP -> help(command.args());
            case REGISTER -> register(command.args(), clientChannel);
            case LOGIN -> login(command.args(), clientChannel);
            case LOGOUT -> logout(command.args(), clientChannel);
            case DEPOSIT_MONEY -> depositMoney(command.args(), clientChannel);
            case LIST_OFFERS -> listOffers(command.args(), clientChannel);
            case BUY -> buy(command.args(), clientChannel);
            case SELL -> sell(command.args(), clientChannel);
            case GET_WALLET_INFO -> getWalletInfo(command.args(), clientChannel);
            case GET_WALLET_OVERALL_INFO -> getWalletOverallInfo(command.args(), clientChannel);
            default -> "Unknown command given." + System.lineSeparator();
        };
    }

    private String register(String[] args, SocketChannel clientChannel) {
        if (!validArgs(REGISTER, args)) {
            return String.format(INVALID_ARGS_COUNT, "register <username> <password>");
        }
        String user = args[0];
        String pass = args[1];
        return storage.registerUser(user, pass, clientChannel);
    }

    private String login(String[] args, SocketChannel clientChannel) {
        if (!validArgs(LOGIN, args)) {
            return String.format(INVALID_ARGS_COUNT, "login <username> <password>");
        }
        String user = args[0];
        String pass = args[1];
        return storage.login(clientChannel, user, pass);
    }

    private String logout(String[] args, SocketChannel clientChannel) {
        if (!validArgs(LOGOUT, args)) {
            return String.format(INVALID_ARGS_COUNT, "logout");
        }

        return storage.logout(clientChannel);
    }

    private String depositMoney(String[] args, SocketChannel clientChannel) {
        if (!validArgs(DEPOSIT_MONEY, args)) {
            return String.format(INVALID_ARGS_COUNT, "deposit-money <money>");
        }
        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException | NullPointerException e) {
            return String.format(INVALID_ARGUMENT, "deposit-money <money> (<money> is a number)");
        }

        return storage.depositMoney(clientChannel, amount);
    }

    private String listOffers(String[] args, SocketChannel clientChannel) {
        if (!validArgs(LIST_OFFERS, args)) {
            return String.format(INVALID_ARGS_COUNT, "list-offers");
        }

        if (!storage.isUserLogged(clientChannel)) {
            return "You are not logged in.";
        }

        int offersNumber = 50;
        Map<CryptoInfo, Double> assetOffers = storage.listOffers(clientChannel, offersNumber);
        StringBuilder serverResponse = new StringBuilder("Crypto id, name, price:" + LINE_SEP);
        assetOffers.forEach((info, price)
                -> serverResponse.append(String.format("[%s, %s, %.8f] %n", info.id(), info.name(), price)));

        return serverResponse.toString();
    }

    private String buy(String[] args, SocketChannel clientChannel) {
        if (!validArgs(BUY, args)) {
            return String.format(INVALID_ARGS_COUNT, "buy <asset id> <amount> "
                    + "(<asset id> should exist in our server, <amount> is a real number");
        }
        String assetId = args[0];
        double assetAmount;
        try {
            assetAmount = Double.parseDouble(args[1]);
        } catch (NumberFormatException | NullPointerException e) {
            return String.format(INVALID_ARGUMENT, "buy <asset id> <amount> (<amount> is a real number)");
        }
        return storage.buy(clientChannel, assetId, assetAmount);
    }

    private String sell(String[] args, SocketChannel clientChannel) {
        if (!validArgs(SELL, args)) {
            return String.format(INVALID_ARGS_COUNT, "sell <asset id> <amount> ; sell <asset id>"
                    + "(<asset id> should exist in our server, <amount> is a real number");
        }
        double assetAmount;
        String assetId = args[0];
        if (args.length == 2) {
            try {
                assetAmount = Double.parseDouble(args[1]);
            } catch (NumberFormatException | NullPointerException e) {
                return String.format(INVALID_ARGUMENT, "sell <crypto id> <amount> (<amount> is a real number>)");
            }
            return storage.sell(clientChannel, assetId, assetAmount);
        }
        return storage.sell(clientChannel, assetId);
    }

    private String getWalletInfo(String[] args, SocketChannel clientChannel) {
        if (!validArgs(GET_WALLET_INFO, args)) {
            return String.format(INVALID_ARGS_COUNT, "get-wallet-info (without any arguments)");
        }

        if (!storage.isUserLogged(clientChannel)) {
            return "You are not logged in." + System.lineSeparator();
        }

        StringBuilder serverResponse = new StringBuilder("Wallet money; crypto id, name, price:" + LINE_SEP);
        serverResponse.append("Wallet money: ")
                .append(storage.getWalletMoney(clientChannel)).append("$").append(LINE_SEP);

        Map<CryptoInfo, Double> assetsOwned = storage.getWalletInfo(clientChannel);

        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
        assetsOwned.forEach((info, price)
                -> serverResponse.append(String.format("[%s, %s, %s] %n", info.id(), info.name(), df.format(price))));

        return serverResponse.toString();
    }

    private String getWalletOverallInfo(String[] args, SocketChannel clientChannel) {
        if (!validArgs(GET_WALLET_INFO, args)) {
            return String.format(INVALID_ARGS_COUNT, "get-wallet-overall-info (without any arguments)");
        }
        if (!storage.isUserLogged(clientChannel)) {
            return "You are not logged in.";
        }

        return "Profit if you sell all of your assets now: "
                + storage.getWalletOverallInfo(clientChannel) + "$";
    }

    private String help(String[] args) {
        if (!validArgs(HELP, args)) {
            return String.format(INVALID_ARGS_COUNT, "help (without any arguments)");
        }
        return  LINE_SEP + "USE THE FOLLOWING COMMANDS FOR OUR SERVICES: "
                + DOTTED_SEPARATOR
                + LINE_SEP + REGISTER + " <username> <password> | to make a registration in our Crypto server."
                + LINE_SEP + "example: register user1 password1" + DOTTED_SEPARATOR
                + LINE_SEP + LOGIN + " <username> <password> | to log into "
                + "the server with an already registered account."
                + LINE_SEP + "example: login user1 password1" + DOTTED_SEPARATOR
                + LINE_SEP + LOGOUT + " logout | to log out"
                + LINE_SEP + "example : logout" + DOTTED_SEPARATOR
                + LINE_SEP + DEPOSIT_MONEY + " <money> | to add money to your account(in $USD)."
                + LINE_SEP + "example: deposit-money 100" + DOTTED_SEPARATOR
                + LINE_SEP + LIST_OFFERS + " | to see a list of cryptocurrencies in our server."
                + LINE_SEP + "example: list-offers" + DOTTED_SEPARATOR
                + LINE_SEP + BUY
                + " <cryptoID> <money> | to buy cryptocurrency assets with the given amount of money(in $USD)."
                + LINE_SEP + "example: buy BTC 1500" + DOTTED_SEPARATOR
                + LINE_SEP + SELL + " <cryptoID> <amount> | to sell a certain number of assets from the "
                +             "given cryptocurrency from your wallet(use without amount to sell all assets)."
                + LINE_SEP + "example with amount argument: sell BTC 0.6"
                + LINE_SEP + "example without amonut argument: sell BTC" + DOTTED_SEPARATOR
                + LINE_SEP + GET_WALLET_INFO + " | to see the current amount of money and assets in your wallet."
                + LINE_SEP + "example: get-wallet-info" + DOTTED_SEPARATOR
                + LINE_SEP + GET_WALLET_OVERALL_INFO + " | to see the profit from all your crypto assets based on the "
                +              "price when you purchased them and on the actual price if you sell them now."
                + LINE_SEP + "example: get-wallet-overall-info" + DOTTED_SEPARATOR;
    }

    private boolean validArgs(String cmd, String[] args) {
        return switch (cmd) {
            case HELP, LOGOUT,  GET_WALLET_INFO, GET_WALLET_OVERALL_INFO, LIST_OFFERS -> args.length == 0;
            case REGISTER, LOGIN -> args.length == 2;
            case DEPOSIT_MONEY -> args.length == 1;
            case BUY -> args.length == 2 && storage.existingAsset(args[0]);
            case SELL -> args.length == 2 && storage.existingAsset(args[0])
                    || args.length == 1 && storage.existingAsset(args[0]);
            default -> false;
        };
    }

    public Set<SocketChannel> closeServer() {
        return storage.closeServer();
    }

}
