package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 物流公司
 * 
 */
public class DeliveryCorp extends Model<DeliveryCorp>{

	private static final long serialVersionUID = 6807939550010139287L;
	
	public static final DeliveryCorp dao = new DeliveryCorp();
	
	public List<DeliveryCorp> getAll() {
		return dao.find("select * from deliverycorp");
	}
}
