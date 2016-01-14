package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class AgreementValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("agreement.content", "errorMessages", "会员注册协议内容不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");		
	}
}
