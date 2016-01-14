package com.jfinalshop.controller.shop;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import cn.dreampie.shiro.core.SubjectKit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.bean.CartItemCookie;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.interceptor.NavigationInterceptor;
import com.jfinalshop.model.Agreement;
import com.jfinalshop.model.CartItem;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberRank;
import com.jfinalshop.model.Product;
import com.jfinalshop.service.MailService;
import com.jfinalshop.util.CommonUtil;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.validator.shop.MemberValidator;

/**
 * 前台类 - 会员
 * 
 */

@ControllerBind(controllerKey = "/shop/member")
@Before(NavigationInterceptor.class)
public class MemberController extends Controller{

	//private static Logger log = LoggerFactory.getLogger(MemberController.class);
	
	private Member member;
	private Boolean isAgreeAgreement;
	private String passwordRecoverKey;
	public static final String STATUS = "status";
	public static final String WARN = "warn";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String MESSAGE = "message";
	
	// 登录验证
	
	@Before({MemberValidator.class,Tx.class})	
	public void login() {		
		String username = getPara("member.username","");
		String password = getPara("member.password","");
		String captchaToken = getPara("captchaToken","");
		
		if (!SubjectKit.doCaptcha("captcha", captchaToken)) {
			addActionError("验证码错误!!!");
			return;
		}
		
		SystemConfig systemConfig = getSystemConfig();
		Member loginMember = Member.dao.getMemberByUsername(username);
		if (loginMember != null) {
			// 解除会员账户锁定
			if (loginMember.getBoolean("isAccountLocked")) {
				if (systemConfig.getIsLoginFailureLock()) {
					int loginFailureLockTime = systemConfig.getLoginFailureLockTime();
					if (loginFailureLockTime != 0) {
						Date lockedDate = loginMember.getDate("lockedDate");
						Date nonLockedTime = DateUtils.addMinutes(lockedDate, loginFailureLockTime);
						Date now = new Date();
						if (now.after(nonLockedTime)) {
							loginMember.set("loginFailureCount",0);
							loginMember.set("isAccountLocked",false);
							loginMember.set("lockedDate",null);
							loginMember.update();
						}
					}					
				}else {
					loginMember.set("loginFailureCount",0);
					loginMember.set("isAccountLocked",false);
					loginMember.set("lockedDate",null);
					loginMember.update();
				}				
			}			
			if (!loginMember.getBoolean("isAccountEnabled")) {
				addActionError("您的账号已被禁用,无法登录!");
				return;
			}
			if (loginMember.getBoolean("isAccountLocked")) {
				addActionError("您的账号已被锁定,无法登录!");
				return;
			}
			if (!Member.dao.verifyMember(username, password)) {
				if (systemConfig.getIsLoginFailureLock()) {
					int loginFailureLockCount = getSystemConfig().getLoginFailureLockCount();
					int loginFailureCount = loginMember.getInt("loginFailureCount") + 1;
					if (loginFailureCount >= systemConfig.getLoginFailureLockCount()) {
						loginMember.set("isAccountLocked",true);
						loginMember.set("lockedDate",new Date());
					}
					loginMember.set("loginFailureCount",loginFailureCount);
					loginMember.update();
					if (getSystemConfig().getIsLoginFailureLock() && loginFailureLockCount - loginFailureCount <= 3) {
						addActionError("若连续" + loginFailureLockCount + "次密码输入错误,账号将被锁定,还有" + (loginFailureLockCount - loginMember.getInt("loginFailureCount")) + " 次机会。");
						return;
					} else {
						addActionError("您的用户名或密码错误!");
						return;
					}
				} else {
					addActionError("用户名或密码错误!");
					return;
				}
			}			
		}else {
			addActionError("用户名或密码错误!");
			return;
		}
		
		loginMember.set("loginIp", getRequest().getRemoteAddr());
		loginMember.set("loginDate", new Date());
		loginMember.update();
		
		// 写入会员登录Session
		setSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME, loginMember.getStr("id"));
		
		// 写入会员登录Cookie
		Cookie loginMemberUsernameCookie = null;
		try {
			loginMemberUsernameCookie = new Cookie(Member.LOGIN_MEMBER_USERNAME_COOKIE_NAME, URLEncoder.encode(username.toLowerCase(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		loginMemberUsernameCookie.setPath(getRequest().getContextPath() + "/");
		getResponse().addCookie(loginMemberUsernameCookie);
		
		// 合并购物车
		Cookie[] cookies = getRequest().getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
					if (StringUtils.isNotEmpty(cookie.getValue())) {
						JSONArray jsonArray = JSON.parseArray(cookie.getValue());
						List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
						for (CartItemCookie cartItemCookie : cartItemCookieList) {
							Product product = Product.dao.findById(cartItemCookie.getI());
							if (product != null) {
								CartItem cartItem = new CartItem();
								cartItem.set("member_id",loginMember.getStr("id"));
								cartItem.set("product_id",product.getStr("id"));
								cartItem.set("quantity",cartItemCookie.getQ());
								cartItem.save(cartItem);
							}
						}
					}
				}
			}
		}
		
		// 清空临时购物车Cookie
		Cookie cartItemCookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, null);
		cartItemCookie.setPath(getRequest().getContextPath() + "/");
		cartItemCookie.setMaxAge(0);
		getResponse().addCookie(cartItemCookie);
		
		String redirectionUrl = getSessionAttr(Member.LOGIN_REDIRECTION_URL_SESSION_NAME);
		getRequest().getSession().removeAttribute(Member.LOGIN_REDIRECTION_URL_SESSION_NAME);
		if (StringUtils.isEmpty(redirectionUrl)) {
			redirect("/memberCenter");
		} else {
			redirect(redirectionUrl);
		}
	}
	
	// Ajax会员登录验证
	@Before({MemberValidator.class,Tx.class})
	public void ajaxLogin() {
		String username = getPara("member.username","");
		String password = getPara("member.password","");
		String captchaToken = getPara("captchaToken","");
		
		if (!SubjectKit.doCaptcha("captcha", captchaToken)) {
			addActionError("验证码错误!");
			return;
		}
		
		SystemConfig systemConfig = getSystemConfig();
		Member loginMember = Member.dao.getMemberByUsername(username);
		if (loginMember != null) {
			// 解除会员账户锁定
			if (loginMember.getBoolean("isAccountLocked")) {
				if (systemConfig.getIsLoginFailureLock()) {
					int loginFailureLockTime = systemConfig.getLoginFailureLockTime();
					if (loginFailureLockTime != 0) {
						Date lockedDate = loginMember.getDate("lockedDate");
						Date nonLockedTime = DateUtils.addMinutes(lockedDate, loginFailureLockTime);
						Date now = new Date();
						if (now.after(nonLockedTime)) {
							loginMember.set("loginFailureCount", 0);
							loginMember.set("isAccountLocked",false);
							loginMember.set("lockedDate",null);
							loginMember.update();
						}
					}
				} else {
					loginMember.set("loginFailureCount", 0);
					loginMember.set("isAccountLocked",false);
					loginMember.set("lockedDate",null);
					loginMember.update();
				}
			}
			if (!loginMember.getBoolean("isAccountEnabled")) {
				ajaxJsonErrorMessage("您的账号已被禁用,无法登录！");
				return;
			}
			if (loginMember.getBoolean("isAccountLocked")) {
				ajaxJsonErrorMessage("您的账号已被锁定,无法登录！");
				return;
			}
			if (!Member.dao.verifyMember(username, password)) {
				if (systemConfig.getIsLoginFailureLock()) {
					int loginFailureLockCount = getSystemConfig().getLoginFailureLockCount();
					int loginFailureCount = loginMember.getInt("loginFailureCount") + 1;
					if (loginFailureCount >= systemConfig.getLoginFailureLockCount()) {
						loginMember.set("isAccountLocked",true);
						loginMember.set("lockedDate",new Date());
					}
					loginMember.set("loginFailureCount",loginFailureCount);
					loginMember.update();
					if (loginFailureLockCount - loginFailureCount <= 3) {
						ajaxJsonErrorMessage("若连续" + loginFailureLockCount + "次密码输入错误,账号将被锁定,还有" + (loginFailureLockCount - loginMember.getInt("loginFailureCount")) + " 次机会。");
					} else {
						ajaxJsonErrorMessage("您的用户名或密码错误！");
						return;
					}
				} else {
					ajaxJsonErrorMessage("您的用户名或密码错误！");
					return;
				}
			}
		} else {
			ajaxJsonErrorMessage("您的用户名或密码错误！");
			return;
		}
		loginMember.set("loginIp",getRequest().getRemoteAddr());
		loginMember.set("loginDate",new Date());
		loginMember.update();
		
		// 写入会员登录Session
		setSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME, loginMember.getStr("id"));
		
		// 写入会员登录Cookie
		Cookie loginMemberUsernameCookie = null;
		try {
			loginMemberUsernameCookie = new Cookie(Member.LOGIN_MEMBER_USERNAME_COOKIE_NAME, URLEncoder.encode(username.toLowerCase(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		loginMemberUsernameCookie.setPath(getRequest().getContextPath() + "/");
		getResponse().addCookie(loginMemberUsernameCookie);

		// 合并购物车
		Cookie[] cookies = getRequest().getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
					if (StringUtils.isNotEmpty(cookie.getValue())) {
						JSONArray jsonArray = JSON.parseArray(cookie.getValue());
						List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
						for (CartItemCookie cartItemCookie : cartItemCookieList) {
							Product product = Product.dao.findById(cartItemCookie.getI());
							if (product != null) {
								CartItem cartItem = new CartItem();
								cartItem.set("member_id",loginMember.getStr("id"));
								cartItem.set("product_id",product.getStr("id"));
								cartItem.set("quantity",cartItemCookie.getQ());
								cartItem.save(cartItem);
							}
						}
					}
				}
			}
		}
		
		// 清空临时购物车Cookie
		Cookie cartItemCookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, null);
		cartItemCookie.setPath(getRequest().getContextPath() + "/");
		cartItemCookie.setMaxAge(0);
		getResponse().addCookie(cartItemCookie);
		
		ajaxJsonSuccessMessage("会员登录成功！");
	}
		

	// 获取注册协议内容
	public void agreement() {
		renderJson(Agreement.dao.getAll().getStr("content"));
	}	
		
	// Ajax会员注册保存
	@Before({MemberValidator.class,Tx.class})
	public void ajaxRegister() {
		isAgreeAgreement = getParaToBoolean("isAgreeAgreement");
		member = getModel(Member.class);
		String captchaToken = getPara("captchaToken","");
		String rePassword = getPara("rePassword","");
		
		if (isAgreeAgreement == null || isAgreeAgreement == false) {
			ajaxJsonErrorMessage("必须同意注册协议才可进行注册操作!");
			return;
		}
		if (!getSystemConfig().getIsRegister()) {
			ajaxJsonErrorMessage("本站注册功能现已关闭!");
			return;
		}
		if (!StringUtils.equalsIgnoreCase(member.getStr("password"), rePassword)) {
			ajaxJsonErrorMessage("两次密码输入不一致!");
			return;
		}
		
		if (!SubjectKit.doCaptcha("captcha", captchaToken)) {
			ajaxJsonErrorMessage("验证码错误!");
			return;
		}
		
		member.set("username", member.getStr("username").toLowerCase());
		member.set("password", DigestUtils.md5Hex(member.getStr("password")));
		member.set("safeQuestion", null);
		member.set("safeAnswer", null);
		member.set("memberRank_id", MemberRank.dao.getDefaultMemberRank().getStr("id"));
		member.set("point", 0);
		member.set("deposit", new BigDecimal("0"));
		member.set("isAccountEnabled", true);
		member.set("isAccountLocked", false);
		member.set("loginFailureCount", 0);
		member.set("passwordRecoverKey", null);
		member.set("lockedDate", null);
		member.set("loginDate", new Date());
		member.set("registerIp",getRequest().getRemoteAddr());
		member.set("loginIp",getRequest().getRemoteAddr());
		member.set("id", CommonUtil.getUUID());
		member.set("createDate", new Date());
		member.save();
		
		// 写入会员登录Session
		setSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME, member.getStr("id"));
		
		// 写入会员登录Cookie
		Cookie loginMemberCookie = null;
		try {
			loginMemberCookie = new Cookie(Member.LOGIN_MEMBER_USERNAME_COOKIE_NAME, URLEncoder.encode(member.getStr("username").toLowerCase(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		loginMemberCookie.setPath(getRequest().getContextPath() + "/");
		getResponse().addCookie(loginMemberCookie);
		
		// 合并购物车
		Cookie[] cookies = getRequest().getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (StringUtils.equalsIgnoreCase(cookie.getName(), CartItemCookie.CART_ITEM_LIST_COOKIE_NAME)) {
					if (StringUtils.isNotEmpty(cookie.getValue())) {
						JSONArray jsonArray = JSON.parseArray(cookie.getValue());
						List<CartItemCookie> cartItemCookieList = JSON.parseArray(jsonArray.toJSONString(), CartItemCookie.class);
						for (CartItemCookie cartItemCookie : cartItemCookieList) {
							Product product = Product.dao.findById(cartItemCookie.getI());
							if (product != null) {
								CartItem cartItem = new CartItem();
								cartItem.set("member_id",member.getStr("id"));
								cartItem.set("product_id",product.getStr("id"));
								cartItem.set("quantity",cartItemCookie.getQ());
								cartItem.save(cartItem);
							}
						}
					}
				}
			}
		}
		
		// 清空临时购物车Cookie
		Cookie cartItemCookie = new Cookie(CartItemCookie.CART_ITEM_LIST_COOKIE_NAME, null);
		cartItemCookie.setPath(getRequest().getContextPath() + "/");
		cartItemCookie.setMaxAge(0);
		getResponse().addCookie(cartItemCookie);		
		ajaxJsonSuccessMessage("会员注册成功！");
	}
	
	// 密码找回
	public void passwordRecover() {
		render("/shop/member_password_recover.html");
	}
	
	// 发送密码找回邮件
	@Before(MemberValidator.class)
	public void sendPasswordRecoverMail() {
		member = getModel(Member.class);
		Member persistent = Member.dao.getMemberByUsername(member.getStr("username"));
		if (persistent == null || StringUtils.equalsIgnoreCase(persistent.getStr("email"), member.getStr("email")) == false) {
			ajaxJsonErrorMessage("用户名或E-mail输入错误！");
			return;
		} 		
		if (!MailService.service.isMailConfigComplete()) {
			ajaxJsonErrorMessage("系统邮件发送功能尚未配置，请联系管理员！");
			return;
		} 		
		if (StringUtils.isNotEmpty(persistent.getStr("safeQuestion")) && StringUtils.isNotEmpty(persistent.getStr("safeQuestion"))) {
			if (StringUtils.isEmpty(member.getStr("safeAnswer"))) {
				Map<String, String> jsonMap = new HashMap<String, String>();
				jsonMap.put("safeQuestion", persistent.getStr("safeQuestion"));
				jsonMap.put(STATUS, WARN);
				jsonMap.put(MESSAGE, "请填写密码保护问题答案！");
				renderJson(jsonMap);
				return;
			} 
			if (StringUtils.equalsIgnoreCase(persistent.getStr("safeAnswer"), member.getStr("safeAnswer")) == false) {
				ajaxJsonErrorMessage("密码保护答案错误！");
				return;
			}
		} 				
		persistent.set("passwordRecoverKey", Member.dao.buildPasswordRecoverKey());
		persistent.set("modifyDate", new Date());
		persistent.update();
		
		if (MailService.service.sendPasswordRecoverMail(persistent)) {
			ajaxJsonSuccessMessage("系统已发送邮件到您的E-mail，请根据邮件提示操作！");
		} else {
			ajaxJsonErrorMessage("发送失败,请联系管理员！");
		}
	}
	
	// 密码修改
	public void passwordModify() {
		String id = getPara("id","");
		passwordRecoverKey = getPara("passwordRecoverKey","");
		
		Member persistent = Member.dao.findById(id);		
		if (persistent == null || StringUtils.equalsIgnoreCase(persistent.getStr("passwordRecoverKey"), passwordRecoverKey) == false) {
			addActionError("对不起，此密码找回链接已失效！");
			return;
		}
		Date passwordRecoverKeyBuildDate = Member.dao.getPasswordRecoverKeyBuildDate(passwordRecoverKey);
		Date passwordRecoverKeyExpiredDate = DateUtils.addMinutes(passwordRecoverKeyBuildDate, Member.PASSWORD_RECOVER_KEY_PERIOD);
		Date now = new Date();
		if (now.after(passwordRecoverKeyExpiredDate)) {
			addActionError("对不起，此密码找回链接已过期！");
			return;
		}
		setAttr("member", persistent);
		render("/shop/member_password_modify.html");
	}
		
	// 密码更新
	@Before(MemberValidator.class)
	public void passwordUpdate() {
		member = getModel(Member.class);
		passwordRecoverKey =member.getStr("passwordRecoverKey");
		
		Member persistent = Member.dao.findById(member.getStr("id"));
		if (persistent == null || StringUtils.equalsIgnoreCase(persistent.getStr("passwordRecoverKey"), passwordRecoverKey) == false) {
			addActionError("对不起，此密码找回链接已失效！");
			return;
		}
		Date passwordRecoverKeyBuildDate = Member.dao.getPasswordRecoverKeyBuildDate(passwordRecoverKey);
		Date passwordRecoverKeyExpiredDate = DateUtils.addMinutes(passwordRecoverKeyBuildDate, Member.PASSWORD_RECOVER_KEY_PERIOD);
		Date now = new Date();
		if (now.after(passwordRecoverKeyExpiredDate)) {
			addActionError("对不起，此密码找回链接已过期！");
			return;
		}		
		persistent.set("password",DigestUtils.md5Hex(member.getStr("password")));
		persistent.set("passwordRecoverKey", null);		
		persistent.set("modifyDate", new Date());
		persistent.update();
		renderSuccessMessage("密码修改成功！", "/html/login.html");
	}

	// 检查用户名是否存在
	public void checkUsername() {
		String username = getPara("member.username","");
		if (Member.dao.isExistByUsername(username)) {
			renderText("false");
		} else {
			renderText("true");
		}
	}
	
	public void addActionError(String error){
		setAttr("errorMessages", error);
		render("/shop/error.html");	
	}
	
	// 获取系统配置信息
	public SystemConfig getSystemConfig() {
		return SystemConfigUtil.getSystemConfig();
	}
	
	// 输出JSON错误消息，返回null
	public void ajaxJsonErrorMessage(String message) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, ERROR);
		jsonMap.put(MESSAGE, message);
		renderJson(jsonMap);
	}
	
	// 输出JSON成功消息，返回null
	public void ajaxJsonSuccessMessage(String message) {
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(MESSAGE, message);
		renderJson(jsonMap);
	}
	
	public void renderSuccessMessage(String message,String url){
		setAttr(MESSAGE, message);
		setAttr("redirectionUrl", url);
		render("/shop/success.html");
	}
}
