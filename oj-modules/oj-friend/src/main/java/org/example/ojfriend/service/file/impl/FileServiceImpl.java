package org.example.ojfriend.service.file.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.common.core.enums.ResultCode;
import org.example.file.domain.OSSResult;
import org.example.file.service.OSSService;
import org.example.ojfriend.service.file.IFileService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements IFileService {

    @Autowired
    private OSSService ossService;

    @Override
    public OSSResult upload(MultipartFile file) {
        try {
            return ossService.uploadFile(file);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }
    }
}
