package org.example.ojfriend.controller.question.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.common.core.controller.BaseController;
import org.example.common.core.domain.R;
import org.example.file.domain.OSSResult;
import org.example.ojfriend.service.file.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@ApiResponse(description = "文件上传测试接口")
public class FileController extends BaseController {

    @Autowired
    private IFileService sysFileService;

    @PostMapping("/upload")
    public R<OSSResult> upload(@RequestBody MultipartFile file) {
        return R.ok(sysFileService.upload(file));
    }
}
