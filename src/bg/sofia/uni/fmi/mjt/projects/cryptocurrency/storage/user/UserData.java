package bg.sofia.uni.fmi.mjt.projects.cryptocurrency.storage.user;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserData implements Serializable {

    private Map<String, String> accounts = new HashMap<>();
    private Map<String, Wallet> userWallets = new HashMap<>();

    public boolean nameAlreadyTaken(String name) {
        return accounts.containsKey(name);
    }

    public void register(String acc, String pass) {
        accounts.put(acc, pass);
        userWallets.put(acc, new Wallet());
    }

    public Wallet getUserWallet(String user) {
        return userWallets.get(user);
    }

    public void depositMoney(String user, double amount) {
        userWallets.get(user).deposit(amount);
    }

    public boolean isUserRegistered(String user) {
        return accounts.containsKey(user);
    }

    public boolean isPasswordWrong(String acc, String pass) {
        return !accounts.get(acc).equals(pass);
    }

}
