package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.bean.DynamicConfig;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.util.TemplateConfigUtil;
import com.jfinalshop.validator.admin.TemplateDynamicValidator;

/**
 * 后台类 - 动态模板
 * 
 */
@Before(AdminInterceptor.class)
public class TemplateDynamicController extends Controller {

	private DynamicConfig dynamicConfig;
	private String templateFileContent;

	// 列表
	public void list() {
		setAttr("dynamicConfigList", TemplateConfigUtil.getDynamicConfigList());
		render("/admin/template_dynamic_list.html");
	}

	// 编辑
	public void edit() {
		String dynamicConfigName = getPara("dynamicConfig.name","");
		dynamicConfig = TemplateConfigUtil.getDynamicConfig(dynamicConfigName);
		templateFileContent = TemplateConfigUtil.readTemplateFileContent(dynamicConfig);
		setAttr("dynamicConfig", dynamicConfig);
		setAttr("templateFileContent", templateFileContent);
		render("/admin/template_dynamic_input.html");
	}
	
	// 更新
	@Before(TemplateDynamicValidator.class)
	public void update() {
		String dynamicConfig_name = getPara("dynamicConfig.name","");
		templateFileContent = getPara("templateFileContent","");
		dynamicConfig = TemplateConfigUtil.getDynamicConfig(dynamicConfig_name);
		TemplateConfigUtil.writeTemplateFileContent(dynamicConfig, templateFileContent);
		redirect("/templateDynamic/list");
	}
}
