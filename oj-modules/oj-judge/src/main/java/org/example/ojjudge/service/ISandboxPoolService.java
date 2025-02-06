package org.example.ojjudge.service;


import org.example.ojjudge.domain.SandBoxExecuteResult;

import java.util.List;

public interface ISandboxPoolService {
    SandBoxExecuteResult exeJavaCode(Long userId, String userCode, List<String> inputList);
}
