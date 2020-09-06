package pl.szymon.btt_bot.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.net.http.HttpClient;

@Getter
@Setter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NetworkContext {
    final String rootUrl;
    final HttpClient httpClient;

    String gsecHash;
}
