package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Message;

public class MessageValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("toMemberUsername", "nameMessages", "收件人不允许为空!");
		validateRequiredString("message.title", "titleMessages", "标题不允许为空!");
		validateRequiredString("message.content", "contentMessages", "消息内容不允许为空!");		
		validateString("message.content", 1, 10000, "contentMessages", "消息内容长度不能为空或超出限制，必须小于10000!");
		// 检查用户名是否存在
		String name = c.getPara("toMemberUsername","");	
		if (!Member.dao.containUsername(name)) {
			addError("nameMessages","收件人不存在!");
		}
	}

	@Override
	protected void handleError(Controller c) {
		String id  = c.getPara("message.id","");
		c.keepModel(Message.class);
		if (StrKit.notBlank(id)){
			c.setAttr("message", Message.dao.findById(id));
			c.render("/admin/message_reply.html");
		} else {
			c.render("/admin/message_send.html");
		}
	}

}
