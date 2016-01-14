package com.jfinalshop.validator.shop;


import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;


public class MemberValidator extends Validator {
	
	protected String actionKey;
	
    @Override
    protected void validate(Controller c) {
    	actionKey = getActionKey();   
    	if (actionKey.equals("/shop/member/ajaxRegister")) { // 注册验证
    		validateRequiredString("member.username", "errorMessages", "用户名不允许为空!");
        	validateRequiredString("member.password", "errorMessages", "密码不允许为空!");
    		validateRequiredString("member.email", "errorMessages", "E-mail不允许为空!");    		
    		validateString("member.username", 2, 20, "errorMessages", "用户名长度必须在【2】到【20】之间!");
    		validateString("member.password", 4, 20, "errorMessages", "密码长度必须在【4】到【20】之间!");    		
    		validateEmail("member.email", "errorMessages", "E-mail格式错误!");
    		validateRegex("member.username", "^[0-9a-z_A-Z\u4e00-\u9fa5]+$", "errorMessages", "用户名只允许包含中文、英文、数字和下划线!");    	
    	
    	} else if (actionKey.equals("/shop/member/login") || actionKey.equals("/shop/member/ajaxLogin")) { // 登录验证    	|| Ajax会员登录验证	
    		validateRequiredString("member.username", "errorMessages", "用户名不允许为空!");
        	validateRequiredString("member.password", "errorMessages", "密码不允许为空!");    	
    	
    	} else if (actionKey.equals("/shop/member/sendPasswordRecoverMail")) { // 发送密码找回邮件验证    		
    		validateRequiredString("member.username", "errorMessages", "用户名不允许为空!");
        	validateRequiredString("member.email", "errorMessages", "E-mail不允许为空!");
        	validateEmail("member.email", "errorMessages", "E-mail格式错误!");
    	
    	} else if (actionKey.equals("/shop/member/passwordModify")) { // 密码修改
    		validateRequiredString("id", "errorMessages", "ID不允许为空!");
        	validateRequiredString("passwordRecoverKey", "errorMessages", "passwordRecoverKey不允许为空!");
    	
    	} else if (actionKey.equals("/shop/member/passwordUpdate")) { // 密码更新
    		validateRequiredString("member.id", "errorMessages", "ID不允许为空!");
        	validateRequiredString("passwordRecoverKey", "errorMessages", "passwordRecoverKey不允许为空!");
        	validateRequiredString("member.password", "errorMessages", "密码不允许为空!");
        	validateString("member.password", 4, 20, "errorMessages", "密码长度必须在【4】到【20】之间!");
    	}
    	
    }

    @Override
    protected void handleError(Controller c) {
    	if (actionKey.equals("/shop/member/ajaxRegister")) { // 注册验证
    		c.renderText("注册信息错误!");
    	} else if (actionKey.equals("/shop/member/ajaxLogin")) { // Ajax会员登录验证	
    		c.renderText("Ajax会员登录验证失败");
    	} else {
    		c.render("/shop/error.html");
    	}
    	
    }
}
