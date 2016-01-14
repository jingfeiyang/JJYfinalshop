package com.jfinalshop.model;

import java.util.List;

import cn.dreampie.tree.TreeNode;
import cn.dreampie.web.model.Model;


public class Menu extends Model<Menu> implements TreeNode<Menu>{
	
	private static final long serialVersionUID = -5360005768283462038L;

	public static final Menu dao = new Menu();

	@Override
	public long getId() {
		return this.getLong("id");
	}

	@Override
	public long getParentId() {
		return this.getLong("parent_id");
	}

	@Override
	public void setChildren(List<Menu> children) {
		this.put("children", children);	
		
	}
	
	@Override
	public List<Menu> getChildren() {
		return this.get("children");
	}
	
}
