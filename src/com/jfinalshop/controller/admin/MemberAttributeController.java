package com.jfinalshop.controller.admin;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.MemberAttribute;
import com.jfinalshop.model.MemberAttribute.AttributeType;
import com.jfinalshop.validator.admin.MemberAttributeValidator;

/**
 * 后台类 - 会员属性
 * 
 */
public class MemberAttributeController extends BaseAdminController<MemberAttribute>{
	
	private MemberAttribute memberAttribute;
	private String[] attributeOptionList;
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/member_attribute_list.html");
	}
	
	// 添加
	public void add() {
		setAttr("allAttributeType", getAllAttributeType());
		render("/admin/member_attribute_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("memberAttribute", MemberAttribute.dao.findById(id));
		}
		setAttr("allAttributeType", getAllAttributeType());
		render("/admin/member_attribute_input.html");
	}
	
	// 是否已存在 ajax验证	
	public void checkName(){
		String newValue = getPara("memberAttribute.name","");	
		if (isUnique("memberAttribute","name",newValue)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
	
	// 添加
	@Before(MemberAttributeValidator.class)
	public void save() {
		memberAttribute = getModel(MemberAttribute.class);
		String attributeType = getPara("attributeType");
		attributeOptionList = getParaValues("attributeOptionList");		
		if (attributeType.equals(AttributeType.select.name()) || attributeType.equals(AttributeType.checkbox.name())) {
			if(attributeOptionList != null && attributeOptionList.length > 0) {	
				List<String> list = new ArrayList<String>();
				for (String optionList : attributeOptionList) {
					list.add(optionList);
				}
				memberAttribute.set("attributeOptionStore", JsonKit.toJson(list, 1));
			}
		} else {
			memberAttribute.set("attributeOptionStore",null);
		}
		memberAttribute.set("attributeType", AttributeType.valueOf(attributeType).ordinal());
		saved(memberAttribute);
		redirect("/memberAttribute/list");		
	}
	
	// 编辑
	@Before(MemberAttributeValidator.class)
	public void update() {
		memberAttribute = getModel(MemberAttribute.class);
		String attributeType = getPara("attributeType");
		attributeOptionList = getParaValues("attributeOptionList");		
		if (attributeType.equals(AttributeType.select.name()) || attributeType.equals(AttributeType.checkbox.name())) {
			if(attributeOptionList != null && attributeOptionList.length > 0) {	
				List<String> list = new ArrayList<String>();
				for (String optionList : attributeOptionList) {
					list.add(optionList);
				}
				memberAttribute.set("attributeOptionStore", JsonKit.toJson(list, 1));
			}
		} else {
			memberAttribute.set("attributeOptionStore",null);
		}
		memberAttribute.set("attributeType", AttributeType.valueOf(attributeType).ordinal());
		updated(memberAttribute);
		redirect("/memberAttribute/list");		
	}
	
	// 删除
	public void delete() {
		String[] ids = getParaValues("ids");
		for (String id : ids) {
			if(MemberAttribute.dao.deleteById(id)){	
				ajaxJsonSuccessMessage("删除成功！");
			}else{
				ajaxJsonErrorMessage("删除失败！");
			}
		}
	}
		
	// 获取所有商品属性类型
	public List<AttributeType> getAllAttributeType() {
		List<AttributeType> allAttributeType = new ArrayList<AttributeType>();
		for (AttributeType attributeType : AttributeType.values()) {
			allAttributeType.add(attributeType);
		}
		return allAttributeType;
	}
	
}
