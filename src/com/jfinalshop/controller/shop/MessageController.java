package com.jfinalshop.controller.shop;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Message;
import com.jfinalshop.model.Message.DeleteStatus;
import com.jfinalshop.validator.shop.MessageValidator;

/**
 * 前台类 - 消息
 * 
 */
@ControllerBind(controllerKey = "/shop/message")
public class MessageController extends BaseShopController<Message> {

	private Message message;

	// 检查用户名是否存在
	public String checkUsername() {
		String value = getPara("toMemberUsername","");
		if (!isUnique("member","username",value)) {
			renderText("true");
		} else {
			renderText("false");
		}
		return null;
	}
		
	// 发送消息
	public void send() {
		id = getPara("id","");
		if (StringUtils.isNotEmpty(id)) {
			message = Message.dao.findById(id);
			if (message.getBoolean("isSaveDraftbox") == false || message.getFromMember() != getLoginMember()) {
				addActionError("参数错误!");
				return;
			}
		}
		setAttr("message", message);
		render("/shop/message_send.html");
	}
	
	// 回复
	public void reply() {
		id = getPara("id","");
		if (StrKit.notBlank(id)){
			message = Message.dao.findById(id);
			if (!message.getToMember().getStr("id").equals(getLoginMember().getStr("id")) ) {
				addActionError("参数错误!");
				return;
			}
		}
		setAttr("message", message);
		render("/shop/message_reply.html");
	}
	
	// 收件箱
	public void inbox() {
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Message.DEFAULT_MESSAGE_LIST_PAGE_SIZE);
		setAttr("pager", Message.dao.getMemberInboxPager(pageNumber,pageSize,getLoginMember()));
		render("/shop/message_inbox.html");
	}
	
	// 草稿箱
	public void draftbox() {
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Message.DEFAULT_MESSAGE_LIST_PAGE_SIZE);
		setAttr("pager", Message.dao.getMemberDraftboxPager(pageNumber,pageSize,getLoginMember()));
		render("/shop/message_draftbox.html");
	}
	
	// 发件箱
	public void outbox() {
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Message.DEFAULT_MESSAGE_LIST_PAGE_SIZE);
		setAttr("pager", Message.dao.getMemberOutboxPager(pageNumber,pageSize,getLoginMember()));
		render("/shop/message_outbox.html");
	}
	
	// 保存消息
	@Before(MessageValidator.class)
	public void save() {
		message = getModel(Message.class);
		String toMemberUsername = getPara("toMemberUsername","");
		if (StringUtils.isNotEmpty(toMemberUsername)) {
			Member toMember = Member.dao.getMemberByUsername(toMemberUsername);
			if (StrKit.isBlank(toMember.getStr("id"))) {
				addActionError("收件人不存在!");
				return;
			}
			if (toMember.getStr("id").equals( getLoginMember().getStr("id"))) {
				addActionError("收件人不允许为自己!");
				return;
			}
			message.set("toMember_id",toMember.getStr("id"));
		} else {
			message.set("toMember_id",null);
		}
		message.set("fromMember_id",getLoginMember().getStr("id"));
		message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal());
		message.set("isRead",false);
		
		if (StringUtils.isNotEmpty(id)) {
			Message persistent = Message.dao.findById(id);
			if (persistent.getBoolean("isSaveDraftbox") == false || persistent.getFromMember() != getLoginMember()) {
				addActionError("参数错误!");
				return;
			}
			updated(persistent);
		} else {
			saved(message);
		}
		if (message.getBoolean("isSaveDraftbox")) {
			redirect("/shop/message/draftbox");
		} else {
			redirect("/shop/message/outbox");
		}
	}
		
	// 删除
	public void delete() {
		id = getPara("id","");
		Message message = Message.dao.findById(id);
		if (message.getBoolean("isSaveDraftbox")) {
			if (message.getFromMember().getStr("id").equals(getLoginMember().getStr("id"))) {
				message.delete();
				redirectionUrl = "/shop/message/draftbox";
			}
		} else {
			if (message.getToMember() != null && message.getToMember().getStr("id").equals(getLoginMember().getStr("id"))) {
				if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal()) {
					message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal());
					message.update();
				} else if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal()) {
					message.delete();
				}
				redirectionUrl = "/shop/message/inbox";
			} else if (message.getFromMember() != null && message.getFromMember().getStr("id").equals(getLoginMember().getStr("id"))) {
				if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal()) {
					message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
					updated(message);
				} else if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal()) {
					message.delete();
				}
				redirectionUrl = "/shop/message/outbox";
			}
		}
		redirect(redirectionUrl);
	}
		
	// AJAX获取消息内容
	public void ajaxMessageContent() {
		String id = getPara("id","");
		Message message = Message.dao.findById(id);
		if (message.getToMember() != null) {
			addActionError("参数错误!");			
		}
		if (!message.getBoolean("isRead")) {
			message.set("isRead",true);
			updated(message);
		}
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(CONTENT, message.getStr("content"));
		renderJson(jsonMap);
	}
}
