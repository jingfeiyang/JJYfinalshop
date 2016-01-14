package com.jfinalshop.model; 

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="member_memberattributemapstore")
public class MemberAttributeMapStore extends Model<MemberAttributeMapStore>{
	
	private static final long serialVersionUID = -2203368079150202428L;
	
	public static final MemberAttributeMapStore dao = new MemberAttributeMapStore();

}
