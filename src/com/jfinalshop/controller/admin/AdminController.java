package com.jfinalshop.controller.admin;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;

import cn.dreampie.shiro.core.SubjectKit;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.ext.plugin.shiro.ShiroMethod;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.bean.SystemConfig;
import com.jfinalshop.model.Admin;
import com.jfinalshop.model.AdminRole;
import com.jfinalshop.model.Article;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Message;
import com.jfinalshop.model.Orders;
import com.jfinalshop.model.Product;
import com.jfinalshop.model.Role;
import com.jfinalshop.service.MenuService;
import com.jfinalshop.util.SystemConfigUtil;
import com.jfinalshop.validator.admin.AdminValidator;

/**
 * 后台类 - 后台管理、管理员
 * 
 */
public class AdminController extends BaseAdminController<Admin>{

	private Admin admin;
	private MenuService menuService = Duang.duang(MenuService.class);
	
	// 登录页面
	@Clear
	public void login(){
		SystemConfig systemConfig = SystemConfigUtil.getSystemConfig();
		setAttr("systemConfig", systemConfig);
		setAttr("base", getRequest().getContextPath());
		render("/admin/admin_login.html");
	}
		
	
	// 登录验证
	@Clear
	public void singIn() {
		String username = getPara("username","");
		String password = getPara("password","");		
		String captchaToken = getPara("captchaToken");
		boolean rememberMe = getPara("remember","").equals("on") ? true : false;
		
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			renderErrorMessage("账号或密码不能为空!");
			return;
		}
		
		if (!SubjectKit.doCaptcha("captcha", captchaToken)) {
			renderErrorMessage("验证码错误!");
			return;
		}
		
		Admin admin = Admin.dao.getAdminByUsername(username);			
		// 开始验证
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, DigestUtils.md5Hex(password));
		token.setRememberMe(rememberMe);
		
		try {
			subject.login(token);
		} catch (UnknownAccountException ue) {
			token.clear();
			renderErrorMessage("登录失败，您输入的账号不存在!");
			return;
		} catch (IncorrectCredentialsException ie) {
			token.clear();
			SystemConfig systemConfig = getSystemConfig();
			if (systemConfig.getIsLoginFailureLock()) {
				int loginFailureLockCount = getSystemConfig().getLoginFailureLockCount();
				int loginFailureCount = admin.getInt("loginFailureCount") + 1;
				if (loginFailureCount >= systemConfig.getLoginFailureLockCount()) {
					admin.set("isAccountLocked", true);
					admin.set("lockedDate", new Date());
					admin.update();
				}
				admin.set("loginFailureCount", loginFailureCount);
				updated(admin);
				if (loginFailureLockCount - loginFailureCount <= 3) {
					renderErrorMessage("若连续" + loginFailureLockCount + "次密码输入错误,您的账号将被锁定,还有" + (loginFailureLockCount - admin.getInt("loginFailureCount")) + " 次机会。");
					return;
				} else {
					renderErrorMessage("您的用户名或密码错误!");
					return;
				}
			} else {
				renderErrorMessage("您的用户名或密码错误!");
				return;
			}
		} catch(LockedAccountException le){
			token.clear();
			renderErrorMessage("用户被用户被锁定!");
			return;
		} catch (RuntimeException re) {
			re.printStackTrace();
			token.clear();
			renderErrorMessage("登录失败");
			return;
		}
			
		// 登录成功后
		if(ShiroMethod.authenticated()) {
			admin.set("loginDate", new Date());
			admin.set("loginIp", getRequest().getRemoteAddr());
			admin.set("loginFailureCount", 0);
			updated(admin);
			redirect("/admin/center");
		} 
	}
	
	// 添加
	public void add() {
		setAttr("allRole", Role.dao.getAllRole());
		render("/admin/admin_input.html");
	}
	
	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("admin", Admin.dao.findById(id));
		}		
		setAttr("allRole", Role.dao.getAllRole());
		render("/admin/admin_input.html");
	}
	
	// 保存
	@Before({Tx.class,AdminValidator.class})
	public void save(){
		admin = getModel(Admin.class);
		String[] roleIds = getParaValues("roleList.id");
				
		admin.set("username",admin.getStr("username").toLowerCase());
		admin.set("loginFailureCount",0);
		admin.set("isAccountLocked",false);
		admin.set("isAccountExpired",false);
		admin.set("isCredentialsExpired",false);
		admin.set("password",DigestUtils.md5Hex(admin.getStr("password")));
		saved(admin);
		
		if (roleIds.length == 0) {
			renderErrorMessage("请至少选择一个角色!");
			return;
		}
		
		for (String roleId : roleIds) {
			AdminRole adminRole = new AdminRole();
			adminRole.set("adminSet_id", admin.getStr("id"));
			adminRole.set("roleSet_id", roleId);
			adminRole.save();
		}
		redirect("/admin/list");	
	}
	
	// 更新
	@Before({Tx.class,AdminValidator.class})
	public void update(){
		admin = getModel(Admin.class);
		String[] roleIds = getParaValues("roleList.id");
		
		if (StringUtils.isNotEmpty(admin.getStr("password"))) {
			String passwordMd5 = DigestUtils.md5Hex(admin.getStr("password"));
			admin.set("password",passwordMd5);
		}else{
			admin.remove("password");
		}
		updated(admin);
		
		// 先删除再写入
		String adminId = admin.getStr("id");
		if (AdminRole.dao.deleteById(adminId)){
			
			if (roleIds.length == 0){
				renderErrorMessage("请至少选择一个角色!");
				return;
			}			
			for(String roleId : roleIds){		
				AdminRole adminRole = new AdminRole();
				adminRole.set("adminSet_id", adminId);
				adminRole.set("roleSet_id", roleId);
				adminRole.save();			
			}
		}
		redirect("/admin/list");
	}
		
	// 删除
	@Before(Tx.class)
	public void delete(){
		String[] ids = getParaValues("ids");
		if (ids != null && ids.length > 0){
			for (String id : ids) {
				if(AdminRole.dao.deleteById(id) && Admin.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}		
	}
	
	// 管理员退出
	public void logout() {
		Subject currentUser = SecurityUtils.getSubject();
		if (SecurityUtils.getSubject().getSession() != null) {
			currentUser.logout();
		}
		redirect("/admin/login");
	}
	
	// 后台首页
	@RequiresRoles(value={"ROLE_ADMIN","ROLE_SERVICE"},logical=Logical.OR)
	@RequiresPermissions("admin")
	public void index(){	
		setAttr("java_version", System.getProperty("java.version"));
		setAttr("os", System.getProperty("os.name"));
		setAttr("os_arch", System.getProperty("os.arch"));
		setAttr("os_version", System.getProperty("os.version"));
		setAttr("user_dir", System.getProperty("user.dir"));
		setAttr("tmpdir", System.getProperty("java.io.tmpdir"));	
		
		setAttr("unprocessedOrderCount", getUnprocessedOrderCount());// 未处理订单
		setAttr("paidUnshippedOrderCount", getPaidUnshippedOrderCount()); // 等待发货订单数
		setAttr("unreadMessageCount", getUnreadMessageCount()); // 未读消息
		setAttr("storeAlertCount", getStoreAlertCount()); // 商品库存报警
		
		setAttr("marketableProductCount", getMarketableProductCount()); // 已上架商品
		setAttr("unMarketableProductCount", getUnMarketableProductCount()); // 已下架商品
		setAttr("memberTotalCount", getMemberTotalCount()); // 会员总数
		setAttr("articleTotalCount", getArticleTotalCount()); // 文章总数
		
		render("/admin/admin_index.html");
	}	
	
	// 获取未处理订单数
	public Long getUnprocessedOrderCount() {
		return Orders.dao.getUnprocessedOrderCount();
	}
	
	// 获取已支付未发货订单数
	public Long getPaidUnshippedOrderCount() {
		return Orders.dao.getPaidUnshippedOrderCount();
	}
	
	// 获取未读消息数
	public Long getUnreadMessageCount() {
		return Message.dao.getUnreadMessageCount();
	}
	
	// 获取商品库存报警数
	public Long getStoreAlertCount() {
		return Product.dao.getStoreAlertCount();
	}
	
	// 获取已上架商品数
	public Long getMarketableProductCount() {
		return Product.dao.getMarketableProductCount();
	}
	
	// 获取已下架商品数
	public Long getUnMarketableProductCount() {
		return Product.dao.getUnMarketableProductCount();
	}
	
	// 获取会员总数
	public Long getMemberTotalCount() {
		return Member.dao.getTotalCount();
	}
	
	// 获取文章总数
	public Long getArticleTotalCount() {
		return Article.dao.getTotalCount();
	}
		
	// 后台主页面
	public void center(){
		setAttr("menuList",menuService.getMenuTree());
		setAttr("loginUsername", getLoginAdminName());
		setAttr("countMail",getUnreadMessageCount());
		render("/admin/admin_center.html");
	}
	
	// 后台Header
	/*public void header(){
		setAttr("loginUsername", getLoginAdminName());
		render("/admin/admin_header.html");
	}*/
	
	// 后台中间(显示/隐藏菜单)
	public void middle(){
		render("/admin/admin_middle.html");
	}
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/admin_list.html");
	}	
	
}
