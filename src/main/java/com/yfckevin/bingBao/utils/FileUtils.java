package com.yfckevin.bingBao.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static Path saveUploadedFile(MultipartFile file, String uploadPath) throws IOException {

        Path path = Paths.get(uploadPath);

        InputStream fileStream = file.getInputStream();
        Files.copy(fileStream, path);
        fileStream.close();

        return path;
    }

    public static void delete(Path path) throws IOException{
        if(Files.exists(path)){  //檢查檔案還在不在
            Files.delete(path);
        }
    }
}
