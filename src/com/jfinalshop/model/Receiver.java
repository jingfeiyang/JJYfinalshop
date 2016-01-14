package com.jfinalshop.model;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 收货地址
 * 
 */
public class Receiver extends Model<Receiver>{

	private static final long serialVersionUID = 8207208628796116409L;
	
	public static final Receiver dao = new Receiver();
	
	public static final Integer MAX_RECEIVER_COUNT = 10;// 会员收货地址最大保存数，为null则无限制

	
	public List<Receiver> getReceiverList(Member member) {
		String sql = "select * from receiver where member_id = ?";
		return dao.find(sql,member.getStr("id"));
	}
	
	/**
	 * 重写方法，保存时若对象isDefault=true，则设置其它对象isDefault值为false
	 * 
	 */
	public void save(Receiver receiver) {
		if (receiver.getBoolean("isDefault")) {
			String sql = "select * from receiver where isDefault = ?";
			List<Receiver> receiverList = dao.find(sql,true);
			if (receiverList != null) {
				for (Receiver r : receiverList) {
					r.set("isDefault", false);
					r.update();
				}
			}
		}
		receiver.set("id", CommonUtil.getUUID());
		receiver.set("createDate", new Date());
		receiver.save();
	}
	
	/**
	 * 重写方法，更新时若对象isDefault=true，则设置其它对象isDefault值为false
	 * 
	 */
	public void update(Receiver receiver) {
		if (receiver.getBoolean("isDefault")) {
			String sql = "select * from receiver where isDefault = ?";
			List<Receiver> receiverList = dao.find(sql,true);
			if (receiverList != null) {
				for (Receiver r : receiverList) {
					r.set("isDefault", false);
					r.update();
				}
			}
		}
		receiver.set("modifyDate", new Date());
		receiver.update();
	}
	
	public boolean equals(Member obj) {
		if (obj == null)
			return false;
		return getStr("member_id").equals(obj.getStr("id"));
	}
}
