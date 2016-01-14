package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 角色
 * 
 */

public class Role extends Model<Role>{

	private static final long serialVersionUID = -177371150424745544L;
	
	public static final Role dao = new Role();
	
	/**
	 * 获取所有Role实体对象集合.
	 * 
	 * @return Role实体对象集合
	 */
	public List<Role> getAllRole(){
		return dao.find("select * from role");
	}
	
	/**
	 * 获取所有Resource实体对象集合.
	 * 
	 * @return Resource实体对象集合
	 */
	public List<Resource> getAllResource() {
		return Resource.dao.getAll();
	}

	/**
	 * 检查角色名是否存在
	 * @param 
	 * @return
	 */
	public boolean checkName(String name) {
		return dao.findFirst("select name from role where name = ? limit 1", name) == null;
	}
	
	/**
	 * 检查角色值是否存在
	 * @param 
	 * @return
	 */
	public boolean checkValue(String value) {
		return dao.findFirst("select value from role where value = ? limit 1", value) == null;
	}
	
	/**
	 * 获得资源
	 */
	public List<Resource> getResourceList(){
		String sql ="" 
				 +" select r.*" 
				 +"  from role_resource rr" 
				 +"  left outer join resource r" 
				 +"    on rr.resourceset_id = r.id" 
				 +" where rr.roleset_id = ?";
		return Resource.dao.find(sql,getStr("id"));		
	}
	
	/**
	 * 获得角色对应的用户
	 */
	public List<Admin> getAdminList(){
		String sql =  "" 
				 + "select a.*"
				 +"  from admin_role ar" 
				 +"  left outer join admin a" 
				 +"    on ar.adminset_id = a.id" 
				 +" where ar.roleset_id = ?";
		return Admin.dao.find(sql,getStr("id"));		
	}
	
}
