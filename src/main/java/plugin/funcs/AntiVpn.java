package plugin.funcs;

import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.Jval;
import plugin.Plugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;

public class AntiVpn {
    public static final ArrayList<Subnet> subnets = new ArrayList<>();

    public static void loadAntiVPN() {
        Http.get("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/datacenter/ipv4.txt", response -> {
            var result = response.getResultAsString().split("\n");

            for (var address : result) subnets.add(parseSubnet(address));

            Log.info("Fetched @ datacenter subnets.", subnets.size());
        }, error -> Log.err("Failed to fetch datacenter subnets.", error));
        Http.get("https://www.gstatic.com/ipranges/goog.json", response -> {
            Jval googleJson = Jval.read(new InputStreamReader(response.getResultAsStream()));
            for (Jval obj : googleJson.get("prefixes").asArray()) {
                Jval ip4 = obj.get("Ipv4Prefix");
                if (ip4 != null) {
                    subnets.add(parseSubnet(ip4.asString()));
                }
            }
        }, error -> Log.err("Failed to fetch datacenter subnets.", error));
        Http.get("https://www.gstatic.com/ipranges/cloud.json", response -> {
            Jval googleJson = Jval.read(new InputStreamReader(response.getResultAsStream()));
            for (Jval obj : googleJson.get("prefixes").asArray()) {
                Jval ip4 = obj.get("Ipv4Prefix");
                if (ip4 != null) {
                    subnets.add(parseSubnet(ip4.asString()));
                }
            }
        }, error -> Log.err("Failed to fetch datacenter subnets.", error));
        InputStream stream = Plugin.class.getResourceAsStream("/azure.json");
        for (Jval value : Jval.read(new InputStreamReader(stream)).get("values").asArray()) {
            for (Jval addressPrefixes : value.get("properties").get("addressPrefixes").asArray()) {
                if (addressPrefixes.asString().charAt(4) != ':') {
                    subnets.add(parseSubnet(addressPrefixes.asString()));
                }
            }
        }
        Log.info("bro " + subnets.size() + " ips is crazy!");
    }

    /**
     * @return true if this address is suspicious, false otherwise
     **/
    public static boolean checkAddress(String address) {
        var ip = parseSubnet(address).ip;

        for (var subnet : subnets)
            if (subnet != null) {
                if ((ip & subnet.mask) == subnet.ip) return true;
            }

        return false;
    }

    private static Subnet parseSubnet(String address) {
        var parts = address.split("/");
        if (parts.length > 2)
            throw new IllegalArgumentException("Invalid IP address: " + address);

        int ip = 0;
        int mask = -1;
        try {
            for (var token : InetAddress.getByName(parts[0]).getAddress()) {
                ip = (ip << 8) + (token & 0xFF);
            }
        } catch (Exception e) {
            Log.err(e);
        }
        if (parts.length == 2) {
            mask = Integer.parseInt(parts[1]);
            if (mask > 32)
                throw new IllegalArgumentException("Invalid IP address: " + address);

            mask = 0xFFFFFFFF << (32 - mask);
        }

        return new Subnet(ip, mask);
    }

    private static class Subnet {
        public final int ip;
        public final int mask;

        public Subnet(int ip, int mask) {
            this.ip = ip;
            this.mask = mask;
        }
    }
}