package com.jfinalshop.controller.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.DeliveryType;
import com.jfinalshop.model.DeliveryType.DeliveryMethod;
import com.jfinalshop.model.Product.WeightUnit;
import com.jfinalshop.validator.admin.DeliveryTypeValidator;

/**
 * 后台类 - 配送方式
 * 
 */
public class DeliveryTypeController extends BaseAdminController<DeliveryType>{
	
	private DeliveryType deliveryType;
	
	// 添加
	public void add() {
		setAttr("allDeliveryMethod", getAllDeliveryMethod());
		setAttr("allWeightUnit", getAllWeightUnit());
		setAttr("allDeliveryCorp", DeliveryType.dao.getAllDeliveryCorp());
		render("/admin/delivery_type_input.html");
	}

	// 编辑
	public void edit() {
		String id = getPara("id","");
		if(StrKit.notBlank(id)){
			setAttr("deliveryType", DeliveryType.dao.findById(id));
		}
		setAttr("allDeliveryMethod", getAllDeliveryMethod());
		setAttr("allWeightUnit", getAllWeightUnit());
		setAttr("allDeliveryCorp", DeliveryType.dao.getAllDeliveryCorp());
		render("/admin/delivery_type_input.html");
	}

	// 列表
	public void list() {
		findByPage();
		render("/admin/delivery_type_list.html");
	}
		
	// 是否已存在 ajax验证
	public void checkName() {
		String value = getPara("deliveryType.name");
		if (isUnique("deliverytype", "name", value)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
	
	// 保存
	@Before(DeliveryTypeValidator.class)
	public void save(){
		deliveryType = getModel(DeliveryType.class);
		
		String deliveryMethod = getPara("deliveryMethod","");		
		if (StrKit.notBlank(deliveryMethod)){
			deliveryType.setDeliveryMethod(deliveryMethod);
		}
		
		String firstWeightUnit = getPara("firstWeightUnit","");
		if(StrKit.notBlank(firstWeightUnit)){
			deliveryType.setFirstWeightUnit(firstWeightUnit);
		}
		
		String continueWeightUnit = getPara("continueWeightUnit","");
		if(StrKit.notBlank(continueWeightUnit)){
			deliveryType.setContinueWeightUnit(continueWeightUnit);
		}
		
		if (deliveryType.getBigDecimal("firstWeightPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("首重价格不允许小于0");
			return;
		}
		
		if (deliveryType.getBigDecimal("continueWeightPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("续重价格不允许小于0");
			return;
		}
		saved(deliveryType);
		redirect("/deliveryType/list");
	}
	
	// 更新
	@Before(DeliveryTypeValidator.class)
	public void update(){
		deliveryType = getModel(DeliveryType.class);
		
		String deliveryMethod = getPara("deliveryMethod","");		
		if (StrKit.notBlank(deliveryMethod)){
			deliveryType.setDeliveryMethod(deliveryMethod);
		}
		
		String firstWeightUnit = getPara("firstWeightUnit","");
		if(StrKit.notBlank(firstWeightUnit)){
			deliveryType.setFirstWeightUnit(firstWeightUnit);
		}
		
		String continueWeightUnit = getPara("continueWeightUnit","");
		if(StrKit.notBlank(continueWeightUnit)){
			deliveryType.setContinueWeightUnit(continueWeightUnit);
		}
		
		if (deliveryType.getBigDecimal("firstWeightPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("首重价格不允许小于0");
			return;
		}
		
		if (deliveryType.getBigDecimal("continueWeightPrice").compareTo(new BigDecimal("0")) < 0) {
			addActionError("续重价格不允许小于0");
			return;
		}
		updated(deliveryType);
		redirect("/deliveryType/list");
	}
	
	// 删除
	public void delete() {
		ids = getParaValues("ids");
		long totalCount = DeliveryType.dao.getTotalCount();
		if (ids.length >= totalCount) {
			ajaxJsonErrorMessage("删除失败!必须至少保留一个配送方式");
		} else {
			for (String id : ids) {
				if(DeliveryType.dao.deleteById(id)){	
					ajaxJsonSuccessMessage("删除成功！");
				}else{
					ajaxJsonErrorMessage("删除失败！");
				}
			}
		}
	}
	
	// 获取所有配送类型
	public List<DeliveryMethod> getAllDeliveryMethod() {
		List<DeliveryMethod> allDeliveryMethod = new ArrayList<DeliveryMethod>();
		for (DeliveryMethod deliveryMethod : DeliveryMethod.values()) {
			allDeliveryMethod.add(deliveryMethod);
		}
		return allDeliveryMethod;
	}

	// 获取所有重量单位
	public List<WeightUnit> getAllWeightUnit() {
		List<WeightUnit> allWeightUnit = new ArrayList<WeightUnit>();
		for (WeightUnit weightUnit : WeightUnit.values()) {
			allWeightUnit.add(weightUnit);
		}
		return allWeightUnit;
	}
}
