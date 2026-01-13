package com.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.annotation.Log;
import com.library.entity.OperationLog;
import com.library.repository.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String method = request != null ? request.getMethod() : "";
        String url = request != null ? request.getRequestURI() : "";
        String ip = request != null ? getClientIp(request) : "";

        // 获取操作人信息
        String operator = "anonymous";
        Long operatorId = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            operator = auth.getName();
        }

        // 获取请求参数
        String params = "";
        if (logAnnotation.saveParams()) {
            params = getParams(joinPoint);
        }

        Object result = null;
        String resultStr = "";
        int status = 1;
        String errorMsg = "";

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录返回结果
            if (logAnnotation.saveResult() && result != null) {
                try {
                    resultStr = objectMapper.writeValueAsString(result);
                    if (resultStr.length() > 2000) {
                        resultStr = resultStr.substring(0, 2000) + "...";
                    }
                } catch (Exception e) {
                    resultStr = result.toString();
                }
            }

            return result;
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 2000);
            }
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            // 异步保存日志
            saveLogAsync(
                    logAnnotation.module(),
                    logAnnotation.operation().getDesc(),
                    logAnnotation.description(),
                    method,
                    url,
                    params,
                    resultStr,
                    operator,
                    operatorId,
                    ip,
                    costTime,
                    status,
                    errorMsg
            );
        }
    }

    /**
     * 异步保存日志
     */
    @Async
    public void saveLogAsync(String module, String operation, String description,
                             String method, String url, String params, String result,
                             String operator, Long operatorId, String ip,
                             long costTime, int status, String errorMsg) {
        try {
            OperationLog operationLog = OperationLog.builder()
                    .module(module)
                    .operation(operation)
                    .description(description)
                    .method(method)
                    .url(url)
                    .params(params)
                    .result(result)
                    .operator(operator)
                    .operatorId(operatorId)
                    .ipAddress(ip)
                    .operationTime(LocalDateTime.now())
                    .costTime(costTime)
                    .status(status)
                    .errorMsg(errorMsg)
                    .build();

            operationLogRepository.save(operationLog);
            log.debug("操作日志已保存: module={}, operation={}, operator={}", module, operation, operator);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 获取请求参数
     */
    private String getParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                // 过滤敏感参数和不可序列化对象
                if (arg == null) {
                    params.put(paramNames[i], null);
                } else if (arg instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile) arg;
                    params.put(paramNames[i], "文件: " + file.getOriginalFilename());
                } else if (isSensitiveParam(paramNames[i])) {
                    params.put(paramNames[i], "******");
                } else if (isSerializable(arg)) {
                    params.put(paramNames[i], arg);
                } else {
                    params.put(paramNames[i], arg.getClass().getSimpleName());
                }
            }

            String json = objectMapper.writeValueAsString(params);
            if (json.length() > 2000) {
                json = json.substring(0, 2000) + "...";
            }
            return json;
        } catch (Exception e) {
            log.warn("获取请求参数失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 判断是否为敏感参数
     */
    private boolean isSensitiveParam(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("password") || lower.contains("pwd")
                || lower.contains("secret") || lower.contains("token")
                || lower.contains("credential");
    }

    /**
     * 判断对象是否可序列化
     */
    private boolean isSerializable(Object obj) {
        return obj instanceof String || obj instanceof Number
                || obj instanceof Boolean || obj instanceof java.util.Date
                || obj instanceof java.time.temporal.Temporal
                || obj instanceof java.util.Map || obj instanceof java.util.Collection
                || obj.getClass().getName().startsWith("com.library.dto");
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
