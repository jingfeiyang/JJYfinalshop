package com.jfinalshop.controller.admin;

import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Shipping;

/**
 * 后台类 - 发货
 * 
 */
public class ShippingController extends BaseAdminController<Shipping>{

	// 列表
	public void list() {
		findByPage();
		render("/admin/shipping_list.html");
	}
	
	// 查看
	public void view(){
		id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("shipping", Shipping.dao.findById(id));
		}
		render("/admin/shipping_view.html");
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for(String id : ids){
				if(Shipping.dao.deleteById(id)){
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
}
