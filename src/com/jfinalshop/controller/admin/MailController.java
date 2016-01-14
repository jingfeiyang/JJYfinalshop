package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.interceptor.AdminInterceptor;
import com.jfinalshop.service.MailService;
import com.jfinalshop.validator.admin.MailValidator;

/**
 * 后台类 - 邮箱
 * 
 */
@Before(AdminInterceptor.class)
public class MailController extends Controller{
	
	private String smtpFromMail;
	private String smtpHost;
	private Integer smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private String smtpToMail;

	private MailService mailService;
	
	// 发送SMTP测试邮件
	@Before(MailValidator.class)
	public void ajaxSendSmtpTest() {
		setSmtpFromMail(getPara("smtpFromMail",""));
		setSmtpHost(getPara("smtpHost",""));
		setSmtpPort(getParaToInt("smtpPort",0));
		setSmtpUsername(getPara("smtpUsername",""));
		setSmtpPassword(getPara("smtpPassword",""));
		setSmtpToMail(getPara("smtpToMail",""));
		try {
			mailService = new MailService();
			mailService.sendSmtpTestMail(smtpFromMail, smtpHost, smtpPort, smtpUsername, smtpPassword, smtpToMail);
		} catch (Exception e) {
			ajaxJsonErrorMessage("邮件发送失败！");
		}
	}
	
	public String getSmtpFromMail() {
		return smtpFromMail;
	}

	public void setSmtpFromMail(String smtpFromMail) {
		this.smtpFromMail = smtpFromMail;
	}

	public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public Integer getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpUsername() {
		return smtpUsername;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public String getSmtpPassword() {
		return smtpPassword;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	public String getSmtpToMail() {
		return smtpToMail;
	}

	public void setSmtpToMail(String smtpToMail) {
		this.smtpToMail = smtpToMail;
	}

	// 输出JSON错误消息，返回null
	public void ajaxJsonErrorMessage(String message) {
		setAttr("status", "error");
		setAttr("message", message);
		renderJson();
	}
}
