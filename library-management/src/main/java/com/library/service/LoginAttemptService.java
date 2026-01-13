package com.library.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录尝试服务
 * 用于记录和限制登录失败次数，防止暴力破解
 */
@Slf4j
@Service
public class LoginAttemptService {

    /**
     * 最大允许失败次数
     */
    private static final int MAX_ATTEMPTS = 5;

    /**
     * 锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 15;

    /**
     * 存储登录尝试信息：用户名 -> 尝试信息
     */
    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    /**
     * 登录尝试信息
     */
    private static class LoginAttempt {
        int count;
        LocalDateTime firstAttemptTime;
        LocalDateTime lockTime;

        LoginAttempt() {
            this.count = 1;
            this.firstAttemptTime = LocalDateTime.now();
            this.lockTime = null;
        }
    }

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return true 如果被锁定
     */
    public boolean isBlocked(String username) {
        LoginAttempt attempt = attempts.get(username);
        if (attempt == null) {
            return false;
        }

        // 检查是否在锁定期内
        if (attempt.lockTime != null) {
            if (LocalDateTime.now().isBefore(attempt.lockTime.plusMinutes(LOCK_TIME_MINUTES))) {
                return true;
            } else {
                // 锁定期已过，重置
                attempts.remove(username);
                return false;
            }
        }

        return false;
    }

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param username 用户名
     * @return 剩余锁定秒数，0 表示未锁定
     */
    public long getRemainingLockTimeSeconds(String username) {
        LoginAttempt attempt = attempts.get(username);
        if (attempt == null || attempt.lockTime == null) {
            return 0;
        }

        LocalDateTime unlockTime = attempt.lockTime.plusMinutes(LOCK_TIME_MINUTES);
        if (LocalDateTime.now().isBefore(unlockTime)) {
            return java.time.Duration.between(LocalDateTime.now(), unlockTime).getSeconds();
        }

        return 0;
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     */
    public void loginFailed(String username) {
        LoginAttempt attempt = attempts.get(username);

        if (attempt == null) {
            attempts.put(username, new LoginAttempt());
            log.warn("用户 {} 登录失败，第 1 次尝试", username);
        } else {
            attempt.count++;
            log.warn("用户 {} 登录失败，第 {} 次尝试", username, attempt.count);

            if (attempt.count >= MAX_ATTEMPTS) {
                attempt.lockTime = LocalDateTime.now();
                log.warn("用户 {} 登录失败次数过多，账号锁定 {} 分钟", username, LOCK_TIME_MINUTES);
            }
        }
    }

    /**
     * 登录成功，清除失败记录
     *
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        attempts.remove(username);
        log.debug("用户 {} 登录成功，清除失败记录", username);
    }

    /**
     * 获取失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getFailedAttempts(String username) {
        LoginAttempt attempt = attempts.get(username);
        return attempt != null ? attempt.count : 0;
    }

    /**
     * 获取剩余可尝试次数
     *
     * @param username 用户名
     * @return 剩余次数
     */
    public int getRemainingAttempts(String username) {
        return Math.max(0, MAX_ATTEMPTS - getFailedAttempts(username));
    }

    /**
     * 获取最大允许失败次数
     */
    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    /**
     * 获取锁定时间（分钟）
     */
    public int getLockTimeMinutes() {
        return LOCK_TIME_MINUTES;
    }
}
