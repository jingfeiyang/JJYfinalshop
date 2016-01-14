package com.jfinalshop.controller.admin;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinalshop.model.LogConfig;
import com.jfinalshop.util.PackageUtil;
import com.jfinalshop.validator.admin.LogConfigValidator;



/**
 * 后台Action类 - 日志设置
 * 
 */
public class LogConfigController extends BaseAdminController<LogConfig>{

	private LogConfig logConfig;
	private List<String> allActionClassName;
	
	// ajax验证操作名称是否已存在
	public void checkOperationName() {
		String value = getPara("logConfig.operationName");
		if (isUnique("logconfig","operationName", value)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
		
	// ajax根据Action类名称获取所有方法名称(不包含已使用的方法)
	public void getAllActionMethod() throws ClassNotFoundException {
		String actionClassName = getPara("logConfig.actionClassName","");
		List<String> allActionClassName = PackageUtil.getAllActionClassName();
		if (allActionClassName.contains(actionClassName)) {
			Class<?> actionClass = Class.forName(actionClassName);
			Method[] methods = actionClass.getDeclaredMethods();
			StringBuilder stringBuilder = new StringBuilder();
			List<LogConfig> logConfigs = LogConfig.dao.getLogConfigList(actionClassName);
			String[] methodNameArray = new String[logConfigs.size()];
			for (int i = 0; i < logConfigs.size(); i++) {
				methodNameArray[i] = logConfigs.get(i).getActionMethodName();
			}
			for (Method method : methods) {
				if (!ArrayUtils.contains(methodNameArray, method.getName())) {
					stringBuilder.append("<option value=\"" + method.getName() + "\">" + method.getName() + "</option>");
				}
			}
			if (stringBuilder.length() == 0) {
				stringBuilder.append("<option value=\"noValue\">无可用方法</option>");
			}
			renderText(stringBuilder.toString());
		}
	}

	// 列表
	public void list() {
		findByPage();
		render("/admin/log_config_list.html");
	}

	// 删除
	public void delete() {
		ids = getParaValues("ids");
		if (ids.length > 0){
			for (String id : ids) {
				if(LogConfig.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}

	// 添加
	public void add() {
		setAttr("allActionClassName", getAllActionClassName());
		render("/admin/log_config_input.html");
	}

	// 编辑
	public void edit() {
		id = getPara("id","");
		logConfig = LogConfig.dao.findById(id);
		setAttr("logConfig", logConfig);
		setAttr("allActionClassName", getAllActionClassName());
		render("/admin/log_config_input.html");
	}
	
	// 保存
	@Before(LogConfigValidator.class)
	public void save() throws ClassNotFoundException {
		logConfig = getModel(LogConfig.class);
		String actionClassName = logConfig.getActionClassName();
		String actionMethodName = logConfig.getActionMethodName();
		
		if (!PackageUtil.getAllActionClassName().contains(actionClassName)) {
			addActionError("Action类错误!");
			return;
		}
		Class<?> actionClass = Class.forName(actionClassName);
		Method[] methods = actionClass.getDeclaredMethods();
		boolean isMethod = false;
		for (Method method : methods) {
			if (StringUtils.equals(method.getName(), actionMethodName)) {
				isMethod = true;
				break;
			}
		}
		if (isMethod == false) {
			addActionError("Action类错误!");
			return;
		}
		saved(logConfig);
		redirect("/logConfig/list");
	}
	
	// 更新
	@Before(LogConfigValidator.class)
	public void update() throws ClassNotFoundException {
		logConfig = getModel(LogConfig.class);
		String actionClassName = logConfig.getActionClassName();
		String actionMethodName = logConfig.getActionMethodName();
		if (!PackageUtil.getAllActionClassName().contains(actionClassName)) {
			addActionError("Action类错误!");
			return;
		}
		Class<?> actionClass = Class.forName(actionClassName);
		Method[] methods = actionClass.getDeclaredMethods();
		boolean isMethod = false;
		for (Method method : methods) {
			if (StringUtils.equals(method.getName(), actionMethodName)) {
				isMethod = true;
				break;
			}
		}
		if (isMethod == false) {
			addActionError("Action类错误!");
			return;
		}
		updated(logConfig);
		redirect("/logConfig/list");
	}
		
	public List<String> getAllActionClassName() {
		allActionClassName = PackageUtil.getAllActionClassName();
		return allActionClassName;
	}
}
