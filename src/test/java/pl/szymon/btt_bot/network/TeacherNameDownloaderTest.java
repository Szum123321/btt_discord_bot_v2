package pl.szymon.btt_bot.network;

import com.google.common.collect.ImmutableMap;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import pl.szymon.btt_bot.structures.data.Teacher;

import java.io.IOException;
import java.net.CookieManager;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
class TeacherNameDownloaderTest {
    private final NetworkContext networkContext = new NetworkContext(
            "https://lo3gdynia.edupage.org/",
            HttpClient.newBuilder().cookieHandler(new CookieManager()).build()
    );

    @Test
    void get() throws IOException, InterruptedException {
        NetworkContextInitializer.getGsecHash(networkContext);


        ImmutableMap<String, Teacher.TeacherName> teachers = TeacherNameDownloader.get(networkContext);

        log.info(teachers);
    }
}