package com.jfinalshop.controller.admin;

import com.jfinalshop.model.Log;
/**
 * 后台类 - 日志
 * 
 */
public class LogController extends BaseAdminController<Log>{

	// 列表
	public void list(){
		findByPage();
		render("/admin/log_list.html");
	}
	
	// 删除
	public void delete() {
		ids = getParaValues("ids");
		if (ids.length > 0){
			for (String id : ids) {
				if(Log.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
}
