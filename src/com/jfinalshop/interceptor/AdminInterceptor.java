package com.jfinalshop.interceptor;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.ext.plugin.shiro.ShiroMethod;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Log;
import com.jfinalshop.model.LogConfig;
import com.jfinalshop.security.ShiroUtils;
import com.jfinalshop.util.SystemConfigUtil;

public class AdminInterceptor implements Interceptor {
	public void intercept(Invocation ai) {
		Controller controller = ai.getController();
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		controller.setAttr("systemConfig", systemConfig);
		controller.setAttr("base", controller.getRequest().getContextPath());
		
		if(ShiroMethod.notAuthenticated()){
			controller.redirect("/admin/login");
		}else{
			String actionClassName = ai.getController().getClass().getName();
			String actionMethodName = ai.getMethodName();
			// 检查是否存在日志监控的方法
			List<LogConfig> allLogConfig = LogConfig.dao.getAll();
			if (allLogConfig != null) {
				for (LogConfig logConfig : allLogConfig) {
					if (StringUtils.equals(logConfig.getActionClassName(), actionClassName) && StringUtils.equals(logConfig.getActionMethodName(), actionMethodName)) {
						String logInfo = ai.getActionKey();
						String operator = ShiroUtils.getLoginAdminName();
						if(operator == null) {
							operator = "未知用户";
						}
						String ip = ai.getController().getRequest().getRemoteAddr();
						String operationName = logConfig.getOperationName();
						Log log = new Log();
						log.setOperationName(operationName);
						log.setActionClassName(actionClassName);
						log.setActionMethodName(actionMethodName);
						log.setOperator(operator);
						log.setIp(ip);
						log.setInfo(logInfo);
						log.save(log);
						break;
					}
				}
			}
			ai.invoke();
        }
	}

}
