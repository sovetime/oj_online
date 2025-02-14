package org.example.ojjudge.callback;

import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.common.core.enums.CodeRunStatus;


@Getter
@Setter
@Slf4j
//ExecStartResultCallback是dockerjava提供的一个回调函数，用于处理docker容器的输出信息
public class DockerStartResultCallback extends ExecStartResultCallback {

    private CodeRunStatus codeRunStatus;  //代码执行成功还是失败

    private String errorMessage;//代码运行失败的错误信息

    private String message;//代码正常运行的输出信息

    /**
     * 处理接收到的帧数据
     * 该方法根据帧中的流类型决定如何处理帧的有效载荷
     * 如果是错误流，则累积错误消息并设置代码运行状态为失败；
     * 如果不是错误流，则将有效载荷作为普通消息保存，并设置代码运行状态为成功
     *
     * @param frame 接收到的帧对象，包含流类型和有效载荷
     */
    @Override
    public void onNext(Frame frame) {
        // 获取帧的流类型
        StreamType streamType = frame.getStreamType();

        // 判断当前帧是否为错误流
        if (StreamType.STDERR.equals(streamType)) {
            // 如果错误消息为空，则初始化错误消息；否则，追加错误消息
            if (StrUtil.isEmpty(errorMessage)) {
                errorMessage = new String(frame.getPayload());
            } else {
                errorMessage = errorMessage + new String(frame.getPayload());
            }
            // 设置代码运行状态为失败
            codeRunStatus = CodeRunStatus.FAILED;
        } else {
            // 如果不是错误流，则将有效载荷作为普通消息保存
            message = new String(frame.getPayload());
            // 设置代码运行状态为成功
            codeRunStatus = CodeRunStatus.SUCCEED;
        }
        // 调用父类的onNext方法，继续处理帧
        super.onNext(frame);
    }
}



