package com.jfinalshop.controller.admin;

import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Payment;

/**
 * 后台类 - 支付
 * 
 */
public class PaymentController extends BaseAdminController<Payment>{
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/payment_list.html");
	}

	// 查看
	public void view(){
		id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("payment", Payment.dao.findById(id));
			render("/admin/payment_view.html");
		}
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for(String id : ids){
				if(Payment.dao.deleteById(id)){
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
}
