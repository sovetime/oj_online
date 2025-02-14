package org.example.ojjudge.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerSandBoxPoolConfig {

    @Value("${sandbox.docker.host:tcp://localhost:2375}")
    private String dockerHost;//主机地址
    @Value("${sandbox.docker.image:openjdk:8-jdk-alpine}")
    private String sandboxImage;//镜像名称
    @Value("${sandbox.docker.volume:/usr/share/java}")
    private String volumeDir;//容器卷目录
    @Value("${sandbox.limit.memory:100000000}")
    private Long memoryLimit;//内存上限
    @Value("${sandbox.limit.memory-swap:100000000}")
    private Long memorySwapLimit;//内存和空间上限
    @Value("${sandbox.limit.cpu:1}")
    private Long cpuLimit;//CPU限制
    @Value("${sandbox.docker.pool.size:4}")
    private int poolSize;//池大小
    @Value("${sandbox.docker.name-prefix:oj-sandbox-jdk}")
    private String containerNamePrefix;//容器名称前缀

    /**
     * 创建DockerClient实例
     *
     * 该方法配置并返回一个DockerClient实例，用于与Docker守护进程进行通信
     * 它使用提供的dockerHost参数来配置客户端，以便连接到正确的Docker主机
     *
     * @return DockerClient 实例，用于执行Docker命令
     */
    @Bean
    public DockerClient createDockerClient() {
        // 创建默认的Docker客户端配置，并设置Docker主机地址
        DefaultDockerClientConfig clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .build();

        // 使用配置好的客户端配置实例化DockerClient
        // 这里选择使用NettyDockerCmdExecFactory来执行Docker命令，因为它提供了异步执行的能力
        return DockerClientBuilder
                .getInstance(clientConfig)
                .withDockerCmdExecFactory(new NettyDockerCmdExecFactory())
                .build();
    }

    /**
     * 创建Docker沙箱池
     *
     * 该方法通过注入DockerClient实例来初始化和配置Docker沙箱池此方法体现了对Docker沙箱池的配置，
     * 包括镜像、卷目录、内存限制、CPU限制、池大小以及容器名称前缀等参数这些配置项决定了沙箱环境的运行时特性，
     * 以及如何在限定的资源范围内高效运行多个容器实例
     *
     * @param dockerClient Docker客户端，用于与Docker守护进程交互
     * @return DockerSandBoxPool实例，用于管理Docker沙箱资源池
     */
    @Bean
    public DockerSandBoxPool createDockerSandBoxPool(DockerClient dockerClient) {
        // 初始化Docker沙箱池实例，传入配置参数
        DockerSandBoxPool dockerSandBoxPool = new DockerSandBoxPool(dockerClient, sandboxImage, volumeDir, memoryLimit,
                memorySwapLimit, cpuLimit, poolSize, containerNamePrefix);

        // 调用初始化方法来设置Docker池，准备资源分配
        dockerSandBoxPool.initDockerPool();
        // 返回配置好的Docker沙箱池实例
        return dockerSandBoxPool;
    }

}
