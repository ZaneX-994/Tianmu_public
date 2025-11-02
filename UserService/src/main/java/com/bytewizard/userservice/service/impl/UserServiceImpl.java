package com.bytewizard.userservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytewizard.common.constants.ErrorCode;
import com.bytewizard.common.constants.JWTConstant;
import com.bytewizard.common.constants.UserConstant;
import com.bytewizard.common.exception.BusinessException;
import com.bytewizard.common.utils.JwtUtil;
import com.bytewizard.userservice.client.VideoActionServiceClient;
import com.bytewizard.userservice.constants.SMSConstant;
import com.bytewizard.userservice.domain.dto.LoginCodeRequest;
import com.bytewizard.userservice.domain.dto.LoginPasswordRequest;
import com.bytewizard.userservice.domain.dto.RegisterRequest;
import com.bytewizard.userservice.domain.dto.UserInfoRequest;
import com.bytewizard.userservice.domain.entity.User;
import com.bytewizard.userservice.domain.entity.UserStats;
import com.bytewizard.userservice.domain.vo.LoginResponse;
import com.bytewizard.userservice.domain.vo.UserInfoResponse;
import com.bytewizard.userservice.mapper.UserMapper;
import com.bytewizard.userservice.service.UserService;
import com.bytewizard.userservice.service.UserStatsService;
import com.bytewizard.userservice.utils.CodeGenerator;
import com.bytewizard.userservice.utils.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserStatsService userStatsService;

    @Autowired
    private VideoActionServiceClient videoActionServiceClient;

    @Override
    public void sendVerificationCode(String account) {

        if (StringUtils.isBlank(account)) {
            throw new BusinessException(ErrorCode.PHONE_EMAIL_ERROR);
        }

        // 生成验证码并发送 (只支持邮箱发送)
        if (account.matches(UserConstant.EMAIL_REGEX)) {
            String code = CodeGenerator.generateVerificationCode();
            redisTemplate.opsForValue().set(account, code, SMSConstant.SMS_EXPIRE_TIME, TimeUnit.MINUTES);
            EmailUtil.sendEmailCode(account, code);
        } else if (account.matches(UserConstant.PHONE_REGEX)) {
            throw new BusinessException(ErrorCode.PHONE_REGISTRATION_NOT_SUPPORTED);
        }else {
            throw new BusinessException(ErrorCode.PHONE_EMAIL_ERROR);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) throws Exception {

        // 1. 参数校验
        validateRegisterRequest(request);

        // 2. 检查用户是否已存在
        if (isRegistered(request.getAccount()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        } else {
            // 3. 验证码校验
            String redisCode = redisTemplate.opsForValue().get(request.getAccount());
            if (StringUtils.isBlank(redisCode) || !redisCode.equals(request.getCode())) {
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
            }
            // 4. 创建用户
            User newUser = createUser(request);

            // 5. 保存用户信息(并发安全处理)
            return saveUserAndGenerateToken(newUser, request.getAccount());
        }
    }

    @Override
    public LoginResponse loginPassword(LoginPasswordRequest request, HttpServletRequest httpServletRequest) {

        User user = isRegistered(request.getAccount());

        // 1. 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        } else {

            // 2. 验证密码
            String encryptedPassword = DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + request.getPassword()).getBytes());
            if (!user.getPassword().equals(encryptedPassword)) {
                throw new BusinessException(ErrorCode.LOGIN_ERROR);
            }

            // 3. 设置用户信息
            LoginResponse loginResponse = new LoginResponse();
            BeanUtil.copyProperties(user, loginResponse);

            // 4. 设置用户统计信息
            UserStats userStats = userStatsService.getById(user.getUserId());
            BeanUtil.copyProperties(userStats, loginResponse);

            // 5. 生成jwt令牌
            String token = JwtUtil.generate(user.getUserId() + "");
            redisTemplate.opsForValue().set(user.getUserId().toString(), token, JWTConstant.JWT_TIME_OUT, TimeUnit.DAYS);

            loginResponse.setToken(token);
            return loginResponse;

        }
    }

    @Override
    public LoginResponse loginCode(LoginCodeRequest request, HttpServletRequest httpServletRequest) {

        User user = isRegistered(request.getAccount());

        // 1. 检查用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        } else {
            // 2. 检查验证码
            String verificationCode = redisTemplate.opsForValue().get(request.getAccount());

            if (verificationCode == null || !verificationCode.equals(request.getCode())) {
                throw new BusinessException(ErrorCode.VERIFICATION_CODE_ERROR);
            }

            // 3. 设置用户信息
            LoginResponse loginResponse = new LoginResponse();
            BeanUtil.copyProperties(user, loginResponse);

            // 4. 设置用户统计信息
            UserStats userStats = userStatsService.getById(user.getUserId());
            BeanUtil.copyProperties(userStats, loginResponse);

            redisTemplate.delete(request.getAccount());

            // 5. 生成jwt令牌
            String token = JwtUtil.generate(user.getUserId() + "");
            redisTemplate.opsForValue().set(user.getUserId().toString(), token, JWTConstant.JWT_TIME_OUT, TimeUnit.DAYS);

            loginResponse.setToken(token);

            return loginResponse;

        }
    }

    @Override
    public Boolean userLogout(Long userId, HttpServletRequest request) {
        redisTemplate.delete(userId.toString());
        return true;
    }

    @Override
    public UserInfoResponse getUserInfo(UserInfoRequest userInfoRequest) {

        // 查询用户信息判断用户是否存在
        User user = this.getById(userInfoRequest.getCreatorId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXISTS);
        }

        // 用户基本信息
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        BeanUtil.copyProperties(user, userInfoResponse);

        // 用户统计信息
        UserStats userStats = userStatsService.getById(userInfoRequest.getCreatorId());
        BeanUtil.copyProperties(userStats, userInfoResponse);

        Long userId = userInfoRequest.getUserId();
        if (userId != null) {
            // 用户关注状态
            userInfoResponse.setFollow(videoActionServiceClient.followType(userInfoRequest.getUserId(), userInfoRequest.getCreatorId()));
        } else {
            userInfoResponse.setFollow(0);
        }
        return userInfoResponse;
    }


    // =========================== Private Helpers =============================

    /**
     * 校验注册请求参数
     */
    private void validateRegisterRequest(RegisterRequest request) {

        String account = request.getAccount();
        if (!account.matches(UserConstant.PHONE_REGEX) && !account.matches(UserConstant.EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号必须是有效的手机号或邮箱");
        }
        if (StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (StringUtils.isBlank(request.getNickName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称不能为空");
        }
    }

    /**
     * 检查用户是否已存在
     */
    private User isRegistered(String account) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (account.matches(UserConstant.EMAIL_REGEX)) {
            wrapper.eq(User::getEmail, account);
        } else if (account.matches(UserConstant.PHONE_REGEX)) {
            wrapper.eq(User::getPhone, account);
        }

        return this.getOne(wrapper);
    }

    /**
     * 创建用户实体
     */
    private User createUser(RegisterRequest request) {

        User user = new User();
        user.setUserId(IdUtil.getSnowflake().nextId());
        user.setNickname(request.getNickName());

        // 设置账号(手机号或邮箱)
        if (request.getAccount().matches(UserConstant.EMAIL_REGEX)) {
            user.setEmail(request.getAccount());
            user.setPhone("");
        }

        // 密码加密
        String password = request.getPassword();
        String encryptedPassword = DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + password).getBytes());
        user.setPassword(encryptedPassword);

        return user;
    }

    /**
     * 保存用户并生成Token(并发安全处理)
     */
    private LoginResponse saveUserAndGenerateToken(User user, String account) {
        synchronized (account.intern()) {
            // 保存用户
            boolean saveSuccess = this.baseMapper.insert(user) > 0;

            // 初始化用户统计信息
            UserStats stats = new UserStats();
            stats.setUserId(user.getUserId());
            boolean saveStatsSuccess = userStatsService.save(stats);

            if (!saveSuccess || !saveStatsSuccess) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户注册失败");
            }

            // 清理验证码
            redisTemplate.delete(account);

            // 生成 Token
            String token = JwtUtil.generate(user.getUserId().toString());
            redisTemplate.opsForValue().set(user.getUserId().toString(), token, JWTConstant.JWT_TIME_OUT, TimeUnit.DAYS);

            // 返回用户信息和 Token
            LoginResponse response = this.baseMapper.getUserInfo(user.getUserId());
            response.setToken(token);

            return response;
        }
    }

}