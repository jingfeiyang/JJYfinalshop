package com.jfinalshop.controller.shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Area;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberAttribute;
import com.jfinalshop.model.MemberAttribute.AttributeType;
import com.jfinalshop.validator.shop.ProfileValidator;

/**
 * 前台类 - 个人信息
 * 
 */
public class ProfileController extends BaseShopController<Member> {

	private Member member;
	
	// 编辑
	public void edit() {
		setAttr("member", getLoginMember());
		setAttr("enabledMemberAttributeList", MemberAttribute.dao.getEnabledMemberAttributeList());
		render("/shop/profile_input.html");
	}
	
	// 更新
	@Before(ProfileValidator.class)
	public void update() {
		member = getModel(Member.class);
		Map<String, String> memberAttributeMap = new HashMap<String, String>();
		List<MemberAttribute> enabledMemberAttributeList = MemberAttribute.dao.getEnabledMemberAttributeList();
		for (MemberAttribute memberAttribute : enabledMemberAttributeList) {
			String parameterValues = getPara(memberAttribute.getStr("id"));
			
			if (memberAttribute.getBoolean("isRequired") && (StrKit.notBlank(parameterValues))) {
				addActionError(memberAttribute.getStr("name") + "不允许为空!");
				return;
			}
			int attributeType = MemberAttribute.dao.findById(memberAttribute.getStr("id")).getAttributeType().ordinal();
			
			if (StringUtils.isNotEmpty(parameterValues)) {
				if (attributeType == AttributeType.number.ordinal()) {
					Pattern pattern = Pattern.compile("^-?(?:\\d+|\\d{1,3}(?:,\\d{3})+)(?:\\.\\d+)?");
					Matcher matcher = pattern.matcher(parameterValues);
					if (!matcher.matches()) {
						addActionError(memberAttribute.getStr("name") + "只允许输入数字!");
						return;
					}
				}
				if (attributeType == AttributeType.alphaint.ordinal()) {
					Pattern pattern = Pattern.compile("[a-zA-Z]+");
					Matcher matcher = pattern.matcher(parameterValues);
					if (!matcher.matches()) {
						addActionError(memberAttribute.getStr("name") + "只允许输入字母!");
						return;
					}
				}
				if (attributeType == AttributeType.email.ordinal()) {
					Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
					Matcher matcher = pattern.matcher(parameterValues);
					if (!matcher.matches()) {
						addActionError(memberAttribute.getStr("name") + "E-mail格式错误!");
						return;
					}
				}
				if (attributeType == AttributeType.date.ordinal()) {
					Pattern pattern = Pattern.compile("\\d{4}[\\/-]\\d{1,2}[\\/-]\\d{1,2}");
					Matcher matcher = pattern.matcher(parameterValues);
					if (!matcher.matches()) {
						addActionError(memberAttribute.getStr("name") + "日期格式错误!");
						return;
					}
				}
				if (attributeType == AttributeType.area.ordinal()) {
					if (!Area.dao.isAreaPath(parameterValues)) {
						addActionError(memberAttribute.getStr("name") + "地区错误!");
						return;
					}
				}
				if (attributeType == AttributeType.checkbox.ordinal()) {
					List<String> attributeOptionList = memberAttribute.getAttributeOptionList();
					if (!attributeOptionList.contains(parameterValues)) {
						addActionError("参数错误!");
						return;
					}
				}
				memberAttributeMap.put(memberAttribute.getStr("id"), parameterValues);
			}
		}
		Member persistent = getLoginMember();
		persistent.setMemberAttributeMap(memberAttributeMap);
		
		if (StringUtils.isNotEmpty(member.getStr("password"))) {
			String passwordMd5 = DigestUtils.md5Hex(member.getStr("password"));
			persistent.set("password",passwordMd5);
		}
		if (StringUtils.isNotEmpty(member.getStr("email"))){
			persistent.set("email",member.getStr("email"));
		}
		updated(persistent);
		renderSuccessMessage("修改成功", "/profile/edit");
	}
}
