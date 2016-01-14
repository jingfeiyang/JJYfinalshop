package com.jfinalshop.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 管理员角色
 * 
 */
@TableBind(tableName = "admin_role", pkName = "adminSet_id")
public class AdminRole extends Model<AdminRole>{

	private static final long serialVersionUID = -3138294156099723210L;
	
	public static final AdminRole dao = new AdminRole();

}
