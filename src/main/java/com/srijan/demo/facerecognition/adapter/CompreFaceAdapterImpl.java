package com.srijan.demo.facerecognition.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.util.Base64;

@Slf4j
@Service
public class CompreFaceAdapterImpl implements CompreFaceAdapter {

    @Value("${compreface.api.url}")
    private String compreFaceApiUrl;

    @Value("${compreface.fe.url}")
    private String compreFaceFeUrl;

    private final RestTemplate compreFaceRestTemplate;

    public CompreFaceAdapterImpl(@Qualifier("compreFaceRestTemplate") RestTemplate compreFaceRestTemplate) {
        this.compreFaceRestTemplate = compreFaceRestTemplate;
    }

    @Override
    public JsonNode listExistingSubjects() {
        return compreFaceRestTemplate.getForObject(compreFaceApiUrl + "/recognition/subjects", JsonNode.class);
    }

    @Override
    public JsonNode listAllImageSubjectMappings() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(compreFaceApiUrl + "/recognition/faces")
                .queryParam("pageSize", 250);

        return compreFaceRestTemplate.getForObject(builder.toUriString(), JsonNode.class).get("faces");
    }

    @Override
    public void deleteSubject(String subjectName) {
        compreFaceRestTemplate.delete(compreFaceApiUrl + "/recognition/subjects/" + subjectName);
    }

    @Override
    public void deleteAllSubjects() {
        compreFaceRestTemplate.delete(compreFaceApiUrl + "/recognition/subjects");
    }

    @Override
    public JsonNode trainFace(String subjectName, Resource image) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", image);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(compreFaceFeUrl + "/recognition/faces")
                .queryParam("subject", subjectName)
                .queryParam("det_prob_threshold", 0.0);

        try {
            return compreFaceRestTemplate.postForObject(builder.toUriString(), buildHttpEntity(form, httpHeaders), JsonNode.class);
        } catch (HttpClientErrorException ex) {
            log.error("Error training model for image {}, ERROR: {}", image.getFilename(), ex.getMessage(), ex);
        }
        return null;
    }

    @SneakyThrows
    public JsonNode recognizeFaceInImage(File image) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(compreFaceFeUrl + "/recognition/recognize")
                .queryParam("det_prob_threshold", 0.5);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new FileSystemResource(image));

        try {
            return compreFaceRestTemplate.postForObject(builder.toUriString(), buildHttpEntity(form, httpHeaders), JsonNode.class)
                    .get("result")
                    .get(0)
                    .get("subjects");
        } catch (HttpClientErrorException ex) {
            log.error("Error identifying faces in image {}, ERROR: {}", image.getName(), ex.getMessage(), ex);
        }
        return null;
    }

    private <T> HttpEntity<T> buildHttpEntity(T t, HttpHeaders httpHeaders) {
        return new HttpEntity<>(t, httpHeaders);
    }
}
