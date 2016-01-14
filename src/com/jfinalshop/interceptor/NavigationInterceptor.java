package com.jfinalshop.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Footer;
import com.jfinalshop.model.FriendLink;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Navigation;
import com.jfinalshop.util.SystemConfigUtil;

public class NavigationInterceptor implements Interceptor{

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
		
		String id = c.getSessionAttr(Member.LOGIN_MEMBER_ID_SESSION_NAME);
		if (StrKit.notBlank(id)) {
			c.setAttr("loginMember", Member.dao.findById(id));
		}
		ai.invoke();
	}

}
