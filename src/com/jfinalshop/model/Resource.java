package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 资源
 * 
 */
public class Resource extends Model<Resource>{

	private static final long serialVersionUID = 7906172672735389314L;
	
	public static final Resource dao = new Resource();
	
	/**
	 * 获得所有资源
	 * @return
	 */
	public List<Resource> getAll(){
		return dao.find("select * from resource");		
	}

	/**
	 * 检查资源名是否存在
	 * @param 
	 * @return
	 */
	public boolean checkName(String name) {
		return dao.findFirst("select name from resource where name = ? limit 1", name) == null;
	}
	
	/**
	 * 检查资源值是否存在
	 * @param 
	 * @return
	 */
	public boolean checkValue(String value) {
		return dao.findFirst("select value from resource where value = ? limit 1", value) == null;
	}
}
