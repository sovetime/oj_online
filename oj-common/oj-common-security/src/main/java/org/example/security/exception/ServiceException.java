package org.example.security.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.common.core.enums.ResultCode;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Date: 2025-01-08
 * Time: 14:18
 */
@Getter
@AllArgsConstructor
public class ServiceException extends RuntimeException {
    private ResultCode resultCode;
}
