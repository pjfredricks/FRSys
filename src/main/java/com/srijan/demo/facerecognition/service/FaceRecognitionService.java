package com.srijan.demo.facerecognition.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface FaceRecognitionService {
    JsonNode listExistingSubjects();
    Map<String, List<String>> listImagesForSubject();
    void deleteSubject(String subjectName);
    void deleteAllSubjects();
    JsonNode uploadAndTrainFace(String subjectName, MultipartFile image);
    void uploadAndTrainFacesInImages(String folderPath);
    void uploadAndTrainFacesInFolder(String folderPath);
    void uploadAndTrainFacesInFolders(String folderPath);
}
