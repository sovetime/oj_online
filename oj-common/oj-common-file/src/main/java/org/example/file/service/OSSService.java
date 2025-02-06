package org.example.file.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ObjectId;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.CacheConstants;
import org.example.common.core.constants.Constants;
import org.example.common.core.enums.ResultCode;
import org.example.common.core.utils.ThreadLocalUtil;
import org.example.file.config.OSSProperties;
import org.example.file.domain.OSSResult;
import org.example.redis.service.RedisService;
import org.example.security.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RefreshScope
public class OSSService {

    @Autowired
    private OSSProperties prop;
    @Resource
    private OSSClient ossClient;
    @Autowired
    private RedisService redisService;

    @Value("${file.max-time}")
    private int maxTime;
    @Value("${file.test}")
    private boolean test;// 是否为测试环境

    //上传文件
    public OSSResult uploadFile(MultipartFile file) throws Exception {
        //不是测试环境会校验用户上传文件的次数，做出限制
        if (!test) {
            checkUploadCount();
        }

        InputStream inputStream = null;
        try {
            String fileName;
            if (file.getOriginalFilename() != null) {
                fileName = file.getOriginalFilename().toLowerCase();
            } else {
                fileName = "a.png";
            }

            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);// 获取文件后缀名
            inputStream = file.getInputStream();
            //调用上传文件方法
            return upload(extName, inputStream);
        } catch (Exception e) {
            log.error("OSS upload file error", e);
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }


    //检查用户上传文件的次数是否超出限制，防止多次上传文件
    private void checkUploadCount() {
        //获取用户ID
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        //从中缓存获取用户当天的上传次数
        Long times = redisService.getCacheMapValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), Long.class);
        // 如果上传次数不为空且达到最大限制，则抛出异常
        if (times != null && times >= maxTime) {
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD_TIME_LIMIT);
        }

        // 增加用户的上传次数
        redisService.incrementHashValue(CacheConstants.USER_UPLOAD_TIMES_KEY, String.valueOf(userId), 1);
        // 如果用户之前没有上传记录或者上传次数重置为0，则设置缓存的过期时间到次日零点
        if (times == null || times == 0) {
            // 计算当前时间到次日零点的秒数
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            // 设置缓存的过期时间
            redisService.expire(CacheConstants.USER_UPLOAD_TIMES_KEY, seconds, TimeUnit.SECONDS);
        }
    }


    /**
     * 上传文件到OSS服务
     * @param fileType 文件类型，用于确定文件扩展名
     * @param inputStream 文件输入流，用于读取文件内容
     * @return 返回OSSResult对象，包含文件上传结果信息
     */
    private OSSResult upload(String fileType, InputStream inputStream) {
        // 生成文件的唯一键，结合了配置的路径前缀和文件类型，防止同名的文件覆盖
        String key = prop.getPathPrefix() + ObjectId.next() + "." + fileType;

        // 创建对象元数据，用于设置文件的访问权限
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setObjectAcl(CannedAccessControlList.PublicRead);

        // 创建上传请求对象，包含桶名、文件键、文件流和元数据
        PutObjectRequest request = new PutObjectRequest(prop.getBucketName(), key, inputStream, objectMetadata);

        // 执行上传操作，并处理可能的异常
        PutObjectResult putObjectResult;
        try {
            putObjectResult = ossClient.putObject(request);
        } catch (Exception e) {
            // 记录异常信息，并抛出自定义的异常
            log.error("OSS put object error: {}", ExceptionUtil.stacktraceToOneLineString(e, 500));
            throw new ServiceException(ResultCode.FAILED_FILE_UPLOAD);
        }

        // 根据上传结果组装并返回OSSResult对象
        return assembleOSSResult(key, putObjectResult);
    }

    private OSSResult assembleOSSResult(String key, PutObjectResult putObjectResult) {
        OSSResult ossResult = new OSSResult();
        if (putObjectResult == null || StrUtil.isBlank(putObjectResult.getRequestId())) {
            ossResult.setSuccess(false);//上传失败
        } else {
            ossResult.setSuccess(true);//上传成功
            ossResult.setName(FileUtil.getName(key));
        }
        return ossResult;
    }
}
