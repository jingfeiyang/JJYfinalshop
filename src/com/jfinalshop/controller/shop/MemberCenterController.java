package com.jfinalshop.controller.shop;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;

import com.jfinalshop.model.Member;
import com.jfinalshop.model.Message;
import com.jfinalshop.util.SystemConfigUtil;

/**
 * 前台类 - 会员中心
 * 
 */
public class MemberCenterController extends BaseShopController<Member>{
	
	public void index(){		
		Member member = getLoginMember();
		setAttr("loginMember", member);
		setAttr("unreadMessageCount", Message.dao.getUnreadMessageCount(member));
		setAttr("systemConfig", SystemConfigUtil.getSystemConfig());
		setAttr("base", getRequest().getContextPath());
		render("/shop/member_center_index.html");
	}

	// 获取当前登录会员，若未登录则返回null
	public Member getLoginMember() {
		String id = getSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME);
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		Member loginMember = Member.dao.findById(id);
		return loginMember;
	}
	
	// 会员注销
	public void logout() {
		getRequest().getSession().removeAttribute(Member.LOGIN_MEMBER_ID_SESSION_NAME);
		Cookie cookie = new Cookie(Member.LOGIN_MEMBER_USERNAME_COOKIE_NAME, null);
		cookie.setPath(getRequest().getContextPath() + "/");
		cookie.setMaxAge(0);
		getResponse().addCookie(cookie);
		redirect("/");
	}
}
