package com.jfinalshop.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.bean.MailConfig;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.util.TemplateConfigUtil;
import com.jfinalshop.validator.admin.TemplateHtmlValidator;


/**
 * 后台类 - 邮件模板
 * 
 */
@Before(AdminInterceptor.class)
public class TemplateMailController extends Controller {

	private MailConfig mailConfig;
	private String templateFileContent;

	// 列表
	public void list() {
		setAttr("mailConfigList", getMailConfigList());
		render("/admin/template_mail_list.html");
	}
	
	// 编辑
	public void edit() {
		String mailConfigName = getPara("mailConfig.name","");
		mailConfig = TemplateConfigUtil.getMailConfig(mailConfigName);
		templateFileContent = TemplateConfigUtil.readTemplateFileContent(mailConfig);
		setAttr("mailConfig", mailConfig);
		setAttr("templateFileContent", templateFileContent);
		render("/admin/template_mail_input.html");
	}
	
	// 更新
	@Before(TemplateHtmlValidator.class)
	public void update() {
		String mailConfigName = getPara("mailConfig.name","");
		templateFileContent = getPara("templateFileContent","");
		mailConfig = TemplateConfigUtil.getMailConfig(mailConfigName);
		TemplateConfigUtil.writeTemplateFileContent(mailConfig, templateFileContent);
		redirect("/templateDynamic/list");
	}
				
	// 获取邮件模板配置集合
	public List<MailConfig> getMailConfigList() {
		return TemplateConfigUtil.getMailConfigList();
	}
}
