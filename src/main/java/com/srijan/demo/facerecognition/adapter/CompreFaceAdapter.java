package com.srijan.demo.facerecognition.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.io.Resource;

import java.io.File;

public interface CompreFaceAdapter {
    JsonNode listExistingSubjects();
    JsonNode listAllImageSubjectMappings();
    void deleteSubject(String subjectName);
    void deleteAllSubjects();
    JsonNode trainFace(String subjectName, Resource image);
    JsonNode recognizeFaceInImage(File image);
}
