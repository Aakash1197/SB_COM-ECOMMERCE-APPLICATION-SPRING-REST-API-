package com.ecommerce.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {


        //get the file name of Current/original file
        String originalFileName = file.getOriginalFilename();

        //Generate a unique  file name
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : null);
        logger.info("UPLOADED FILE NAME :" + fileName);
        logger.info("UPLOADED PATH :" + path);
        String filePath = path + File.separator + fileName;
        logger.info("UPLOADED FILE PATH :" + filePath);
        //check the mentioned path is exists or create
        File folder = new File(path);
        logger.info("CREATED FILE  :" + folder.exists());
        if (!folder.exists()) {
            folder.mkdirs();
        }


        //upoading file to the server
        Files.copy(file.getInputStream(), Paths.get(filePath));

        //returning the file name
        return fileName;

    }

}

