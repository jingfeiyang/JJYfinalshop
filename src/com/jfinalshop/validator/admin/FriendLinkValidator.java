package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.FriendLink;

public class FriendLinkValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		c.getFile();
		validateRequiredString("friendLink.name", "nameMessages", "友情链接名称不允许为空!");
		validateUrl("friendLink.url", "urlMessages", "链接地址不合法，请以http://或https://开头!");
		validateRequiredString("friendLink.orderList", "orderListMessages", "排序不允许为空!");
		validateInteger("friendLink.orderList", 0, 200, "orderListMessages", "排序必须为零或正整数!");
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(FriendLink.class);
		c.render("/admin/friend_link_input.html");
	}

}
