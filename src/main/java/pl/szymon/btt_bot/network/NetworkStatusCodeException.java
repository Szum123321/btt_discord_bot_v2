package pl.szymon.btt_bot.network;

public class NetworkStatusCodeException extends RuntimeException {
    public NetworkStatusCodeException(int code) {
        super("Server responded with " + code + "status code!");
    }
}
