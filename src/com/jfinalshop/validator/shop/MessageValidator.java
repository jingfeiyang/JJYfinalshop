package com.jfinalshop.validator.shop;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class MessageValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("message.title", "errorMessages", "标题不允许为空!");
		validateRequiredString("message.content", "errorMessages", "消息内容不允许为空!");
		validateString("message.content", 0, 10000, "errorMessages", "消息内容长度超出限制，必须小于10000!");
		
		validateRequiredString("message.isSaveDraftbox", "errorMessages", "是否保存草稿箱不允许为空!");
	}

	@Override
	protected void handleError(Controller c) {
		c.render("/shop/error.html");
	}

}
