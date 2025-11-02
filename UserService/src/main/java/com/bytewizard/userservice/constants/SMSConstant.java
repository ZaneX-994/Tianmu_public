package com.bytewizard.userservice.constants;

/**
 * SMS 常量
 */
public class SMSConstant {

    public static final Integer SMS_EXPIRE_TIME = 3;

    public static final String SMS_SEND_SUCCESS_MSG = "验证码发送成功";

    public static final String EMAIL_HOST_NAME = "smtp.qq.com";

    public static final String EMAIL_PROTOCOL = "mail.smtp.ssl.protocols";

    public static final String TLS_VERSION = "TLSv1.2";

    public static final String VERIFICATION_CODE_TEMPLATE = "您的验证码为: %s (1分钟内有效)";

    public static final String EMAIL_USER_NAME = "1393895459@qq.com";

    public static final String EMAIL_PASSWORD = "rwfqgnkkljzubabd";

    public static final String EMAIL_NAME = "TianMu";

    public static final String EMAIL_SUBJECT = "注册验证码";

    public static final String EMAIL_EXCEPTION_LOG_TEMPLATE = "发送验证码到 {} 失败";

}
