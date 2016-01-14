package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class FooterValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("footer.content", "errorMessages", "内容不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
