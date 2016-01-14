package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class RefundValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("refund.paymentConfig_id", "errorMessages", "退款方式不允许为空!");
		validateRequiredString("refundType", "errorMessages", "退款类型不允许为空!");
		validateRequiredString("refund.totalAmount", "errorMessages", "内容不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/admin/error.html");
	}

}
