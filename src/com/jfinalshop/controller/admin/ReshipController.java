package com.jfinalshop.controller.admin;

import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Reship;

/**
 * 后台类 - 退货
 * 
 */
public class ReshipController extends BaseAdminController<Reship>{
	
	// 列表
	public void list() {
		findByPage();
		render("/admin/reship_list.html");
	}
	
	// 查看
	public void view() {
		id = getPara("id", "");
		if (StrKit.notBlank(id)) {
			setAttr("reship", Reship.dao.findById(id));
			render("/admin/reship_view.html");
		}
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for(String id : ids){
				if(Reship.dao.deleteById(id)){
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
	
}
