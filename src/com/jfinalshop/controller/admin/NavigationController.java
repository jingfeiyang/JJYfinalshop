package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.ArticleCategory;
import com.jfinalshop.model.Navigation;
import com.jfinalshop.model.Navigation.Position;
import com.jfinalshop.model.ProductCategory;
import com.jfinalshop.validator.admin.NavigationValidator;

/**
 * 后台类 - 导航
 *   
 */
public class NavigationController extends BaseAdminController<Navigation>{
	
	private Navigation navigation;

	// 列表
	public void list() {
		findByPage();
		render("/admin/navigation_list.html");
	}
	
	// 添加
	public void add() {
		setAttr("productCategoryTreeList", ProductCategory.dao.getProductCategoryTreeList());
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/navigation_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id", "");
		if (StrKit.notBlank(id)) {
			setAttr("navigation", Navigation.dao.findById(id));
		}
		setAttr("productCategoryTreeList", ProductCategory.dao.getProductCategoryTreeList());
		setAttr("articleCategoryTreeList", ArticleCategory.dao.getArticleCategoryTreeList());
		render("/admin/navigation_input.html");
	}
	
	// 保存
	@Before(NavigationValidator.class)
	public void save() {
		navigation = getModel(Navigation.class);
		String position = getPara("position");
		navigation.set("position", Position.valueOf(position).ordinal());
		saved(navigation);
		redirect("/navigation/list");
	}
	
	// 更新
	@Before(NavigationValidator.class)
	public void update() {
		navigation = getModel(Navigation.class);
		String position = getPara("position");
		navigation.set("position", Position.valueOf(position).ordinal());
		updated(navigation);
		redirect("/navigation/list");
	}
	
	// 删除
	public void delete() {
		String[] ids = getParaValues("ids");
		for (String id : ids) {
			if (Navigation.dao.deleteById(id)) {
				ajaxJsonSuccessMessage("删除成功！");
			} else {
				ajaxJsonErrorMessage("删除失败！");
			}
		}
	}
}
