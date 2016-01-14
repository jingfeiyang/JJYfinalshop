package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class DeliveryCorpValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("deliveryCorp.name", "errorMessages", "物流公司名称不允许为空!");
		validateRequiredString("deliveryCorp.orderList", "errorMessages", "排序不允许为空!");
		validateUrl("deliveryCorp.url", "errorMessages", "网址格式错误!");
		
		validateInteger("deliveryCorp.orderList", 0, 100, "errorMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}
}
