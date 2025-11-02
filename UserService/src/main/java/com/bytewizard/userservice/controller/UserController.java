package com.bytewizard.userservice.controller;

import com.bytewizard.common.domain.BaseResponse;
import com.bytewizard.common.utils.ResultUtils;
import com.bytewizard.userservice.constants.SMSConstant;
import com.bytewizard.userservice.domain.dto.LoginCodeRequest;
import com.bytewizard.userservice.domain.dto.LoginPasswordRequest;
import com.bytewizard.userservice.domain.dto.RegisterRequest;
import com.bytewizard.userservice.domain.dto.UserInfoRequest;
import com.bytewizard.userservice.domain.entity.User;
import com.bytewizard.userservice.domain.vo.LoginResponse;
import com.bytewizard.userservice.domain.vo.UserInfoResponse;
import com.bytewizard.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // 获取所有用户
    @GetMapping
    public List<User> listAllUsers() {
        return userService.list();
    }

    // 根据ID获取用户
    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @GetMapping("/sendVerificationCode")
    public BaseResponse<String> sendVerificationCode(@RequestParam(value = "email") String email) {

        userService.sendVerificationCode(email);

        return ResultUtils.success(SMSConstant.SMS_SEND_SUCCESS_MSG);
    }

    @PostMapping("/register")
    public BaseResponse<LoginResponse> register(@RequestBody RegisterRequest request) throws Exception {

        LoginResponse response = userService.register(request);

        return ResultUtils.success(response);
    }

    // 密码登陆
    @PostMapping("/loginPassword")
    public BaseResponse<LoginResponse> loginPassword(@Valid @RequestBody LoginPasswordRequest loginPasswordRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.loginPassword(loginPasswordRequest, request));
    }


    // 验证码登陆
    @PostMapping("/loginCode")
    public BaseResponse<LoginResponse> loginCode(@Valid @RequestBody LoginCodeRequest loginCodeRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.loginCode(loginCodeRequest, request));
    }

    @GetMapping("/logout")
    public BaseResponse<Boolean> logout(@NotNull(message = "手用户id不能为空") @RequestParam Long userId, HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(userId, request));
    }

    @PostMapping("/info")
    public BaseResponse<UserInfoResponse> getUserInfo(@RequestBody UserInfoRequest userInfoRequest) {
        return ResultUtils.success(userService.getUserInfo(userInfoRequest));
    }

}