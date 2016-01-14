package com.jfinalshop.controller.admin;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.bean.HtmlConfig;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.util.TemplateConfigUtil;
import com.jfinalshop.validator.admin.TemplateHtmlValidator;


/**
 * 后台类 - 静态模板
 * 
 */
@Before(AdminInterceptor.class)
public class TemplateHtmlController extends Controller {

	private HtmlConfig htmlConfig;
	private String templateFileContent;

	// 列表
	public void list() {
		setAttr("htmlConfigList", getHtmlConfigList());
		render("/admin/template_html_list.html");
	}
	
	// 编辑
	public void edit() {
		String htmlConfig_name = getPara("htmlConfig.name","");
		htmlConfig = TemplateConfigUtil.getHtmlConfig(htmlConfig_name);
		templateFileContent = TemplateConfigUtil.readTemplateFileContent(htmlConfig);
		setAttr("htmlConfig", htmlConfig);
		setAttr("templateFileContent", templateFileContent);
		render("/admin/template_html_input.html");
	}

	// 更新	
	@Before(TemplateHtmlValidator.class)
	public void update() {
		String htmlConfigName = getPara("htmlConfig.name","");
		templateFileContent = getPara("templateFileContent","");
		htmlConfig = TemplateConfigUtil.getHtmlConfig(htmlConfigName);
		TemplateConfigUtil.writeTemplateFileContent(htmlConfig, templateFileContent);
		redirect("/templateHtml/list");
	}
	
	// 获取生成静态模板配置集合
	public List<HtmlConfig> getHtmlConfigList() {
		return TemplateConfigUtil.getHtmlConfigList();
	}
}
