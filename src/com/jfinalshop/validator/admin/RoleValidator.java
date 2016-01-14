package com.jfinalshop.validator.admin;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;
import com.jfinalshop.model.Role;

public class RoleValidator extends Validator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("role.name", "nameMessages", "角色名称不允许为空!");
		validateRequiredString("role.value", "valueMessages", "角色标识不允许为空!");		
		validateString("role.value", 6, 20, "valueMessages", "角色标识长度不能小于【6】!");		
		validateRegex("role.value", "^ROLE_.*", "valueMessages", "角色标识必须以ROLE_开头!");
		
		// 检查角色名是否存在
		String name = c.getPara("role.name","");	
		if (!Role.dao.checkName(name)) {
			addError("nameMessages","角色名称已存在!");
		}
		
		// 检查角色值是否存在
		String value = c.getPara("role.value","");
		if (!Role.dao.checkValue(value)) {
			addError("valueMessages","角色值称已存在!");
		}
		
	}

	@Override
	protected void handleError(Controller c) {
		c.keepModel(Role.class);
		c.setAttr("allResource", Role.dao.getAllResource());
		c.render("/admin/role_input.html");
	}

}
