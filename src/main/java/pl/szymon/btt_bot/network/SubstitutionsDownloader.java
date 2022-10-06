package pl.szymon.btt_bot.network;

import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pl.szymon.btt_bot.structures.data.Substitution;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class SubstitutionsDownloader {
	public static List<Substitution> get(NetworkContext networkContext, LocalDate date, String className) throws IOException, InterruptedException, NetworkStatusCodeException, NoSuchElementException {
		var req = new HttpPost(networkContext.getRootUrl() + "substitution/server/viewer.js?__func=getSubstViewerDayDataHtml");
		req.setEntity(new StringEntity("{\"__args\":[null,{\"date\":\"" +
				date +
				"\",\"mode\":\"classes\"}],\"__gsh\":\"" +
				networkContext.getGsecHash() +
				"\"}"));

		CloseableHttpResponse resp;
		String body;
		try (var client = HttpClients.createDefault()) {
			resp = client.execute(req, networkContext.getContext());
			body = new String(resp.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
		}

		if(resp.getStatusLine().getStatusCode() != 200) throw new NetworkStatusCodeException(resp.getStatusLine().getStatusCode());

		Element doc = Jsoup.parse(
					JsonParser.parseString(body)
							.getAsJsonObject()
							.get("r").getAsString()
				).selectFirst("table");

		if(doc == null)
			throw new NoSuchElementException("Couldn't find given elements!");

		Elements table = doc.select("tr");

		return table.stream()
				.dropWhile(e -> !(e.selectFirst("td").hasClass("header") && e.selectFirst("td").html().equals(className)))
				.takeWhile(e -> !e.selectFirst("td").hasClass("header") || e.selectFirst("td").html().equals(className))
				.map(element -> buildFromElement(element, date))
				.collect(Collectors.toList());
	}

	private static Substitution buildFromElement(Element element, LocalDate date) {
		String periods = element
				.getElementsByClass("period")
				.first()
				.selectFirst("span")
				.html();

		periods = periods.replace("(", "").replace(")", "").replace(" ", "");

		int start, end;

		if(periods.contains("-")) {
			start = Integer.parseInt(periods.split("-")[0]);
			end = Integer.parseInt(periods.split("-")[1]);
		} else {
			start = Integer.parseInt(periods);
			end = start;
		}

		return new Substitution(
				start,
				end,
				element.getElementsByClass("what").first().selectFirst("span").html(),
				element.getElementsByClass("info").first().selectFirst("span").html(),
				date
		);
	}
}
