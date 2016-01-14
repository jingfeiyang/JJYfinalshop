package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class PaymentConfigValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("paymentConfig.name", "errorMessages", "支付方式名称不允许为空!");
		validateRequiredString("paymentConfig.paymentConfigType", "errorMessages", "支付方式类型不允许为空!");
		validateRequiredString("paymentConfig.paymentFeeType", "errorMessages", "支付手续费设置不允许为空!");
		validateRequiredString("paymentConfig.paymentFee", "errorMessages", "支付手续费不允许为空!");
		validateRequiredString("paymentConfig.orderList", "errorMessages", "排序不允许为空!");
		
		validateInteger("paymentConfig.orderList", 0, 50, "errorMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
