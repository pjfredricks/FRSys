package com.srijan.demo.facerecognition.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.srijan.demo.facerecognition.service.FaceRecognitionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/v1")
@SecurityRequirement(name = "x-api-key")
public class FaceRecognitionController {

    private final FaceRecognitionService faceRecognitionService;

    public FaceRecognitionController(FaceRecognitionService faceRecognitionService) {
        this.faceRecognitionService = faceRecognitionService;
    }

    @PostMapping(value = "/upload/images")
    public ResponseEntity<Void> uploadAndTrainFacesInImages(@RequestParam("folderPath") String folderPath) {
        faceRecognitionService.uploadAndTrainFacesInImages(folderPath);
        return ResponseEntity.ok(null);
    }

    @PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JsonNode> uploadAndTrainFacesInFolder(@RequestParam("subjectName") String subjectName,
                                                                @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(faceRecognitionService.uploadAndTrainFace(subjectName, image));
    }

    @PostMapping(value = "/upload/folder")
    public ResponseEntity<Void> uploadAndTrainFacesInFolder(@RequestParam("folderPath") String folderPath) {
        faceRecognitionService.uploadAndTrainFacesInFolder(folderPath);
        return ResponseEntity.ok(null);
    }

    @PostMapping(value = "/upload/folders")
    public ResponseEntity<Void> uploadAndTrainFacesInFolders(@RequestParam("folderPath") String folderPath) {
        faceRecognitionService.uploadAndTrainFacesInFolders(folderPath);
        return ResponseEntity.ok(null);
    }

    @GetMapping(value = "/list/subjects")
    public ResponseEntity<JsonNode> listSubjects() {
        return ResponseEntity.ok(faceRecognitionService.listExistingSubjects());
    }

    @GetMapping(value = "/list/subject/images")
    public ResponseEntity<Map<String, List<String>>> listImagesForSubject() {
        return ResponseEntity.ok(faceRecognitionService.listImagesForSubject());
    }

    @DeleteMapping(value = "/delete/subject/{subjectName}")
    public ResponseEntity<Void> deleteSubject(@PathVariable("subjectName") String subjectName) {
        faceRecognitionService.deleteSubject(subjectName);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping(value = "/delete/subjects")
    public ResponseEntity<Void> deleteSubjects() {
        faceRecognitionService.deleteAllSubjects();
        return ResponseEntity.ok(null);
    }
}
