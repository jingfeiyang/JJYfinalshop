package com.jfinalshop.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName = "role_resource", pkName = "roleSet_id")
public class RoleResource extends Model<RoleResource>{
	
	private static final long serialVersionUID = -372974665995862512L;
	
	public static final RoleResource dao = new RoleResource();

}
