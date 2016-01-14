package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class MemberAttributeValidator extends Validator	{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("memberAttribute.name", "errorMessages", "注册项名称不允许为空!");
		validateRequiredString("attributeType", "errorMessages", "自定义注册项类型不允许为空!");
		validateRequiredString("memberAttribute.isRequired", "errorMessages", "是否必填不允许为空!");
		validateRequiredString("memberAttribute.isEnabled", "errorMessages", "是否启用不允许为空!");
		validateRequiredString("memberAttribute.orderList", "errorMessages", "排序不允许为空!");
		
		validateInteger("memberAttribute.orderList", 0, 100, "errorMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
