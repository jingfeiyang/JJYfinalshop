package com.jfinalshop.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 会员注册协议
 * 
 */
public class Agreement extends Model<Agreement>{

	private static final long serialVersionUID = -9117047038133409488L;
	
	public static final Agreement dao = new Agreement();

	public Agreement getAll(){
		return dao.findFirst("select * from agreement");
	}
}
