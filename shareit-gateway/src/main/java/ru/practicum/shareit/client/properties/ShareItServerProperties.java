package ru.practicum.shareit.client.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "shareit-server")
@RequiredArgsConstructor
@Getter
public class ShareItServerProperties {
    private final String url;
}