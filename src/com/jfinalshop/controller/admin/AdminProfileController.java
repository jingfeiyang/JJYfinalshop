package com.jfinalshop.controller.admin;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinalshop.model.Admin;
import com.jfinalshop.security.ShiroUtils;
import com.jfinalshop.validator.admin.AdminProfileValidator;

/**
 * 后台类 - 管理员个人资料
 * 
 */
public class AdminProfileController extends BaseAdminController<Admin> {

	private String currentId = ShiroUtils.getLoginAdminId();
	private Admin admin;
	private String currentPassword;

	// 编辑管理员资料
	public void edit() {
		setAttr("admin", Admin.dao.findById(getLoginAdminId()));
		render("/admin/admin_profile_input.html");
	}

	// 更新个人资料
	@Before(AdminProfileValidator.class)
	public void update(){
		admin = getModel(Admin.class);	
		currentPassword = getPara("currentPassword", "");
		
		Admin persistent = Admin.dao.findById(getLoginAdminId());
		if (StringUtils.isNotEmpty(currentPassword) && StringUtils.isNotEmpty(admin.getStr("password"))) {
			if (!StringUtils.equals(DigestUtils.md5Hex(currentPassword), persistent.getStr("password"))) {
				addActionError("当前密码输入错误!");
				return;
			}
			if (StringUtils.equals(DigestUtils.md5Hex(currentPassword), admin.getStr("password"))) {
				addActionError("新旧密码相同!");
				return;
			}
			admin.set("password",DigestUtils.md5Hex(admin.getStr("password")));
		}
		admin.set("id", getLoginAdminId());
		updated(admin);
		setAttr("admin", Admin.dao.findById(getLoginAdminId()));
		renderSuccessMessage("密码更新成功!","/adminProfile/edit");
	}
	
	// ajax验证当前密码是否正确
	public void checkCurrentPassword() {
		currentPassword = getPara("currentPassword","");
		Admin admin = Admin.dao.findById(currentId);
		if (StringUtils.equals(DigestUtils.md5Hex(currentPassword),admin.getStr("password"))) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
}
