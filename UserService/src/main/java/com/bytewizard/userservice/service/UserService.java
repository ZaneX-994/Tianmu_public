package com.bytewizard.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytewizard.userservice.domain.dto.UserInfoRequest;
import com.bytewizard.userservice.domain.entity.User;
import com.bytewizard.userservice.domain.dto.LoginCodeRequest;
import com.bytewizard.userservice.domain.dto.LoginPasswordRequest;
import com.bytewizard.userservice.domain.dto.RegisterRequest;
import com.bytewizard.userservice.domain.vo.LoginResponse;
import com.bytewizard.userservice.domain.vo.UserInfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface UserService extends IService<User> {

    void sendVerificationCode(String email);

    LoginResponse register(RegisterRequest request) throws Exception;

    LoginResponse loginPassword(@Valid LoginPasswordRequest request, HttpServletRequest httpServletRequest);

    LoginResponse loginCode(@Valid LoginCodeRequest request, HttpServletRequest httpServletRequest);

    Boolean userLogout(@NotNull(message = "手用户id不能为空") Long userId, HttpServletRequest request);

    UserInfoResponse getUserInfo(UserInfoRequest userInfoRequest);
}