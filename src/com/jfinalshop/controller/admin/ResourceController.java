package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Resource;
import com.jfinalshop.validator.admin.ResourceValidator;

/**
 * 后台类 - 资源
 * 
 */
public class ResourceController extends BaseAdminController<Resource>{
	
	private Resource resource;
	
	public void list(){
		findByPage();
		render("/admin/resource_list.html");
	}
	
	// 添加
	public void add() {
		render("/admin/resource_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("resource", Resource.dao.findById(id));
		}		
		render("/admin/resource_input.html");
	}

	
	// 是否已存在 ajax验证
	public void checkName() {
		String name = getPara("resource.name","");
		if (isUnique("resource","name",name)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}

	// 是否已存在 ajax验证
	public void checkValue() {
		String value = getPara("resource.value","");
		if (isUnique("resource","value",value)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}

	// 保存
	@Before(ResourceValidator.class)
	public void save(){
		resource = getModel(Resource.class);
		resource.set("isSystem", false);
		saved(resource);
		redirect("/resource/list");	
	}
	
	// 更新
	@Before(ResourceValidator.class)
	public void update(){
		resource = getModel(Resource.class);
		Resource persistent = Resource.dao.findById(resource.getStr("id"));
		if (persistent.getBoolean("isSystem")) {
			renderErrorMessage("系统内置资源不允许修改!");
			return;
		}
		updated(resource);
		redirect("/resource/list");
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for(String id : ids){
				if(Resource.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
	
}
