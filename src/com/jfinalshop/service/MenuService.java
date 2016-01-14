package com.jfinalshop.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinalshop.model.Menu;



public class MenuService {

	/**
	 * 树形菜单
	 * @return
	 * @return List<Menu>
	 *
	 */
	public List<Menu> getMenuTree() {
		List<Menu> areaList = Menu.dao.findAll();
		List<Menu> nodeList = new ArrayList<Menu>();
		for (Menu node1 : areaList) {
			boolean mark = false;
			for (Menu node2 : areaList) {
				if (node1.getParentId() > 0L && node1.getParentId() == node2.getId()) {
					mark = true;
					if (node2.getChildren() == null)
						node2.setChildren(new ArrayList<Menu>());
					node2.getChildren().add(node1);
					break;
				}
			}
			if (!mark) {
				nodeList.add(node1);
			}
		}
		return nodeList;
	}
}
