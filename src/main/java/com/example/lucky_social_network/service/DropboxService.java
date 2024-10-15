package com.example.lucky_social_network.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class DropboxService {
    private static final String AVATAR_DIRECTORY = "/avatars/";
    private final DbxClientV2 client;

    public DropboxService(@Value("${dropbox.access.token}") String accessToken) {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("sotdx2mdj3sn3tm/1.0")
                .withAutoRetryEnabled(3)  // Включаем автоматический повтор с максимум 3 попытками
                .build();
        this.client = new DbxClientV2(config, accessToken);
    }

    public String uploadFile(MultipartFile file) throws IOException, DbxException {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        String dropboxPath = AVATAR_DIRECTORY + uniqueFileName;

        try (InputStream in = file.getInputStream()) {
            UploadUploader uploader = client.files().uploadBuilder(dropboxPath)
                    .withMode(WriteMode.OVERWRITE)
                    .start();

            FileMetadata metadata = uploader.uploadAndFinish(in);
            return metadata.getPathLower();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public String getTemporaryLink(String dropboxPath) throws DbxException {
        return client.files().getTemporaryLink(dropboxPath).getLink();
    }


//    @PostConstruct
//    public void testDropboxConnection() {
//        try {
//            // Проверка возможности получения информации об аккаунте
//            client.users().getCurrentAccount();
//            log.info("Successfully connected to Dropbox and retrieved account info");
//
//            // Проверка возможности листинга файлов
//            client.files().listFolder("");
//            log.info("Successfully listed files in root folder");
//
//            // Попытка создать тестовый файл
//            String testContent = "Test file content";
//            try (InputStream in = new ByteArrayInputStream(testContent.getBytes())) {
//                client.files().upload("/test_file.txt").uploadAndFinish(in);
//            }
//            log.info("Successfully uploaded test file");
//
//            // Попытка получить временную ссылку на тестовый файл
//            String link = client.files().getTemporaryLink("/test_file.txt").getLink();
//            log.info("Successfully got temporary link for test file: {}", link);
//
//        } catch (DbxException | IOException e) {
//            log.error("Error testing Dropbox connection: {}", e.getMessage(), e);
//        }
//    }
}