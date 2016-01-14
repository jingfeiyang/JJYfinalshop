package com.jfinalshop.model;

import java.util.Date;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 预存款
 * 
 */
public class Deposit extends Model<Deposit>{

	private static final long serialVersionUID = 5983402751963592858L;
	
	public static final Deposit dao = new Deposit();

	public static final int DEFAULT_DEPOSIT_LIST_PAGE_SIZE = 15;// 充值记录列表默认每页显示数
	
	// 预存款操作类型（会员充值、会员支付、后台代支付、后台代扣费、后台代充值、后台退款）
	public enum DepositType {
		memberRecharge, memberPayment, adminRecharge, adminChargeback, adminPayment, adminRefund
	};
	
	public DepositType getDepositType() {
		return DepositType.values()[getInt("depositType")];
	}
	
	//重写save
	public boolean save(Deposit deposit){
		deposit.set("id", CommonUtil.getUUID());
		deposit.set("createDate", new Date());
		return deposit.save();
	}
}
