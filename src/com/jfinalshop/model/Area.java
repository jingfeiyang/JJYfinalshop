package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 地区
 * 
 */
public class Area extends Model<Area>{

	private static final long serialVersionUID = 4432853203007019067L;
	
	public static final Area dao = new Area();
	
	public static final String PATH_SEPARATOR = ",";// 树路径分隔符
	
	/**
	 * 获取所有顶级地区集合;
	 * 
	 * @return 所有顶级地区集合
	 * 
	 */
	public List<Area> getRootAreaList() {
		String sql = "select * from area  where parent_id is null";
		return dao.find(sql);
	}

	/**
	 * 根据Area对象获取所有子类集合，若无子类则返回null;
	 * 
	 * @return 子类集合
	 * 
	 */
	public List<Area> getChildrenAreaList(Area area) {
		String sql = "select * from  area where id != ? and area.path like ?";
		return dao.find(sql,area.getStr("id"),area.getStr("path")+"%");
	}
	
	public Area getParent() {
		String parent_id = getStr("parent_id");
		return dao.findById(parent_id);
	}
	
	public List<Area> getChildren(String id) {
		String sql = "select * from area t where id != ? and t.path like ?";
		return dao.find(sql,id,id+"%");
	} 
	
	/**
	 * 判断地区Path字符串是否正确;
	 */
	public Boolean isAreaPath(String areaPath) {
		Area area = dao.findFirst("select * from area where path like ?",areaPath+"%");
		if (area == null) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 根据地区路径获取完整地区字符串，若地区路径错误则返回null
	 * 
	 * @param areaPath
	 *         地区路径
	 */	
	public String getAreaString(String areaPath) {
		if (!isAreaPath(areaPath)) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();
		String[] ids = areaPath.split(Area.PATH_SEPARATOR);
		for (String id : ids) {
			Area area = dao.findById(id);
			stringBuffer.append(area.getStr("name"));
		}
		return stringBuffer.toString();
	}
}
