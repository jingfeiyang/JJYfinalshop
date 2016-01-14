package com.jfinalshop.model;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="member_product",pkName="favoriteProductSet_id")
public class MemberProduct extends Model<MemberProduct>{

	private static final long serialVersionUID = -2325263685757820088L;

	public static final MemberProduct dao = new MemberProduct();
}
