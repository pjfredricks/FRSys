package com.srijan.demo.facerecognition.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.srijan.demo.facerecognition.adapter.CompreFaceAdapter;
import com.srijan.demo.facerecognition.dto.ImageSubjectMetaData;
import com.srijan.demo.facerecognition.dto.SubjectsSimilarityScoreList;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Slf4j
@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private final ObjectMapper mapper;
    private final CompreFaceAdapter compreFaceAdapter;

    public FaceRecognitionServiceImpl(ObjectMapper mapper, CompreFaceAdapter compreFaceAdapter) {
        this.mapper = mapper;
        this.compreFaceAdapter = compreFaceAdapter;
    }

    @Override
    public JsonNode listExistingSubjects() {
        return compreFaceAdapter.listExistingSubjects();
    }

    @Override
    @SneakyThrows
    public Map<String, List<String>> listImagesForSubject() {
        ObjectReader reader = mapper.readerFor(new TypeReference<Set<String>>() {
        });
        Set<String> subjects = reader.readValue(compreFaceAdapter.listExistingSubjects().get("subjects"));

        reader = mapper.readerFor(new TypeReference<List<ImageSubjectMetaData>>() {
        });
        List<ImageSubjectMetaData> imageSubjectMetaDataList = reader.readValue(compreFaceAdapter.listAllImageSubjectMappings());

        Map<String, List<String>> subjectImagesMap = new HashMap<>();
        subjects.forEach(subject -> subjectImagesMap.put(subject, new ArrayList<>()));

        imageSubjectMetaDataList.forEach(imageSubjectMetaData -> {
            String subjectName = imageSubjectMetaData.getSubject();
            subjectImagesMap.containsKey(subjectName);
            subjectImagesMap.get(subjectName).add(imageSubjectMetaData.getImage_id());
        });

        return subjectImagesMap;
    }

    @Override
    public void deleteSubject(String subjectName) {
        compreFaceAdapter.deleteSubject(subjectName);
    }

    @Override
    public void deleteAllSubjects() {
        compreFaceAdapter.deleteAllSubjects();
    }

    @Override
    public JsonNode uploadAndTrainFace(String subjectName, MultipartFile image) {
        return compreFaceAdapter.trainFace(subjectName, image.getResource());
    }

    @Override
    @SneakyThrows
    public void uploadAndTrainFacesInImages(String folderPath) {
        Collection<File> images = FileUtils.listFiles(new File(folderPath), new String[]{"jpeg", "jpg", "png"}, false);
        ObjectReader reader = mapper.readerFor(new TypeReference<List<SubjectsSimilarityScoreList>>() {
        });

        for (File image : images) {
            List<SubjectsSimilarityScoreList> similarityScoreLists = reader.readValue(compreFaceAdapter.recognizeFaceInImage(image));
            Optional<SubjectsSimilarityScoreList> similarityScore = similarityScoreLists.stream()
                    .filter(subjectsSimilarityScore -> subjectsSimilarityScore.getSimilarity() > 0.95)
                    .findAny();
            if (similarityScore.isPresent()) {
                compreFaceAdapter.trainFace(similarityScore.get().getSubject(), new FileSystemResource(image));
                log.info("Training subject {} to image {} with similarityScore {}",
                        similarityScore.get().getSubject(), image.getName(), similarityScore.get().getSimilarity());
            } else {
                String subjectName = "subject-" + RandomStringUtils.random(6);
                compreFaceAdapter.trainFace(subjectName, new FileSystemResource(image));
                log.info("Training subject {} to image {}", subjectName, image.getName());
            }
        }
    }

    @Override
    public void uploadAndTrainFacesInFolder(String folderPath) {
        uploadAndTrainFacesInSubFolder(new File(folderPath));
    }

    @Override
    public void uploadAndTrainFacesInFolders(String folderPath) {
        File[] subFolders = new File(folderPath).listFiles(File::isDirectory);
        if (subFolders != null) {
            Arrays.stream(subFolders).forEach(this::uploadAndTrainFacesInSubFolder);
        }
    }

    private void uploadAndTrainFacesInSubFolder(File subFolder) {
        String subjectName = subFolder.getName();
        Collection<File> images = FileUtils.listFiles(subFolder, new String[]{"jpeg", "jpg", "png"}, false);
        images.forEach(image -> compreFaceAdapter.trainFace(subjectName, new FileSystemResource(image)));
    }
}
