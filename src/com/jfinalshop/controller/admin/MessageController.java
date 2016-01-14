package com.jfinalshop.controller.admin;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Member;
import com.jfinalshop.model.Message;
import com.jfinalshop.model.Message.DeleteStatus;
import com.jfinalshop.validator.admin.MessageValidator;

/**
 * 后台类 - 消息
 * 
 */
public class MessageController extends BaseAdminController<Message>{
	
	private Message message;
	
	// 收件箱
	public void inbox(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Message.DEFAULT_MESSAGE_LIST_PAGE_SIZE);
		setAttr("pager", Message.dao.getAdminInboxPager(pageNumber,pageSize,getOrderBy(),getOrderType(),getProperty(),getKeyword()));
		render("/admin/message_inbox.html");
	}

	// 发件箱
	public void outbox(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",Message.DEFAULT_MESSAGE_LIST_PAGE_SIZE);
		setAttr("pager", Message.dao.getAdminOutboxPager(pageNumber,pageSize,getOrderBy(),getOrderType(),getProperty(),getKeyword()));
		render("/admin/message_outbox.html");
	}
	
	// 发送
	public void send(){
		render("/admin/message_send.html");
	}
	
	// 发送
	public void reply(){
		String id  = getPara("id","");
		if (StrKit.notBlank(id)){
			setAttr("message", Message.dao.findById(id));
		}
		render("/admin/message_reply.html");
	}
	
	// 保存
	@Before(MessageValidator.class)
	public void save(){
		message = getModel(Message.class);
		String toMemberUsername = getPara("toMemberUsername","");
		Member toMember = Member.dao.getMemberByUsername(toMemberUsername);
		if (toMember == null) {
			renderErrorMessage("收件人不存在!");
			return;
		}
		message.set("toMember_id",toMember.getStr("id"));
		message.set("fromMember_id",null);
		message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal());			
		message.set("isRead",false);
		message.set("isSaveDraftbox",false);
		saved(message);
		redirect("/message/outbox");	
	}
	
	// 删除
	public void delete() {
		ids = getParaValues("ids");
		for (String id : ids) {
			Message message = Message.dao.findById(id);
			if (!message.getBoolean("isSaveDraftbox")) {
				if (message.getToMember() == null) {
					if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal()) {
						message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal());
						message.update();
					} else if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal()) {
						message.delete();
					}
				} else if (message.getFromMember() == null) {
					if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.nonDelete.name()).ordinal()) {
						message.set("deleteStatus",DeleteStatus.valueOf(DeleteStatus.fromDelete.name()).ordinal());
						message.update();
					} else if (message.getInt("deleteStatus") == DeleteStatus.valueOf(DeleteStatus.toDelete.name()).ordinal()) {
						message.delete();
					}
				}
			}
		}	
		ajaxJsonSuccessMessage("删除成功!");
	}
	
	// AJAX获取消息内容
	public void ajaxMessageContent() {
		String id = getPara("id","");
		Message message = Message.dao.findById(id);
		if (message.getToMember() != null) {
			renderErrorMessage("参数错误!");
			return;
		}
		if (!message.getBoolean("isRead")) {
			message.set("isRead",true);
			message.update();
		}
		Map<String, String> jsonMap = new HashMap<String, String>();
		jsonMap.put(STATUS, SUCCESS);
		jsonMap.put(CONTENT, message.getStr("content"));
		renderJson(jsonMap);
	}
}
