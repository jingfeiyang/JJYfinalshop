package com.jfinalshop.controller.admin;

import java.util.List;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.model.Admin;
import com.jfinalshop.model.Role;
import com.jfinalshop.model.RoleResource;
import com.jfinalshop.validator.admin.RoleValidator;

/**
 * 后台类 - 角色
 *  
 */

@RequiresRoles(value={"ROLE_ADMIN","ROLE_SERVICE"},logical=Logical.OR)
public class RoleController extends BaseAdminController<Role>{
	
	private Role role;
	private String[] resourceIds;
	
	// 列表
	public void list(){
		findByPage();
		render("/admin/role_list.html");
	}
	
	// 添加
	@RequiresPermissions("role:add")
	public void add() {
		setAttr("allResource", Role.dao.getAllResource());
		render("/admin/role_input.html");
	}

	// 编辑
	@RequiresPermissions("role:edit")
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("role", Role.dao.findById(id));
		}		
		setAttr("allResource", Role.dao.getAllResource());
		render("/admin/role_input.html");
	}
		
	// 保存
	@Before({Tx.class,RoleValidator.class})
	public void save(){
		resourceIds = getParaValues("resourceIds");
		role = getModel(Role.class);
		role.set("isSystem", false);
		saved(role);
		// 角色资源
		if (resourceIds != null && resourceIds.length > 0) {
			for(String resourceId : resourceIds){
				RoleResource roleResource = new RoleResource();
				roleResource.set("roleSet_id", role.getStr("id"));
				roleResource.set("resourceSet_id", resourceId);
				roleResource.save();
			}
		}
		redirect("/role/list");	
	}

	// 更新
	@Before({Tx.class,RoleValidator.class})
	public void update(){
		resourceIds = getParaValues("resourceIds");
		role = getModel(Role.class);		
		Role persistent = Role.dao.findById(role.getStr("id"));
		if (persistent.getBoolean("isSystem")) {
			renderErrorMessage("系统内置角色不允许修改!");
			return;
		}		
		// 先删除再写入
		String roleId = role.getStr("id");		
		if (resourceIds != null && resourceIds.length > 0) {
			RoleResource.dao.deleteById(roleId);
			for (String resourceId : resourceIds) {
				RoleResource roleResource = new RoleResource();
				roleResource.set("roleSet_id", role.getStr("id"));
				roleResource.set("resourceSet_id", resourceId);
				roleResource.save();
			}
		}				
		updated(role);
		redirect("/role/list");
	}
	
	// 删除
	@Before(Tx.class)
	public void delete(){
		ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				Role role = Role.dao.findById(id);
				List<Admin> adminList = role.getAdminList();
				if (adminList != null && adminList.size() > 0) {
					ajaxJsonErrorMessage("角色[" + role.getStr("name") + "]下存在管理员，删除失败！");
					return;
				}			
				boolean isResource = Db.deleteById("role_resource", "roleSet_id", id);
				boolean isRole = Role.dao.deleteById(id);
				if (isResource && isRole) {
					ajaxJsonSuccessMessage("删除成功！");
				} else {
					ajaxJsonErrorMessage("删除失败！");
				}
			}		
		}		
	}
	
}
