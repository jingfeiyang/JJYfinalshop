package com.jfinalshop.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 导航
 * 
 */
public class Navigation extends Model<Navigation>{

	private static final long serialVersionUID = 7618679206151166761L;

	public static final Navigation dao = new Navigation();
	
	// 导航位置:顶部、中间、底部
	public enum Position {
		top, middle, bottom
	}
	
	/**
	 * 获取顶部Navigation对象集合（只包含isVisible=true的对象）
	 * 
	 * @return Navigation对象集合
	 * 
	 */
	public List<Navigation> getTopNavigationList() {
		String sql = "select * from  navigation where position = ? and isVisible = ? order by orderList asc";
		return dao.find(sql,Position.top.ordinal(),true);
	}

	/**
	 * 获取中间Navigation对象集合（只包含isVisible=true的对象）
	 * 
	 * @return Navigation对象集合
	 * 
	 */
	public List<Navigation> getMiddleNavigationList() {
		String sql = "select * from navigation where position = ? and isVisible = ? order by orderList asc";
		return dao.find(sql,Position.middle.ordinal(),true);
	}

	/**
	 * 获取底部Navigation对象集合（只包含isVisible=true的对象）
	 * 
	 * @return Navigation对象集合
	 * 
	 */
	public List<Navigation> getBottomNavigationList() {
		String sql = "select * from  navigation where position = ? and isVisible = ? order by orderList asc";
		return dao.find(sql,Position.bottom.ordinal(),true);
	}
	
	public Position getPosition() {
		return Position.values()[getInt("position")];
	}
}
