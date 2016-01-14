package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class LogConfigValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("logConfig.operationName", "errorMessages", "操作名称不允许为空!");
		validateRequiredString("logConfig.actionClassName", "errorMessages", "Action类不允许为空!");
		validateRequiredString("logConfig.actionMethodName", "errorMessages", "Action方法不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");		
	}

}
