package org.example.ojjudge.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.Constants;
import org.example.common.core.constants.JudgeConstants;
import org.example.common.core.enums.CodeRunStatus;
import org.example.ojjudge.callback.DockerStartResultCallback;
import org.example.ojjudge.callback.StatisticsCallback;
import org.example.ojjudge.domain.CompileResult;
import org.example.ojjudge.domain.SandBoxExecuteResult;
import org.example.ojjudge.service.ISandboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SandboxServiceImpl implements ISandboxService {

    //@Value("${sandbox.docker.host:tcp://119.23.53.45:2375}")
    @Value("${sandbox.docker.host:tcp://localhost:2375}")
    private String dockerHost;
    @Value("${sandbox.limit.memory:100000000}")
    private Long memoryLimit;
    @Value("${sandbox.limit.memory-swap:100000000}")
    private Long memorySwapLimit;
    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;//cpu核数
    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;//时间限制

    private DockerClient dockerClient;//docker客户端
    private String containerId;
    private String userCodeDir;
    private String userCodeFileName;

    //执行用户代码
    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        //创建并返回用户代码的文件
        createUserCodeFile(userId, userCode);
        //初始化docker沙箱
        initDockerSanBox();
        //编译代码
        CompileResult compileResult = compileCodeByDocker();
        //如果代码编译失败，删除容器和用户代码文件，并返回编译失败的信息
        if (!compileResult.isCompiled()) {
            deleteContainer();
            deleteUserCodeFile();
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }
        //执行代码
        return executeJavaCodeByDocker(inputList);
    }

    //创建并返回用户代码的文件
    //System.getProperty("user.dir") 用于获取当前项目的根目录
    //File.separator表示文件路径分隔符：Windows中值为 \（反斜杠）
    private void createUserCodeFile(Long userId, String userCode) {
        // 获取当前项目的根目录，拼接存放用户代码的目录路径
        String examCodeDir = System.getProperty("user.dir") + File.separator + JudgeConstants.EXAM_CODE_DIR;
        // 检查存放用户代码的目录是否存在，如果不存在则创建
        if (!FileUtil.exist(examCodeDir)) {
            FileUtil.mkdir(examCodeDir); //创建存放用户代码的目录
        }
        // 获取当前时间，用于生成唯一的文件名
        String time = LocalDateTimeUtil.format(LocalDateTime.now(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 拼接用户代码文件格式
        userCodeDir = examCodeDir + File.separator + userId + Constants.UNDERLINE_SEPARATOR + time;
        // 拼接用户代码文件名，固定Java类名
        userCodeFileName = userCodeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        // 将用户代码内容写入到文件中，使用UTF-8编码
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
    }

    // 初始化docker沙箱
    private void initDockerSanBox() {
        //构建Docker客户端配置
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        //创建Docker客户端实例
        dockerClient = DockerClientBuilder
                .getInstance(clientConfig)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();

        //拉取镜像
        pullJavaEnvImage();

        //创建容器，限制资源，控制权限
        HostConfig hostConfig = getHostConfig();
        CreateContainerCmd containerCmd = dockerClient
                .createContainerCmd(JudgeConstants.JAVA_ENV_IMAGE)
                .withName(JudgeConstants.JAVA_CONTAINER_NAME);

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        // 记录容器id
        containerId = createContainerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
    }


    //拉取java执行环境镜像 需要控制只拉取一次
    private void pullJavaEnvImage() {
        //列出Docker客户端的所有镜像
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        List<Image> imageList = listImagesCmd.exec();
        for (Image image : imageList) {
            String[] repoTags = image.getRepoTags();
            // 检查镜像是否为所需的Java环境镜像
            if (repoTags != null && repoTags.length > 0 && JudgeConstants.JAVA_ENV_IMAGE.equals(repoTags[0])) {
                // 如果镜像已存在，则不执行进一步操作
                return;
            }
        }
        // 如果镜像不存在，则从Docker仓库中拉取
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(JudgeConstants.JAVA_ENV_IMAGE);
        try {
            // 执行镜像拉取并等待操作完成
            pullImageCmd.exec(new PullImageResultCallback()).awaitCompletion();
        } catch (InterruptedException e) {
            // 如果拉取过程中被中断，则抛出运行时异常
            throw new RuntimeException(e);
        }
    }


    //限制资源   控制权限
    private HostConfig getHostConfig() {
        HostConfig hostConfig = new HostConfig();
        //设置挂载目录，指定用户代码路径
        hostConfig.setBinds(new Bind(userCodeDir, new Volume(JudgeConstants.DOCKER_USER_CODE_DIR)));
        //限制docker容器使用资源
        hostConfig.withMemory(memoryLimit);//设置内存限制
        hostConfig.withMemorySwap(memorySwapLimit);//设置内存交换限制
        hostConfig.withCpuCount(cpuLimit);//设置CPU核数
        hostConfig.withNetworkMode("none");  //禁用网络
        hostConfig.withReadonlyRootfs(true); //禁止在root目录写文件
        return hostConfig;
    }

    //使用Docker编译代码
    private CompileResult compileCodeByDocker() {
        //创建编译命令
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);
        //创建一个回调对象来处理编译结果
        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        //初始化编译结果对象
        CompileResult compileResult = new CompileResult();
        try {
            // 启动编译命令并等待完成
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    .awaitCompletion();
            // 根据回调对象的状态设置编译结果
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            // 直接抛出 已做统一异常处理  也可再做定制化处理
            throw new RuntimeException(e);
        }
    }


    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        List<String> outList = new ArrayList<>(); //记录输出结果
        long maxMemory = 0L;  //最大占用内存
        long maxUseTime = 0L; //最大运行时间
        //执行用户代码
        for (String inputArgs : inputList) {
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);
            //执行代码
            StopWatch stopWatch = new StopWatch();        //执行代码后开始计时
            //执行情况监控
            StatsCmd statsCmd = dockerClient.statsCmd(containerId); //启动监控
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            stopWatch.start();
            DockerStartResultCallback resultCallback = new DockerStartResultCallback();
            try {
                dockerClient.execStartCmd(cmdId)//执行命令
                        .exec(resultCallback)//执行回调
                        .awaitCompletion(timeLimit, TimeUnit.SECONDS);//设置超时时间
                log.info("回调结果: {}", resultCallback.getMessage());
                if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                    //未通过所有用例返回结果
                    return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            stopWatch.stop();  //结束时间统计
            statsCmd.close();  //结束docker容器执行统计
            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime, maxUseTime);       //记录最大的执行用例耗时
            Long memory = statisticsCallback.getMaxMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, statisticsCallback.getMaxMemory()); //记录最大的执行用例占用内存
            }
            outList.add(resultCallback.getMessage().trim());   //记录正确的输出结果
        }
        deleteContainer();//删除容器
        deleteUserCodeFile(); //清理文件

        return getSanBoxResult(inputList, outList, maxMemory, maxUseTime); //封装结果
    }


    //构建并执行Docker容器中的命令
    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        // 检查输入参数是否为空
        if (!StrUtil.isEmpty(inputArgs)) {
            // 当入参不为空时拼接入参
            String[] inputArray = inputArgs.split(" ");
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        // 在指定容器中创建执行命令
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        // 返回执行命令的ID
        return cmdResponse.getId();
    }


    private SandBoxExecuteResult getSanBoxResult(List<String> inputList, List<String> outList,
                                                 long maxMemory, long maxUseTime) {
        if (inputList.size() != outList.size()) {
            //输入用例数量 不等于 输出用例数量  属于执行异常
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }

    private void deleteContainer() {
        //执行完成之后删除容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        //断开和docker连接
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteUserCodeFile() {
        FileUtil.del(userCodeDir);
    }
}
