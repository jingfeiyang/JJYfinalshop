package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class MailValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("smtpFromMail", "errorMessages", "发件人邮箱不允许为空!");
		validateRequiredString("smtpHost", "errorMessages", "SMTP服务器地址不允许为空!");
		validateRequiredString("smtpUsername", "errorMessages", "SMTP用户名不允许为空!");
		validateRequiredString("smtpPassword", "errorMessages", "SMTP密码不允许为空!");
		validateRequiredString("smtpToMail", "errorMessages", "收件人邮箱不允许为空!");
		validateInteger("smtpPort", 0, 65535, "errorMessages", "SMTP端口必须为零正整数!");
		
		validateEmail("smtpFromMail", "errorMessages", "发件人邮箱格式错误!");
		validateEmail("smtpToMail", "errorMessages", "收件人邮箱格式错误!");		
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
