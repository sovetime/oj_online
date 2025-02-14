package org.example.ojjudge.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.constants.Constants;
import org.example.common.core.constants.JudgeConstants;
import org.example.common.core.enums.CodeRunStatus;
import org.example.ojjudge.callback.DockerStartResultCallback;
import org.example.ojjudge.callback.StatisticsCallback;
import org.example.ojjudge.config.DockerSandBoxPool;
import org.example.ojjudge.domain.CompileResult;
import org.example.ojjudge.domain.SandBoxExecuteResult;
import org.example.ojjudge.service.ISandboxPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SandboxPoolServiceImpl implements ISandboxPoolService {

    @Autowired
    private DockerSandBoxPool sandBoxPool;
    @Autowired
    private DockerClient dockerClient;

    private String containerId;
    private String userCodeFileName;

    @Value("${sandbox.limit.time:5}")
    private Long timeLimit;

    @Override
    public SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList) {
        containerId = sandBoxPool.getContainer();
        //创建并返回用户代码的文件
        createUserCodeFile(userCode);
        //编译代码
        CompileResult compileResult = compileCodeByDocker();
        //编译失败，删除容器和用户代码文件，并返回编译失败的信息
        if (!compileResult.isCompiled()) {
            sandBoxPool.returnContainer(containerId);
            deleteUserCodeFile();
            return SandBoxExecuteResult.fail(CodeRunStatus.COMPILE_FAILED, compileResult.getExeMessage());
        }
        //执行测试用例的代码
        return executeJavaCodeByDocker(inputList);
    }

    //创建并返回用户代码的文件
    private void createUserCodeFile(String userCode) {
        String codeDir = sandBoxPool.getCodeDir(containerId);
        log.info("user-pool路径信息：{}", codeDir);
        userCodeFileName = codeDir + File.separator + JudgeConstants.USER_CODE_JAVA_CLASS_NAME;
        //如果文件之前存在，将之前的文件删除掉,后续拓展历史记录可以保留下来，在进行改进
        if (FileUtil.exist(userCodeFileName)) {
            FileUtil.del(userCodeFileName);
        }
        //创建用户代码文件
        FileUtil.writeString(userCode, userCodeFileName, Constants.UTF8);
    }


    //使用docker编译
    private CompileResult compileCodeByDocker() {
        //创建编译命令
        String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVAC_CMD, null, containerId);
        //创建一个回调对象来处理编译结果
        DockerStartResultCallback resultCallback = new DockerStartResultCallback();
        //初始化编译结果对象
        CompileResult compileResult = new CompileResult();
        try {
            log.info("启动编译命令并等待完成");
            dockerClient.execStartCmd(cmdId)
                    .exec(resultCallback)
                    .awaitCompletion();
            //根据回调对象的状态设置编译结果
            if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                compileResult.setCompiled(false);
                compileResult.setExeMessage(resultCallback.getErrorMessage());
            } else {
                compileResult.setCompiled(true);
            }
            return compileResult;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //执行测试用例的代码
    private SandBoxExecuteResult executeJavaCodeByDocker(List<String> inputList) {
        List<String> outList = new ArrayList<>(); //记录输出结果
        long maxMemory = 0L;
        long maxUseTime = 0L;

        //执行用户代码
        for (String inputArgs : inputList) {
            //创建执行命令
            String cmdId = createExecCmd(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArgs, containerId);
            //创建计时器
            StopWatch stopWatch = new StopWatch();
            //执行情况监控
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            //启动监控
            StatisticsCallback statisticsCallback = statsCmd.exec(new StatisticsCallback());
            //开始计时
            stopWatch.start();
            //创建一个回调对象来处理执行结果
            DockerStartResultCallback resultCallback = new DockerStartResultCallback();
            try {
                log.info("执行命令并等待完成");
                dockerClient.execStartCmd(cmdId)
                        .exec(resultCallback)//执行回调
                        .awaitCompletion(100, TimeUnit.SECONDS);
                log.info("回调结果: {}", resultCallback.getMessage());
                if (CodeRunStatus.FAILED.equals(resultCallback.getCodeRunStatus())) {
                    //未通过所有用例返回结果
                    return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //结束计时
            stopWatch.stop();
            //停止监控
            try {
                statisticsCallback.await(5,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            statsCmd.close();

            long userTime = stopWatch.getLastTaskTimeMillis(); //执行耗时
            maxUseTime = Math.max(userTime, maxUseTime);       //记录最大的执行用例耗时
            Long memory = statisticsCallback.getMaxMemory();  //获取最大内存使用量
            log.info("memory->{}",memory);
            if (memory != null) {
                maxMemory = Math.max(maxMemory, statisticsCallback.getMaxMemory()); //记录最大的执行用例占用内存
            }
            //记录正确的输出结果
            outList.add(resultCallback.getMessage().trim());
        }
        sandBoxPool.returnContainer(containerId);
        //清理文件
        deleteUserCodeFile();
        return getSanBoxResult(inputList, outList, maxMemory, maxUseTime); //封装结果
    }

    /**
     * 构建并执行Docker容器中的命令
     *
     * @param javaCmdArr 基础命令数组，用于执行Java命令
     * @param inputArgs 用户输入的附加参数，将被拆分成数组并追加到基础命令数组后
     * @param containerId 容器ID，指定在哪个容器中执行命令
     * @return 返回执行命令的ID
     */
    private String createExecCmd(String[] javaCmdArr, String inputArgs, String containerId) {
        //有输入是需要拼接输入参数
        if (!StrUtil.isEmpty(inputArgs)) {
            String[] inputArray = inputArgs.split(" "); //输入参数以空格分割
            javaCmdArr = ArrayUtil.append(JudgeConstants.DOCKER_JAVA_EXEC_CMD, inputArray);
        }
        // 在指定容器中创建执行命令
        ExecCreateCmdResponse cmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(javaCmdArr)//创建执行命令
                .withAttachStderr(true)//捕获标准错误流
                .withAttachStdin(true)//捕获标准输入流
                .withAttachStdout(true)//捕获标准输出流
                .exec();//执行命令并返回响应
        // 返回执行命令的ID
        return cmdResponse.getId();
    }


    //获取判题结果
    private SandBoxExecuteResult getSanBoxResult(List<String> inputList, List<String> outList,
                                                 long maxMemory, long maxUseTime) {
        //输入输出不一样属于没有通过的情况
        if (inputList.size() != outList.size()) {
            return SandBoxExecuteResult.fail(CodeRunStatus.NOT_ALL_PASSED, outList, maxMemory, maxUseTime);
        }
        return SandBoxExecuteResult.success(CodeRunStatus.SUCCEED, outList, maxMemory, maxUseTime);
    }

    //删除用户代码文件
    private void deleteUserCodeFile() {
        FileUtil.del(userCodeFileName);
    }
}
