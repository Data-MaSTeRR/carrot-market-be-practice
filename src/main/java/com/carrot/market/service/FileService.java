package com.carrot.market.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 파일 업로드 및 관리를 담당하는 서비스 클래스
 * 주로 프로필 이미지와 같은 사용자 업로드 파일을 처리합니다.
 */
@Service  // Spring의 서비스 계층 컴포넌트로 등록
@Slf4j    // Lombok의 로깅 기능 자동 생성
public class FileService {

    // application.properties 또는 application.yml에서 설정값 주입
    @Value("${file.upload-dir}")
    private String uploadDir;  // 파일이 저장될 서버의 실제 디렉토리 경로 (예: /var/uploads)

    @Value("${file.base-url}")
    private String baseUrl;    // 파일 접근을 위한 기본 URL (예: http://localhost:8080)

    /**
     * 업로드된 파일을 서버에 저장하는 메서드
     * @param file 클라이언트로부터 전송받은 MultipartFile 객체
     * @return 저장된 파일에 접근할 수 있는 전체 URL
     * @throws RuntimeException 파일 저장 중 오류 발생 시
     */
    public String saveFile(MultipartFile file) {
        try {
            // 1. 업로드 디렉토리가 존재하지 않으면 생성
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);  // 상위 디렉토리까지 모두 생성
            }

            // 2. 원본 파일명 가져오기 및 검증
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new RuntimeException("파일명이 올바르지 않습니다");
            }

            // 3. 파일 확장자 추출
            // 예: "image.jpg" -> ".jpg"
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            // 4. 고유한 파일명 생성
            // UUID를 사용하여 파일명 중복을 방지하고 보안성 향상
            // 예: "a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
            String filename = UUID.randomUUID().toString() + extension;

            // 5. 최종 파일 저장 경로 생성
            Path filePath = uploadPath.resolve(filename);

            // 6. 파일을 실제로 디스크에 저장
            // REPLACE_EXISTING: 동일한 파일이 있으면 덮어쓰기 (UUID 사용으로 거의 발생하지 않음)
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 7. 저장 완료 로그 기록
            log.info("파일 저장 완료: {}", filePath);

            // 8. 클라이언트가 파일에 접근할 수 있는 URL 반환
            // 예: "http://localhost:8080/profile-images/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
            return baseUrl + "/profile-images/" + filename;

        } catch (IOException e) {
            // 파일 저장 중 I/O 오류 발생 시 처리
            log.error("파일 저장 실패", e);
            throw new RuntimeException("파일 저장에 실패했습니다", e);
        }
    }

    /**
     * 서버에 저장된 파일을 삭제하는 메서드
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public void deleteFile(String fileUrl) {
        // null 또는 빈 문자열인 경우 아무 작업도 하지 않음
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 1. URL에서 파일명만 추출
            // 예: "http://localhost:8080/uploads/profile-images/abc-123.jpg" -> "abc-123.jpg"
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);

            // 2. 삭제할 파일의 전체 경로 생성
            Path filePath = Paths.get(uploadDir).resolve(filename);

            // 3. 파일이 실제로 존재하는지 확인 후 삭제
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", filePath);
            }
        } catch (IOException e) {
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
            // 이미 삭제된 파일이거나 권한 문제 등의 경우를 대비
            log.error("파일 삭제 실패: {}", fileUrl, e);
        }
    }

    /**
     * 업로드된 파일이 유효한 이미지 파일인지 검증하는 메서드
     * @param file 검증할 MultipartFile 객체
     * @return 유효한 이미지 파일이면 true, 그렇지 않으면 false
     */
    public boolean isValidImageFile(MultipartFile file) {
        // 1. 파일이 null이거나 비어있는지 검증
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 2. 파일 크기 검증 (최대 10MB)
        // 10 * 1024 * 1024 = 10,485,760 bytes
        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }

        // 3. 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        // 4. 허용된 이미지 확장자인지 확인
        // 대소문자 구분 없이 비교하기 위해 toLowerCase() 사용
        String extension = originalFilename.toLowerCase();
        return extension.endsWith(".jpg") ||    // JPEG 형식
                extension.endsWith(".jpeg") ||   // JPEG 형식 (다른 확장자)
                extension.endsWith(".png") ||    // PNG 형식
                extension.endsWith(".gif") ||    // GIF 형식
                extension.endsWith(".webp");     // WebP 형식 (최신 이미지 포맷)
    }
}
