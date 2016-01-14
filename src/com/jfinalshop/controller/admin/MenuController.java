package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinalshop.interceptor.AdminInterceptor;



/**
 * 后台类 - 菜单
 * 
 */
@Before(AdminInterceptor.class)
public class MenuController extends Controller {

	// 常用
	public void common() {
		render("/admin/menu_common.html");
	}

	// 商品
	public void product() {
		render("/admin/menu_product.html");
	}

	// 订单管理
	public void order() {
		render("/admin/menu_order.html");
	}

	// 会员
	public void member() {
		render("/admin/menu_member.html");
	}

	// 页面管理
	public void content() {
		render("/admin/menu_content.html");
	}

	// 管理员
	public void admin() {
		render("/admin/menu_admin.html");
	}

	// 商店配置
	public void setting() {
		render("/admin/menu_setting.html");
	}
}
