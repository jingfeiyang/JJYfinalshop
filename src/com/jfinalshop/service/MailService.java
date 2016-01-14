package com.jfinalshop.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.MimeUtility;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.WebAppResourceLoader;

import com.jfinal.core.JFinal;
import com.jfinalshop.bean.MailConfig;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Member;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.util.TemplateConfigUtil;

/**
 * Service实现类 - 邮件服务
 * 
 */
public class MailService {

	private HtmlEmail email;
	public static final MailService service = new MailService();
	
	public boolean isMailConfigComplete() {
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		if (StringUtils.isEmpty(systemConfig.getSmtpFromMail()) || StringUtils.isEmpty(systemConfig.getSmtpHost()) || systemConfig.getSmtpPort() == null || StringUtils.isEmpty(systemConfig.getSmtpUsername()) || StringUtils.isEmpty(systemConfig.getSmtpPassword())) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean sendMail(String subject, String templateFilePath, Map<String, Object> data, String toMail) {
		boolean isSend = false;
		try {
			SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
			
			email = new HtmlEmail ();
			email.setHostName(systemConfig.getSmtpHost());
			email.setSmtpPort(systemConfig.getSmtpPort());
			email.setAuthenticator(new DefaultAuthenticator(systemConfig.getSmtpUsername(), systemConfig.getSmtpPassword()));
			email.setSSLOnConnect(true);
			
			WebAppResourceLoader resourceLoader = new WebAppResourceLoader();
			Configuration cfg = Configuration.defaultConfiguration();
			GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
			Template template = gt.getTemplate(templateFilePath);
			template.binding(data);
			String text = template.render();
			
			email.setFrom(MimeUtility.encodeWord(systemConfig.getShopName()) + " <" + systemConfig.getSmtpFromMail() + ">");
			email.setSubject(subject);
			email.setMsg(text);
			email.addTo(toMail);
			email.send();
			isSend = true;
		} catch (EmailException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isSend;
	}
	
	// 获取公共数据
	public Map<String, Object> getCommonData() {
		Map<String, Object> commonData = new HashMap<String, Object>();
		ServletContext servletContext = JFinal.me().getServletContext();
		commonData.put("base", servletContext.getContextPath());
		commonData.put("systemConfig", SystemConfigUtil.getSystemConfig());
		return commonData;
	}
	
	public void sendSmtpTestMail(String smtpFromMail, String smtpHost, Integer smtpPort, String smtpUsername, String smtpPassword, String toMail) {
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		MailConfig mailConfig = TemplateConfigUtil.getMailConfig(MailConfig.SMTP_TEST);
		String subject = mailConfig.getSubject();
		String templateFilePath = mailConfig.getTemplateFilePath();
		try {
			email = new HtmlEmail();
			email.setHostName(systemConfig.getSmtpHost());
			email.setSmtpPort(systemConfig.getSmtpPort());
			email.setAuthenticator(new DefaultAuthenticator(systemConfig.getSmtpUsername(), systemConfig.getSmtpPassword()));
			email.setSSLOnConnect(true);
			
			WebAppResourceLoader resourceLoader = new WebAppResourceLoader();
			Configuration cfg = Configuration.defaultConfiguration();
			GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);
			Template template = gt.getTemplate(templateFilePath);
			template.binding("systemConfig", systemConfig);
			String text = template.render();
			
			email.setFrom(MimeUtility.encodeWord(systemConfig.getShopName()) + " <" + systemConfig.getSmtpFromMail() + ">");
			email.setSubject(subject);
			email.setMsg(text);
			email.addTo(toMail);
			email.send();
			
		} catch (EmailException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public boolean sendPasswordRecoverMail(Member member) {
		Map<String, Object> data = getCommonData();
		data.put("member", member);
		MailConfig mailConfig = TemplateConfigUtil.getMailConfig(MailConfig.PASSWORD_RECOVER);
		String subject = mailConfig.getSubject();
		String templateFilePath = mailConfig.getTemplateFilePath();
		return sendMail(subject, templateFilePath, data, member.getStr("email"));
	}

}