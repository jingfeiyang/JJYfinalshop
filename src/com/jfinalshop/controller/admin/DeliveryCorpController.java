package com.jfinalshop.controller.admin;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.DeliveryCorp;
import com.jfinalshop.validator.admin.DeliveryCorpValidator;

/**
 * 后台类 - 物流公司
 * 
 */
public class DeliveryCorpController extends BaseAdminController<DeliveryCorp>{
	
	private DeliveryCorp deliveryCorp;

	// 列表
	public void list() {
		findByPage();
		render("/admin/delivery_corp_list.html");
	}
		
	// 添加
	public void add() {
		render("/admin/delivery_corp_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("deliveryCorp", DeliveryCorp.dao.findById(id));
		}		
		render("/admin/delivery_corp_input.html");
	}
		
	// 是否已存在 ajax验证
	public void checkName() {
		String value = getPara("deliveryCorp.name","");
		if (isUnique("deliverycorp", "name", value)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
				
	// 保存
	@Before(DeliveryCorpValidator.class)
	public void save(){
		deliveryCorp = getModel(DeliveryCorp.class);
		saved(deliveryCorp);
		redirect("/deliveryCorp/list");
	}
	
	// 更新
	@Before(DeliveryCorpValidator.class)
	public void update(){
		deliveryCorp = getModel(DeliveryCorp.class);
		updated(deliveryCorp);
		redirect("/deliveryCorp/list");
	}
	
	// 删除
	public void delete(){
		ids = getParaValues("ids");
		if(ids != null && ids.length > 0){
			for (String id : ids) {
				if(DeliveryCorp.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
}
