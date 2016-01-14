package com.jfinalshop.controller.admin;

import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Refund;

/**
 * 后台类 - 退款
 * 
 */
public class RefundController extends BaseAdminController<Refund>{
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/refund_list.html");
	}

	// 查看
	public void view(){
		id = getPara("id", "");
		if (StrKit.notBlank(id)) {
			setAttr("refund", Refund.dao.findById(id));
			render("/admin/refund_view.html");
		}
	}
	
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for(String id : ids){
				if(Refund.dao.deleteById(id)){
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
}
