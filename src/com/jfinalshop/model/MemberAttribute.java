package com.jfinalshop.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 会员注册项
 * 
 */
public class MemberAttribute extends Model<MemberAttribute>{

	private static final long serialVersionUID = 2583740485777411429L;

	public static final MemberAttribute dao = new MemberAttribute();
	
	
	// 注册项类型：
	public enum AttributeType {
		text, number, alphaint, email, select, checkbox, name, gender, date, area, address, zipCode, mobile, phone, qq, msn, wangwang, skype
	}
	
	public AttributeType getAttributeType() {
		return AttributeType.values()[getInt("attributeType")];
	}
	
	// 获取可选项
	public List<String> getAttributeOptionList() {
		String attributeOptionStore = getStr("attributeOptionStore");
		if (StrKit.isBlank(attributeOptionStore)) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		JSONArray jsonArray = JSONArray.parseArray((attributeOptionStore));		
		for(int i = 0; i < jsonArray.size(); i++){
			list.add(jsonArray.get(i).toString());
		}
		return list;
	}
	
	/**
	 * 获取已启用的会员注册项.
	 * 
	 * @return 已启用的会员注册项集合.
	 */
	public List<MemberAttribute> getEnabledMemberAttributeList(){		
		return dao.find("select * from MemberAttribute  where isEnabled = ? order by orderList asc",true);		
	}
}
