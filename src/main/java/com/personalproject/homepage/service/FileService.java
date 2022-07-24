package com.personalproject.homepage.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.personalproject.homepage.dto.FileResponseDto;
import com.personalproject.homepage.error.ApiException;
import com.personalproject.homepage.error.ErrorMessage;

@Service
public class FileService {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/static/uploadfile";

    private static final String UPLOAD_RESPONSE_DIR = "/static/uploadfile/";

    public FileService() {
        if (!Files.exists(Paths.get(UPLOAD_DIR))) {
            try {
                Files.createDirectories(Paths.get(UPLOAD_DIR));
            } catch (IOException ioe) {/** TODO: handle exception */}
        }
    }

    public FileResponseDto save(MultipartFile file) {
        String contentType = file.getContentType();
        // TODO - 다른 타입의 파일 업로드 처리
        // 이미지가 아닌 경우
        checkArgument(contentType != null && contentType.startsWith("image/"),
            "이미지 파일만 업로드 할 수 있습니다.");

        String exe = contentType.split("/")[1];
        String newName = UUID.randomUUID().toString() + "." + exe;
        Path path = Paths.get(UPLOAD_DIR, newName);
        while (Files.exists(path)) {
            newName = UUID.randomUUID().toString() + "." + exe;
            path = Paths.get(UPLOAD_DIR, newName);
        }

        try {
            Files.copy(file.getInputStream(), path);
        } catch (IOException ioe) {/** TODO: handle exception */}
        return new FileResponseDto(newName, UPLOAD_RESPONSE_DIR + newName);
    }

    public FileResponseDto delete(String fileName) {
        checkArgument(StringUtils.hasText(fileName), "fileName을 입력해주세요.");
        try {
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.delete(filePath);
        } catch (IOException e) {
            throw new ApiException(ErrorMessage.NON_EXISTENT, "파일");
        }
        return new FileResponseDto(fileName, UPLOAD_RESPONSE_DIR + fileName);
    }
}
