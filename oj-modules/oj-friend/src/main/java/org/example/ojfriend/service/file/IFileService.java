package org.example.ojfriend.service.file;

import org.example.file.domain.OSSResult;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    OSSResult upload(MultipartFile file);
}
