package com.jfinalshop.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Model;

/**
 * 实体类 - 文章分类
 * 
 */
public class ArticleCategory extends Model<ArticleCategory>{

	private static final long serialVersionUID = 8467248679952662449L;

	public static final String PATH_SEPARATOR = ",";// 树路径分隔符
	
	public static final ArticleCategory dao = new ArticleCategory();
	
	public List<ArticleCategory> getAll() {
		String sql = "select * from ArticleCategory  order by orderList asc ";
		return dao.find(sql);
	}
	
	/**
	 * 获取文章分类树集合;
	 * 
	 * @return 文章分类树集合
	 * 
	 */
	public List<ArticleCategory> getArticleCategoryTreeList() {
		List<ArticleCategory> allArticleCategoryList = this.getAll();
		return recursivArticleCategoryTreeList(allArticleCategoryList, null, null);
	}
	
	// 递归父类排序分类树
	private List<ArticleCategory> recursivArticleCategoryTreeList(List<ArticleCategory> allArticleCategoryList, ArticleCategory p, List<ArticleCategory> temp) {
		if (temp == null) {
			temp = new ArrayList<ArticleCategory>();
		}
		for (ArticleCategory articleCategory : allArticleCategoryList) {
			ArticleCategory parent = articleCategory.getParent();	
			
			if ((p == null && parent == null) || (articleCategory != null && parent == p || (parent!=null && parent.equals(p)))) {
				temp.add(articleCategory);
				List<ArticleCategory> children = articleCategory.getChildren();
				if (children != null && children.size() > 0) {
					recursivArticleCategoryTreeList(allArticleCategoryList, articleCategory, temp);
				}
			}
		}
		return temp;
	}
		
	/**
	 * 根据Article对象获取路径集合;
	 * 
	 * @return ArticleCategory集合
	 * 
	 */
	public List<ArticleCategory> getArticleCategoryPathList(Article article) {
		ArticleCategory articleCategory = article.getArticleCategory();
		List<ArticleCategory> articleCategoryList = new ArrayList<ArticleCategory>();
		articleCategoryList.addAll(this.getParentArticleCategoryList(articleCategory));
		articleCategoryList.add(articleCategory);
		return articleCategoryList;
	}
	
	/**
	 * 根据ArticleCategory对象获取路径集合;
	 * 
	 * @return ArticleCategory集合
	 * 
	 */
	public List<ArticleCategory> getArticleCategoryPathList(ArticleCategory articleCategory) {
		List<ArticleCategory> articleCategoryPathList = new ArrayList<ArticleCategory>();
		articleCategoryPathList.addAll(this.getParentArticleCategoryList(articleCategory));
		articleCategoryPathList.add(articleCategory);
		return articleCategoryPathList;
	}
	
	/**
	 * 根据ArticleCategory对象获取所有父类集合，若无父类则返回null;
	 * 
	 * @return 父类集合
	 * 
	 */	
	public List<ArticleCategory> getParentArticleCategoryList(ArticleCategory articleCategory) {
		String sql = "select * from ArticleCategory where id != ? and id in(?) order by orderList asc";
		String[] ids = articleCategory.getStr("path").split(ArticleCategory.PATH_SEPARATOR);
		return dao.find(sql,articleCategory.getStr("id"),ids);
	}
	
	/**
	 * 获取所有顶级文章分类集合;
	 * 
	 * @return 所有顶级文章分类集合
	 * 
	 */
	public List<ArticleCategory> getRootArticleCategoryList() {
		String sql = "select * from ArticleCategory where parent_id is null order by orderList asc";
		return dao.find(sql);
	}
	
	/**
	 * 根据ArticleCategory对象获取所有子类集合，若无子类则返回null;
	 * 
	 * @return 子类集合
	 * 
	 */
	public List<ArticleCategory> getChildrenArticleCategoryList(ArticleCategory articleCategory) {
		String sql = "select * from ArticleCategory where id != ? and path like ? order by orderList asc";
		return dao.find(sql, articleCategory.getStr("id"),articleCategory.getStr("path") + "%");
	}
	
	// 获得父
	public ArticleCategory getParent() {
		return dao.findById(getStr("parent_id"));
	}

	// 获得子
	public List<ArticleCategory> getChildren() {
		return dao.find("select * from ArticleCategory t where t.parent_id = ? order by orderList asc ",getStr("id"));
	}
	
	// 获得层
	public Integer getLevel() {
		return getStr("path").split(PATH_SEPARATOR).length - 1;
	}
	
	// 获取分类的文章
	public List<Article> getArticleList(){
		String sql = "select * from article where articleCategory_id = ?";
		return Article.dao.find(sql,getStr("id"));		
	}
	
	// 重写equals
	public boolean equals(ArticleCategory obj) {
		if (obj == null)
			return false;
		return getStr("id").equals(obj.getStr("id"));
	}
}
