package pl.szymon.btt_bot.network;

import com.google.common.collect.ImmutableMap;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.szymon.btt_bot.structures.data.Teacher;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

@Log4j2
public class TeacherNameDownloader {
    public static ImmutableMap<String, Teacher.TeacherName> get(NetworkContext context) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(context.getRootUrl() + "teachers/")) //this slash is important. if not present server response with 302
                .build();

        HttpResponse<String> httpResponse = context.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if(httpResponse.statusCode() != 200) {
            throw new NetworkStatusCodeException(httpResponse.statusCode());
        }

        Elements table = Jsoup.parse(httpResponse.body())
                .getElementsByClass("skinTemplateMainDiv")
                .first()
                .selectFirst("tbody")
                .select("tr");

        ImmutableMap.Builder<String, Teacher.TeacherName> mapBuilder = ImmutableMap.builder();

        table.stream()
                .filter(element -> element.hasClass("row1") || element.hasClass("row2"))
                .filter(TeacherNameDownloader::isValidName)
                .map(TeacherNameDownloader::toTeacherName)
                .filter(Objects::nonNull)
                .forEach(intermid -> mapBuilder.put(intermid.getShortName(), intermid.getCoagulatedName()));

        return mapBuilder.build();
    }

    private static boolean isValidName(Element element) {
        Elements columns = element.select("td");
        return columns.get(2).hasText() && columns.get(3).hasText();
    }

    private static Intermid toTeacherName(Element element) {
        Elements elements = element.select("td");
        String coagulated = elements.get(2).text();
        String[] nameParts = coagulated.split(" ");

        if(nameParts.length != 2)
            return null;

        return new Intermid(
                new Teacher.TeacherName(nameParts[0], nameParts[1]),
                elements.get(3).text()
        );
    }

    @Value
    private static class Intermid {
        Teacher.TeacherName coagulatedName;
        String shortName;
    }
}
