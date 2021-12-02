package hu.pelyheadam.config;

import java.util.HashMap;
import java.util.Map;

public class AddressConfig {

    Map<String, String> addressPathMap = new HashMap<>();

    public AddressConfig() {
        initMap();
    }

    private void initMap() {
        addressPathMap.put("0xec709e1774f0ce4aba47b52a499f9abaaa159f71", "D:\\suli\\5\\nft\\src\\main\\resources\\account0--ec709e1774f0ce4aba47b52a499f9abaaa159f71.json");
        addressPathMap.put("0x36e146d5afab61ab125ee671708eeb380aea05b6", "D:\\suli\\5\\nft\\src\\main\\resources\\account1--36e146d5afab61ab125ee671708eeb380aea05b6.json");
        addressPathMap.put("0x06fc56347d91c6ad2dae0c3ba38eb12ab0d72e97", "D:\\suli\\5\\nft\\src\\main\\resources\\account2--06fc56347d91c6ad2dae0c3ba38eb12ab0d72e97.json");
        addressPathMap.put("0x9d624f7995e8bd70251f8265f2f9f2b49f169c55", "D:\\suli\\5\\nft\\src\\main\\resources\\account3--9d624f7995e8bd70251f8265f2f9f2b49f169c55.json");
        addressPathMap.put("0x2666a32bf7594ab5395d766dcfbf03d557dab538", "D:\\suli\\5\\nft\\src\\main\\resources\\account4--2666a32bf7594ab5395d766dcfbf03d557dab538.json");
    }

    public void addAddress(String address, String path) {
        addressPathMap.put(address, path);
    }

    public void removeAddress(String address) {
        addressPathMap.remove(address);
    }

    public String getPathFromAddress(String address) {
        String a = address.toLowerCase();
        return addressPathMap.get(a);
    }

}
