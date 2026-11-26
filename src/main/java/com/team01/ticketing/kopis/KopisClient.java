package com.team01.ticketing.kopis;

import com.team01.ticketing.error.BusinessException;
import com.team01.ticketing.error.ErrorCode;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
@EnableConfigurationProperties(KopisProperties.class)
public class KopisClient {

    public static final int PAGE_SIZE = 100;

    private static final DateTimeFormatter RESPONSE_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final KopisProperties properties;
    private final RestClient restClient;

    public KopisClient(KopisProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create(properties.baseUrl());
    }

    public List<KopisPerformanceItem> fetchPerformances(LocalDate from, LocalDate to, int page) {
        if (properties.serviceKey() == null || properties.serviceKey().isBlank()) {
            throw new BusinessException(ErrorCode.KOPIS_API_FAILED);
        }

        byte[] body;
        try {
            body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pblprfr")
                            .queryParam("service", properties.serviceKey())
                            .queryParam("stdate", DateTimeFormatter.BASIC_ISO_DATE.format(from))
                            .queryParam("eddate", DateTimeFormatter.BASIC_ISO_DATE.format(to))
                            .queryParam("cpage", page)
                            .queryParam("rows", PAGE_SIZE)
                            .build())
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.KOPIS_API_FAILED);
        }

        if (body == null || body.length == 0) {
            return List.of();
        }
        return parse(body);
    }

    List<KopisPerformanceItem> parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Document document = factory.newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xml)));

            NodeList nodes = document.getElementsByTagName("db");
            List<KopisPerformanceItem> items = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String kopisId = text(element, "mt20id");
                String title = text(element, "prfnm");
                if (kopisId == null || title == null) {
                    continue;
                }
                items.add(new KopisPerformanceItem(
                        kopisId,
                        title,
                        text(element, "genrenm"),
                        text(element, "poster"),
                        text(element, "fcltynm"),
                        date(text(element, "prfpdfrom")),
                        date(text(element, "prfpdto"))));
            }
            return items;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.KOPIS_API_FAILED);
        }
    }

    private String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = nodes.item(0).getTextContent();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate date(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, RESPONSE_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}