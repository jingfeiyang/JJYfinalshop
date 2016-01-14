package com.jfinalshop.validator.shop;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class ProfileValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("member.email", "errorMessages", "E-mail不允许为空!");
		validateEmail("member.email", "errorMessages", "E-mail格式错误!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/shop/error.html");
	}

}
