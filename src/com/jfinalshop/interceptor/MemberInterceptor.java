package com.jfinalshop.interceptor;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Footer;
import com.jfinalshop.model.FriendLink;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Navigation;
import com.jfinalshop.util.SystemConfigUtil;

public class MemberInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation ai) {
		Controller c = ai.getController();
		c.setAttr("systemConfig", SystemConfigUtil.getSystemConfig());
		c.setAttr("base", c.getRequest().getContextPath());
		c.setAttr("topNavigationList", Navigation.dao.getTopNavigationList());
		c.setAttr("middleNavigationList", Navigation.dao.getMiddleNavigationList());
		c.setAttr("bottomNavigationList", Navigation.dao.getBottomNavigationList());
		
		c.setAttr("pictureFriendLinkList", FriendLink.dao.getPictureFriendLinkList());
		c.setAttr("textFriendLinkList", FriendLink.dao.getTextFriendLinkList());
		c.setAttr("footer", Footer.dao.getAll());
		
		String loginMemberId = c.getSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME);
		if (StrKit.notBlank(loginMemberId)) {
			c.setAttr("loginMember", Member.dao.findById(loginMemberId));
		}
		if (loginMemberId == null) {
			Cookie cookie = new Cookie(Member.LOGIN_MEMBER_USERNAME_COOKIE_NAME, null);
			cookie.setPath(c.getRequest().getContextPath());
			cookie.setMaxAge(0);
			c.getResponse().addCookie(cookie);
			String redirectionUrl = c.getRequest().getRequestURL().toString();
			String queryString = c.getRequest().getQueryString();
			if (StringUtils.isNotEmpty(queryString)) {
				redirectionUrl += "?" + queryString;
			}
			c.getRequest().getSession().setAttribute(Member.LOGIN_REDIRECTION_URL_SESSION_NAME, redirectionUrl);
			c.redirect("/html/login.html");
			return;
		} else {
			ai.invoke();
		}
		
	}

}
