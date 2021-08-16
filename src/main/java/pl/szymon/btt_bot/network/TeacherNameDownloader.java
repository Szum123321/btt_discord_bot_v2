package pl.szymon.btt_bot.network;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pl.szymon.btt_bot.structures.data.Teacher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;



//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
//Zmienili standart nazw!
@Log4j2
public class TeacherNameDownloader {
    public static ImmutableMap<String, Teacher.TeacherName> get(NetworkContext context) throws IOException, InterruptedException {
        Element nextPageElement;
        String url = context.getRootUrl() + "teachers/";

        ImmutableMap.Builder<String, Teacher.TeacherName> builder = ImmutableMap.builder();

        ConcurrentHashMultiset<String> foundNames = ConcurrentHashMultiset.create();

        do {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> httpResponse = context.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                throw new NetworkStatusCodeException(httpResponse.statusCode());
            }

            Document doc = Jsoup.parse(httpResponse.body());

            nextPageElement = doc.getElementById("teachers_Paginator_1")
                    .getElementsByClass("skgdPaginatorItem ")
                    .last()
                    .getElementsByTag("a")
                    .first();

            url = context.getRootUrl() + nextPageElement.attributes().get("href");

            doc.getElementById("teachers_TeachersList_1")
                    .getElementsByClass("compositeInner")
                    .stream()
                    .filter(element -> !element.getElementsByClass("skgd skgdli-teachers_ListItem_1-teachers_DFText_1").isEmpty())
                    .map(element -> element.getElementsByTag("span").first().text().stripTrailing().stripLeading())
                    .map(NameMapper::new)
                    .filter(NameMapper::isValid)
                    .filter(m -> {
                        if(foundNames.contains(m.getProbablyShort())) {
                            log.warn("Found another instace of {}, skipping: {}", m.getProbablyShort(), m);
                        }
                        return !foundNames.contains(m.getProbablyShort());
                    })
                    .peek(m -> foundNames.add(m.getProbablyShort()))
                    .peek(log::info)
                    .forEach(m -> builder.put(m.getProbablyShort(), new Teacher.TeacherName(m.firstName, m.lastName)));
        } while(nextPageElement.text().equals("Dalej"));

        return builder.build();
    }

    @Value
    private static class NameMapper {
        String firstName, lastName;
        String probablyShort;
        boolean valid;

        public NameMapper(String mashup) {
            String[] parts = mashup.split(" ");

            if(parts.length != 2) {
                firstName = mashup;
                lastName = "";
                probablyShort = mashup;
                valid = false;
            } else {
                firstName = parts[0];
                lastName = parts[1];
                probablyShort = lastName.substring(0, 3) + firstName.charAt(0);
                valid = true;
            }
        }
    }

}
