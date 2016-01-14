package com.jfinalshop.model;

import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 退货
 * 
 */
public class Reship extends Model<Reship>{

	private static final long serialVersionUID = -2172630625298053536L;
	
	public static final Reship dao = new Reship();

	// 重写save方法
	public boolean save(Reship reship){
		reship.set("id", CommonUtil.getUUID());
		reship.set("createDate", new Date());		
		return reship.save();
	}
		
	// 物流项
	public List<DeliveryItem> getDeliveryItem(){
		String sql = "select * from deliveryitem where reship_id = ?";
		return DeliveryItem.dao.find(sql,getStr("id"));		
	}
	
	// 订单
	public Orders getOrder(){
		return Orders.dao.findById(getStr("order_id"));
	}
	
	/**
	 * 获取最后生成的退货编号
	 * 
	 * @return 退货编号
	 */
	public String getLastReshipSn() {
		String sql = "select * from Reship  order by createDate desc";
		Reship reshipList =  dao.findFirst(sql);
		if (reshipList != null) {
			return reshipList.getStr("reshipSn");
		} else {
			return null;
		}
	}
 }
