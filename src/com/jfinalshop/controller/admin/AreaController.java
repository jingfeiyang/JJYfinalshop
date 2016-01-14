package com.jfinalshop.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.kit.StrKit;
import com.jfinalshop.model.Area;
import com.jfinalshop.validator.admin.AreaValidator;

/**
 * 后台类 - 地区
 * 
 */
public class AreaController extends BaseAdminController<Area>{
	
	private Area area;
	private String parentId;
	private Area parent;
	private List<Area> areaList;
	
	// 是否已存在ajax验证
	public void checkName() {
		String areaName = getPara("area.name","");
		if (isUnique("area", "name", areaName)) {
			renderText("true");
		} else {
			renderText("false");
		}
	}
		
	// 列表
	public void list(){
		parentId = getPara("parentId","");		
		if (StrKit.notBlank(parentId)){
			parent = Area.dao.findById(parentId);
			areaList = Area.dao.getChildrenAreaList(parent);
			setAttr("parent", parent);
		} else {
			areaList = Area.dao.getRootAreaList();
		}
		setAttr("areaList", areaList);
		render("/admin/area_list.html");		
	}
	
	// 添加
	public void add() {
		parentId = getPara("parentId","");
		if (StringUtils.isNotEmpty(parentId)) {
			parent = Area.dao.findById(parentId);;
			setAttr("parent", parent);
		}
		render("/admin/area_input.html");
	}
	
	// 编辑
	public void edit() {
		id = getPara("id","");
		area = Area.dao.findById(id);
		parent = area.getParent();
		setAttr("parent", parent);
		setAttr("area", area);
		render("/admin/area_input.html");
	}
		
	// 保存
	@Before(AreaValidator.class)
	public void save() {
		area = getModel(Area.class);
		String newName = area.getStr("name");
		parent = area.getParent();
		if (!isUnique("area", "name", newName)) {
			addActionError("地区名称已存在!");
			return;
		}
		saved(area);
		if (parent != null) {
			String parentPath = parent.getStr("path");
			area.set("path",parentPath + Area.PATH_SEPARATOR + area.getStr("id"));
		} else {
			area.set("path",area.getStr("id"));
		}
		updated(area);
		if (area.get("parent_id") == null) {
			redirect("/area/list");
		} else {
			redirect("/area/list?parentId="+area.getStr("parent_id"));
		}
	}
		
	// 更新
	@Before(AreaValidator.class)
	public void update() {
		area = getModel(Area.class);
		if (!isUnique("area", "name", area.getStr("name"))) {
			addActionError("地区名称已存在!");
		}
		parent = area.getParent();
		if (parent != null) {
			String parentPath = parent.getStr("path");
			area.set("path",parentPath + Area.PATH_SEPARATOR + area.getStr("id"));
		} else {
			area.set("path",area.getStr("id"));
		}
		updated(area);
		if (area.get("parent_id") == null) {
			redirect("/area/list");
		} else {
			redirect("/area/list?parentId="+area.getStr("parent_id"));
		}		
	}
		
	// 删除
	public void delete() {
		id = getPara("id", "");
		if (Area.dao.deleteById(id)) {
			ajaxJsonSuccessMessage("删除成功!");
		} else {
			ajaxJsonErrorMessage("删除失败!");
		}
	}
		
	// 根据地区Path值获取下级地区JSON数据
	@Clear
	public void ajaxChildrenArea() {
		String path = getPara("path","");
		if (StringUtils.contains(path,  Area.PATH_SEPARATOR)) {
			id = StringUtils.substringAfterLast(path, Area.PATH_SEPARATOR);
		} else {
			id = path;
		}
		List<Area> childrenAreaList = new ArrayList<Area>();
		if (StringUtils.isEmpty(id)) {
			childrenAreaList = Area.dao.getRootAreaList();
		} else {
			childrenAreaList = new ArrayList<Area>(Area.dao.getChildren(id));
		}
		List<Map<String, String>> optionList = new ArrayList<Map<String, String>>();
		for (Area area : childrenAreaList) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("title", area.getStr("name"));
			map.put("value", area.getStr("path"));
			optionList.add(map);
		}		
		renderJson(optionList);
	}
}
