package com.jfinalshop.controller.shop;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Area;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Receiver;
import com.jfinalshop.validator.shop.ReceiverValidator;

/**
 * 前台类 - 收货地址
 * 
 */
public class ReceiverController extends BaseShopController<Receiver>{
	
	private Receiver receiver;
	
	// 收货地址列表
	public void list() {
		setAttr("receiver", Receiver.dao.getReceiverList(getLoginMember()));
		render("/shop/receiver_list.html");
	}

	// 收货地址添加
	public void add() {
		Member loginMember = getLoginMember();
		List<Receiver> receiverList = loginMember.getReceiverList();
		if (receiverList != null && Receiver.MAX_RECEIVER_COUNT != null && receiverList.size() >= Receiver.MAX_RECEIVER_COUNT) {
			addActionError("只允许添加最多" + Receiver.MAX_RECEIVER_COUNT + "项收货地址!");
			return;
		}
		render("/shop/receiver_input.html");
	}
		
	// 收货地址编辑
	public void edit() {
		id = getPara("id","");
		if (StrKit.notBlank(id)){
			receiver = Receiver.dao.findById(id);
		}
		if(receiver.equals(getLoginMember().getStr("id"))) {
			addActionError("参数错误!");
			return;
		}
		setAttr("receiver", receiver);
		render("/shop/receiver_input.html");		
	}
	
	// 收货地址删除
	public void delete() {
		id = getPara("id","");
		Receiver receiver = Receiver.dao.findById(id);
		if(receiver.equals(getLoginMember().getStr("id"))) {
			addActionError("参数错误!");
			return;
		}
		receiver.delete();
		redirect("/receiver/list");
	}
	
	// 收货地址保存
	@Before(ReceiverValidator.class)
	public void save() {
		receiver = getModel(Receiver.class);
		if (StringUtils.isEmpty(receiver.getStr("phone")) && StringUtils.isEmpty(receiver.getStr("mobile"))) {
			addActionError("联系电话、联系手机必须填写其中一项!");
			return;
		}
		if (!Area.dao.isAreaPath(receiver.getStr("areaPath"))) {
			addActionError("地区错误!");
			return;
		}
		Member loginMember = getLoginMember();
		List<Receiver> receiverList = loginMember.getReceiverList();
		if (receiverList != null && Receiver.MAX_RECEIVER_COUNT != null && receiverList.size() >= Receiver.MAX_RECEIVER_COUNT) {
			addActionError("只允许添加最多" + Receiver.MAX_RECEIVER_COUNT + "项收货地址!");
			return;
		}
		
		receiver.set("member_id", loginMember.getStr("id"));
		receiver.save(receiver);
		redirect("/receiver/list");
	}
	
	// 收货地址更新
	public void update() {
		receiver = getModel(Receiver.class);
		if (StringUtils.isEmpty(receiver.getStr("phone")) && StringUtils.isEmpty(receiver.getStr("mobile"))) {
			addActionError("联系电话、联系手机必须填写其中一项!");
			return;
		}
		if (!Area.dao.isAreaPath(receiver.getStr("areaPath"))) {
			addActionError("地区错误!");
			return;
		}
		Receiver persistent = Receiver.dao.findById(receiver.getStr("id"));
		if(persistent.equals(getLoginMember().getStr("id"))) {
			addActionError("参数错误!");
			return;
		}
		receiver.update(receiver);
		redirect("/receiver/list");
	}
}
