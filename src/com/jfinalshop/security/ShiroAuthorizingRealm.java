package com.jfinalshop.security;



import java.util.List;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jfinalshop.model.Admin;

/**
 * shiro的认证授权域
 * @author LiHongYuan
 *
 */
public class ShiroAuthorizingRealm extends AuthorizingRealm{

	private static Logger log = LoggerFactory.getLogger(ShiroAuthorizingRealm.class);
	/**
	 * 构造函数，设置安全的初始化信息
	 */
	public ShiroAuthorizingRealm() {
		super();
	}
	
	/**
	 * 获取当前认证实体的授权信息（授权包括：角色、权限）
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		//获取当前登录的用户名
		ShiroPrincipal subject = (ShiroPrincipal)super.getAvailablePrincipal(principals);
		String adminName = subject.getAdminName();
		String adminId = subject.getId();
		try {
			if(!subject.isAuthorized()) {
				//根据用户名称，获取该用户所有的权限列表
				List<String> authorities = Admin.dao.getAuthoritiesName(adminId);
				List<String> rolelist = Admin.dao.getRolesName(adminId);
				subject.setAuthorities(authorities);
				subject.setRoles(rolelist);
				subject.setAuthorized(true);
				log.info("用户【" + adminName + "】授权初始化成功......");
				log.info("用户【" + adminName + "】 角色列表为：" + subject.getRoles());
				log.info("用户【" + adminName + "】 权限列表为：" + subject.getAuthorities());
			}
		} catch(RuntimeException e) {
			throw new AuthorizationException("用户【" + adminName + "】授权失败");
		}
		//给当前用户设置权限
		info.addStringPermissions(subject.getAuthorities());
		info.addRoles(subject.getRoles());
		return info;
	}

	/**
	 * 根据认证方式（如表单）获取用户名称、密码
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		String username = upToken.getUsername();
		
		if (username == null) {
			log.warn("用户名不能为空");
			throw new AccountException("用户名不能为空");
		}

		Admin admin = null;
		try {
			admin = Admin.dao.getAdminByUsername(username);
		} catch(Exception ex) {
			log.warn("获取用户失败\n" + ex.getMessage());
		}
		if (admin == null) {
		    log.warn("用户不存在");
		    throw new UnknownAccountException("用户不存在!");
		}
		if(!admin.getBoolean("isAccountEnabled")) {
		    log.warn("用户被禁止使用");
		    throw new UnknownAccountException("用户被禁止使用!");
		}
		if(admin.getBoolean("isAccountLocked")){
			log.warn("用户被锁定！");
			throw new LockedAccountException("用户被用户被锁定!");
		}
		ShiroPrincipal subject = new ShiroPrincipal(admin);
		List<String> authorities = Admin.dao.getAuthoritiesName(admin.getStr("id"));
		List<String> rolelist = Admin.dao.getRolesName(admin.getStr("id"));
		subject.setAuthorities(authorities);
		subject.setRoles(rolelist);
		subject.setAuthorized(true);
		return new SimpleAuthenticationInfo(subject, admin.getStr("password"),getName());
	}

}
