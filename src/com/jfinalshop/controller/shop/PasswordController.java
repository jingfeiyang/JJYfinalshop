package com.jfinalshop.controller.shop;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinalshop.model.Member;
import com.jfinalshop.validator.shop.PasswordValidator;

/**
 * 前台类 - 密码、安全问题
 * 
 */
public class PasswordController extends BaseShopController<Member>{

	private Member member;
	private String oldPassword;
	
	// 密码修改
	public void edit() {
		setAttr("member", getLoginMember());
		render("/shop/password_input.html");
	}
	
	// 密码更新
	@Before(PasswordValidator.class)
	public void update() {
		member = getModel(Member.class);
		oldPassword = getPara("oldPassword","");
		Member persistent = getLoginMember();
		if (StringUtils.isNotEmpty(oldPassword) && StringUtils.isNotEmpty(member.getStr("password"))) {
			String oldPasswordMd5 = DigestUtils.md5Hex(oldPassword);
			if (!StringUtils.equals(persistent.getStr("password"), oldPasswordMd5)) {
				addActionError("旧密码不正确!");
				return;
			}
			String newPasswordMd5 = DigestUtils.md5Hex(member.getStr("password"));
			persistent.set("password",newPasswordMd5);
		}
		if (StringUtils.isNotEmpty(member.getStr("safeQuestion")) && StringUtils.isNotEmpty(member.getStr("safeAnswer"))) {
			persistent.set("safeQuestion",member.getStr("safeQuestion"));
			persistent.set("safeAnswer",member.getStr("safeAnswer"));
		}
		
		updated(persistent);
		renderSuccessMessage("更新成功!","/password/edit");	
	}
}
