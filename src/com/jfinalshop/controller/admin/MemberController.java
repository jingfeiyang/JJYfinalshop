package com.jfinalshop.controller.admin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinalshop.model.Area;
import com.jfinalshop.model.Deposit;
import com.jfinalshop.model.Deposit.DepositType;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.MemberAttribute;
import com.jfinalshop.model.MemberAttribute.AttributeType;
import com.jfinalshop.model.MemberRank;
import com.jfinalshop.model.Message;
import com.jfinalshop.model.Orders;
import com.jfinalshop.validator.admin.MemberValidator;

/**
 * 后台类 - 会员
 * 
 */

@ControllerBind(controllerKey="/admin/member")
public class MemberController extends BaseAdminController<Member>{
			
	private Member member;
	
	// 是否已存在 ajax验证
	public void checkUsername() {
		String username = getPara("member.username","");
		if (Member.dao.isExistByUsername(username)) {
			renderText("false");
		} else {
			renderText("true");
		}
	}
		
	// 列表
	public void list() {
		findByPage();
		render("/admin/member_list.html");
	}
		
	// 添加
	public void add() {
		setAttr("allMemberRank", getAllMemberRank());
		setAttr("enabledMemberAttributeList", getEnabledMemberAttributeList());
		render("/admin/member_input.html");
	}

	// 编辑
	public void edit() {
		id = getPara("id", id);
		if (StrKit.notBlank(id)) {
			member = Member.dao.findById(id);
			setAttr("member", member);
		}
		setAttr("enabledMemberAttributeList", getEnabledMemberAttributeList());
		setAttr("allMemberRank", getAllMemberRank());
		render("/admin/member_input.html");
	}	
	
	// 保存
	@Before({MemberValidator.class,Tx.class})
	public void save() {
		member = getModel(Member.class);
		String rePassword = getPara("rePassword","");
		
		if (member.getBigDecimal("deposit").compareTo(new BigDecimal("0")) < 0) {
			addActionError("预存款不允许小于0");
			return;
		}
		if (!StringUtils.equalsIgnoreCase(member.getStr("password"), rePassword)) {
			addActionError("两次密码输入不一致!");
			return;
		}
		
		Map<String, String> memberAttributeMap = new HashMap<String, String>();
		List<MemberAttribute> enabledMemberAttributeList = MemberAttribute.dao.getEnabledMemberAttributeList();
		for (MemberAttribute memberAttribute : enabledMemberAttributeList) {
			String parameterValues = getPara(memberAttribute.getStr("id"),"");
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
				if (memberAttribute.getAttributeType() == AttributeType.email) {
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
				if (attributeType == AttributeType.select.ordinal() || attributeType == AttributeType.checkbox.ordinal()) {
					List<String> attributeOptionList = memberAttribute.getAttributeOptionList();
					if (!attributeOptionList.contains(parameterValues)) {
						addActionError("参数错误!");
						return;
					}
				}
				memberAttributeMap.put(memberAttribute.getStr("id"), parameterValues);
			}
		}
		member.set("username",member.getStr("username").toLowerCase());
		member.set("password",DigestUtils.md5Hex(member.getStr("password")));
		member.set("safeQuestion",null);
		member.set("safeAnswer",null);
		member.set("isAccountLocked",false);
		member.set("loginFailureCount",0);
		member.set("passwordRecoverKey",null);
		member.set("lockedDate",null);
		member.set("loginDate",new Date());
		member.set("registerIp",getRequest().getRemoteAddr());
		member.set("loginIp",null);
		saved(member);
		
		// 保存会员属性
		if (memberAttributeMap != null && memberAttributeMap.size() > 0){
			member.setMemberAttributeMap(memberAttributeMap);
		}		
		
		// 预存款记录
		if (member.getBigDecimal("deposit").compareTo(new BigDecimal("0")) > 0) {
			Deposit deposit = new Deposit();
			deposit.set("depositType",DepositType.adminRecharge.ordinal());
			deposit.set("credit",member.getBigDecimal("deposit"));
			deposit.set("debit",new BigDecimal("0"));
			deposit.set("balance",member.getBigDecimal("deposit"));
			deposit.set("member_id",member.getStr("id"));
			deposit.save(deposit);
		}
		redirect("/admin/member/list");
	}
		
	// 更新
	@Before({MemberValidator.class,Tx.class})
	public void update() {		
		member = getModel(Member.class);
		String rePassword = getPara("rePassword","");
		
		if (member.getBigDecimal("deposit").compareTo(new BigDecimal("0")) < 0) {
			addActionError("预存款不允许小于0");
			return;
		}
		if (member.getStr("password") != null && !StringUtils.equalsIgnoreCase(member.getStr("password"), rePassword)) {
			addActionError("两次密码输入不一致!");
			return;
		}
		
		Map<String, String> memberAttributeMap = new HashMap<String, String>();
		List<MemberAttribute> enabledMemberAttributeList = MemberAttribute.dao.getEnabledMemberAttributeList();
		for (MemberAttribute memberAttribute : enabledMemberAttributeList) {
			String parameterValues = getPara(memberAttribute.getStr("id"),"");
			
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
				if (memberAttribute.getAttributeType() == AttributeType.email) {
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
				if (attributeType == AttributeType.select.ordinal() || attributeType == AttributeType.checkbox.ordinal()) {
					List<String> attributeOptionList = memberAttribute.getAttributeOptionList();
					if (!attributeOptionList.contains(parameterValues)) {
						addActionError("参数错误!");
						return;
					}
				}
				memberAttributeMap.put(memberAttribute.getStr("id"), parameterValues);
			}
		}
		
		// 保存会员属性
		if (memberAttributeMap != null && memberAttributeMap.size() > 0){
			member.setMemberAttributeMap(memberAttributeMap);
		}	
		
		Member persistent = Member.dao.findById(member.getStr("id"));
		BigDecimal previousDeposit = persistent.getBigDecimal("deposit");
		BigDecimal currentDeposit = member.getBigDecimal("deposit");
		
		//密码是否有修改	
		if (StringUtils.isEmpty(member.getStr("password"))) {
			member.set("password",persistent.getStr("password"));
		} else {
			member.set("password",DigestUtils.md5Hex(member.getStr("password")));
		}
		updated(member);
		
		// 预存款记录
		if (currentDeposit.compareTo(previousDeposit) > 0) {
			Deposit deposit = new Deposit();
			deposit.set("depositType",DepositType.adminRecharge.ordinal());
			deposit.set("credit",currentDeposit.subtract(previousDeposit));
			deposit.set("debit",new BigDecimal("0"));
			deposit.set("balance",currentDeposit);
			deposit.set("member_id",persistent.getStr("id"));
			deposit.save(deposit);
		} else if (member.getBigDecimal("deposit").compareTo(previousDeposit) < 0) {
			Deposit deposit = new Deposit();
			deposit.set("depositType",DepositType.adminRecharge.ordinal());
			deposit.set("credit",new BigDecimal("0"));
			deposit.set("debit",previousDeposit.subtract(currentDeposit));
			deposit.set("balance",currentDeposit);
			deposit.set("member_id",persistent.getStr("id"));
			deposit.save(deposit);
		}
		redirect("/admin/member/list");
	}
		
	// 删除
	@Before(Tx.class)
	public void delete() {
		ids = getParaValues("ids");
		if (ids != null && ids.length > 0) {
			for (String id : ids) {
				Member member = Member.dao.findById(id);
				String username = member.getStr("username");
				// 检查是否有预付款
				if (member.getBigDecimal("deposit").compareTo(new BigDecimal("0")) > 0) {
					ajaxJsonErrorMessage("会员[" + username + "]预付款余额不为零，删除失败！");
					return;
				}
				// 充值记录
				List<Deposit> depositList = member.getDepositList();
				if (depositList != null && depositList.size() > 0){
					ajaxJsonErrorMessage("会员[" + username + "]存在充值记录，删除失败！");
					return;
				}				
				// 检查是否有订单
				List<Orders> orderList = member.getOrderList();
				if (orderList != null && orderList.size() > 0){
					ajaxJsonErrorMessage("会员[" + username + "]存在订单，删除失败！");
					return;
				}
				// 收件箱是否存在消息
				List<Message> inboxMessage = member.getInboxMessageList();
				if (inboxMessage != null && inboxMessage.size() > 0){
					ajaxJsonErrorMessage("会员[" + username + "]收件箱有内容，删除失败！");
					return;
				}
				// 发件箱是否存在消息
				List<Message> outboxMessage = member.getOutboxMessageList();
				if (outboxMessage != null && outboxMessage.size() > 0){
					ajaxJsonErrorMessage("会员[" + username + "]发件箱有内容，删除失败！");
					return;
				}				
				Db.deleteById("member_memberattributemapstore", "member_id", member.getStr("id"));
				member.delete();
			}
			ajaxJsonSuccessMessage("删除成功！");
		}
	}
		
	// 获取所有会员等级
	public List<MemberRank> getAllMemberRank() {
		return MemberRank.dao.getAll();
	}
	
	// 获取已启用的会员注册项
	public List<MemberAttribute> getEnabledMemberAttributeList() {
		return MemberAttribute.dao.getEnabledMemberAttributeList();
	}
}
