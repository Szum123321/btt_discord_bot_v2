package pl.szymon.btt_bot.network;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class NetworkContextTest {

    @Test
    void init() throws IOException {
        NetworkContext networkContext = new NetworkContext (
                "https://lo3gdynia.edupage.org/"
        );

        networkContext.init();

        log.info("Context {}", networkContext);


        networkContext.update_gsec("https://lo3gdynia.edupage.org/");

        networkContext.update_gsec("https://lo3gdynia.edupage.org/timetable/");

        log.info("Context {}", networkContext);
    }
}