package com.personalproject.homepage.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.personalproject.homepage.api.ApiResult;
import com.personalproject.homepage.dto.FileResponseDto;
import com.personalproject.homepage.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileRestController {

    private final FileService fileService;

    @PostMapping("")
    public ApiResult<FileResponseDto> uploadFile(MultipartFile file) {
        return ApiResult.success(fileService.save(file));
    }

    @DeleteMapping("/{fileName}")
    public ApiResult<FileResponseDto> deleteFile(@PathVariable String fileName) {
        return ApiResult.success(fileService.delete(fileName));
    }
}
