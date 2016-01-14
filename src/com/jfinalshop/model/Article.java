package com.jfinalshop.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.BulletList;
import org.htmlparser.tags.DefinitionList;
import org.htmlparser.tags.DefinitionListBullet;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ParagraphTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.TextExtractingVisitor;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinalshop.util.CommonUtil;

/**
 * 实体类 - 文章
 * 
 */
public class Article extends Model<Article>{

	private static final long serialVersionUID = 3557090377516817056L;
	
	public static final Article dao = new Article();
	
	public static final int MAX_RECOMMEND_ARTICLE_LIST_COUNT = 20;// 推荐文章列表最大文章数
	public static final int MAX_HOT_ARTICLE_LIST_COUNT = 20;// 热点文章列表最大文章数
	public static final int MAX_NEW_ARTICLE_LIST_COUNT = 20;// 最新文章列表最大文章数
	public static final int MAX_PAGE_CONTENT_COUNT = 2000;// 内容分页每页最大字数
	public static final int DEFAULT_ARTICLE_LIST_PAGE_SIZE = 20;// 文章列表默认每页显示数

	
	/**
	 * 根据ArticleCategory和Pager对象，获取此分类下的文章分页对象（只包含isPublication=true的对象，包含子分类文章）
	 * 
	 * @param articleCategory
	 *            文章分类
	 *            
	 * @param pager
	 *            分页对象
	 * 
	 * @return Pager
	 */
	public Page<Article> getArticlePager(int pageNumber, int pageSize, ArticleCategory articleCategory) {
		String select = "select a.* ";
		String sqlExceptSelect =  "" 
						 +"  from article a"
						 +" inner join articlecategory ac " 
						 +"   on a.articlecategory_id = ac.id " 
						 +" where (a.articlecategory_id = ? or ac.path like ?) " 
						 +"  and a.ispublication = ? ";
		Page<Article> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect,articleCategory.getStr("id"),articleCategory.getStr("path"),true);
		return pager;
	}
	
	/**
	 * 根据分页对象搜索文章
	 * 
	 * @return 分页对象
	 */
	public Page<Article> search(int pageNumber, int pageSize, String keyword) {
		String select = "select * ";
		String sqlExceptSelect = " from Article where isPublication = true ";

		if (StrKit.notBlank(keyword)) {
			sqlExceptSelect += " and title like '%" + keyword + "%'";
		}
		
		sqlExceptSelect += " order by hits desc, isTop desc, createDate desc";
				
		Page<Article> pager = dao.paginate(pageNumber, pageSize, select, sqlExceptSelect);
		return pager;
	}
	
	/**
	 * 根据最大返回数获取所有推荐文章(只包含isPublication=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 所有推荐文章集合
	 */
	public List<Article> getRecommendArticleList(int maxResults) {
		String sql = "select * from Article where isPublication = ? and isRecommend = ? order by isTop desc, createDate desc limit ?";
		return dao.find(sql,true,true,maxResults);
	}
	
	/**
	 * 根据ArticleCategory对象和最大返回数获取此分类下的所有推荐文章(只包含isPublication=true的对象，包含子分类文章)
	 * 
	 * @param articleCategory
	 *            文章分类
	 *            
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的所有推荐文章集合
	 */
	public List<Article> getRecommendArticleList(ArticleCategory articleCategory, int maxResults) {
		String sql = "select * from Article where isPublication = ? and isRecommend = ? and articleCategory_id = ?  order by isTop desc, createDate desc limit ?";
		return dao.find(sql,true,true,articleCategory.getStr("id"),maxResults);
	}
	
	/**
	 * 根据最大返回数获取所有热点文章(只包含isPublication=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 所有热点文章集合
	 */
	public List<Article> getHotArticleList(int maxResults) {
		String sql = "select * from Article  where isPublication = ? order by hits desc, isTop desc, createDate desc limit ?";
		return dao.find(sql,true,maxResults);
	}
	
	/**
	 * 根据ArticleCategory对象和最大返回数获取此分类下的所有热点文章(只包含isPublication=true的对象，包含子分类文章)
	 * 
	 * @param articleCategory
	 *            文章分类
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的所有热点文章集合
	 */
	public List<Article> getHotArticleList(ArticleCategory articleCategory, int maxResults) {
		String sql = "select * from Article  where isPublication = ? and articleCategory_id = ? order by hits desc, isTop desc, createDate desc limit ?";
		return dao.find(sql,true,articleCategory.getStr("id"),maxResults);
	}
	
	/**
	 * 根据最大返回数获取最新文章(只包含isPublication=true的对象，不限分类)
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 最新文章集合
	 */
	public List<Article> getNewArticleList(int maxResults) {
		String sql = "select * from Article where isPublication = ? order by article.createDate desc limit ?";
		return dao.find(sql,true,maxResults);
	}
	
	/**
	 * 根据ArticleCategory对象和最大返回数获取此分类下的最新文章(只包含isPublication=true的对象，包含子分类文章)
	 * 
	 * @param articleCategory
	 *            文章分类
	 * 
	 * @param maxResults
	 *            最大返回数
	 * 
	 * @return 此分类下的最新文章集合
	 */
	public List<Article> getNewArticleList(ArticleCategory articleCategory, int maxResults) {
		String sql = "select * from Article  where isPublication = ? and articleCategory_id = ? order by article.createDate desc limit ?";
		return dao.find(sql,true,articleCategory.getStr("id"),maxResults);
	}
	
	/**
	 * 根据起始结果数、最大结果数，获取所有文章（只包含isPublication=true的对象）
	 * 
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有文章集合
	 */
	public List<Article> getArticleList(int firstResult, int maxResults) {
		String sql = "select * from Article where isPublication = ? order by isTop desc, createDate desc limit ?,?";
		return dao.find(sql, true, firstResult, maxResults);
	}
	
	/**
	 * 根据ArticleCategory对象、起始结果数、最大结果数，获取此分类下的所有文章（只包含isPublication=true的对象，包含子分类文章）
	 * 
	 * @param articleCategory
	 *            文章分类
	 *            
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有文章集合
	 */
	public List<Article> getArticleList(ArticleCategory articleCategory, int firstResult, int maxResults) {
		String sql =  "" 
					 +"select a.* "
					 +"  from article a, articlecategory ac" 
					 +" where a.articlecategory_id = ac.id" 
					 +"   and a.ispublication = ?" 
					 +"   and ac.path like ?" 
					 +" order by a.istop desc, a.createdate desc  limit ?,?";
		return dao.find(sql, true, articleCategory.getStr("path") + "%", firstResult, maxResults);

	}
	/**
	 * 根据起始日期、结束日期、起始结果数、最大结果数，获取文章集合（只包含isPublication=true的对象）
	 * 
	 * @param beginDate
	 *            起始日期，为null则不限制起始日期
	 *            
	 * @param endDate
	 *            结束日期，为null则不限制结束日期
	 *            
	 * @param firstResult
	 *            起始结果数
	 *            
	 * @param maxResults
	 *            最大结果数
	 * 
	 * @return 此分类下的所有文章集合
	 */
	public List<Article> getArticleList(Date beginDate, Date endDate, int firstResult, int maxResults) {
		if (beginDate != null && endDate == null) {
			String sql = "select * from Article  where isPublication = ? and createDate >= ? order by isTop desc, createDate desc  limit ?,?";
			return dao.find(sql, true, beginDate, firstResult, maxResults);
		} else if (endDate != null && beginDate == null) {
			String sql = "select * from Article  where isPublication = ? and createDate <= ? order by isTop desc, createDate desc  limit ?,?";
			return dao.find(sql,true,endDate,firstResult, maxResults);
		} else if (endDate != null && beginDate != null) {
			String sql = "select * from Article where isPublication = ? and createDate >= ? and createDate <= ? order by isTop desc, createDate desc limit ?,?";
			return dao.find(sql, true, beginDate, endDate, firstResult, maxResults);
		} else {
			String sql = "select * from Article where isPublication = ? order by isTop desc, createDate desc limit ?,?";
			return dao.find(sql, true, firstResult, maxResults);
		}
	}
	
	/**
	 * 获取所有实体对象总数.
	 * 
	 * @return 实体对象总数
	 */
	public Long getTotalCount() {
		String sql = "select count(*) from article ";
		return Db.queryLong(sql);
	}
	
	public ArticleCategory getArticleCategory() {
		return ArticleCategory.dao.findById(getStr("articleCategory_id"));
	}
	
	// 获取HTML静态文件路径
	public List<String> getHtmlFilePathList() {
		Article article = dao.findById(getStr("id"));
		ArrayList<String> htmlFilePathList = new ArrayList<String>();
		String prefix = StringUtils.substringBeforeLast(article.getStr("htmlFilePath"), ".");
		String extension = StringUtils.substringAfterLast(article.getStr("htmlFilePath"), ".");
		htmlFilePathList.add(article.getStr("htmlFilePath"));
		for (int i = 2; i <= article.getInt("pageCount"); i++) {
			htmlFilePathList.add(prefix + "_" + i + "." + extension);
		}
		return htmlFilePathList;
	}

	// 获取文章分页内容
	public List<String> getPageContentList() {
		List<String> pageContentList = new ArrayList<String>();
		// 如果文章内容长度小于等于每页最大字符长度则直接返回所有字符
		if (getStr("content").length() <= Article.MAX_PAGE_CONTENT_COUNT) {
			pageContentList.add(getStr("content"));
			return pageContentList;
		}
		NodeFilter tableFilter = new NodeClassFilter(TableTag.class);// TABLE
		NodeFilter divFilter = new NodeClassFilter(Div.class);// DIV
		NodeFilter paragraphFilter = new NodeClassFilter(ParagraphTag.class);// P
		NodeFilter bulletListFilter = new NodeClassFilter(BulletList.class);// UL
		NodeFilter bulletFilter = new NodeClassFilter(Bullet.class);// LI
		NodeFilter definitionListFilter = new NodeClassFilter(DefinitionList.class);// DL
		NodeFilter DefinitionListBulletFilter = new NodeClassFilter(DefinitionListBullet.class);// DD

		OrFilter orFilter = new OrFilter();
		orFilter.setPredicates(new NodeFilter[] { paragraphFilter, divFilter, tableFilter, bulletListFilter, bulletFilter, definitionListFilter, DefinitionListBulletFilter });
		List<Integer> indexList = new ArrayList<Integer>();
		// 按每页最大字符长度分割文章内容
		List<String> contentList = CommonUtil.splitString(getStr("content"), Article.MAX_PAGE_CONTENT_COUNT);
		for (int i = 0; i < contentList.size() - 1; i++) {
			try {
				String currentContent = contentList.get(i);
				Parser parser = Parser.createParser(currentContent, "UTF-8");
				NodeList nodeList = parser.parse(orFilter);
				if (nodeList.size() > 0) {
					// 若在此段内容中查找到相关标签，则记录最后一个标签的索引位置
					Node node = nodeList.elementAt(nodeList.size() - 1);
					indexList.add(i * Article.MAX_PAGE_CONTENT_COUNT + node.getStartPosition());
				} else {
					// 若在此段内容中未找到任何相关标签，则查找相关标点符号，并记录最后一个标点符号的索引位置
					String regEx = "\\.|。|\\!|！|\\?|？";
					Pattern pattern = Pattern.compile(regEx);
					Matcher matcher = pattern.matcher(currentContent);
					if (matcher.find()) {
						int endIndex = 0;
						while (matcher.find()) {
							endIndex = matcher.end();
						}
						indexList.add(i * Article.MAX_PAGE_CONTENT_COUNT + endIndex);
					} else {
						indexList.add((i + 1) * Article.MAX_PAGE_CONTENT_COUNT);
					}
				}
			} catch (ParserException e) {
				e.printStackTrace();
			}
		}
		for (int i = 0; i <= indexList.size(); i++) {
			String pageContent = "";
			if (i == 0) {
				pageContent = getStr("content").substring(0, indexList.get(0));
			} else if (i == indexList.size()) {
				pageContent = getStr("content").substring(indexList.get(i - 1));
			} else {
				pageContent = getStr("content").substring(indexList.get(i - 1), indexList.get(i));
			}
			try {
				// 对分割出的分页内容进行HTML标签补全
				Parser parser = Parser.createParser(pageContent, "UTF-8");
				NodeList nodeList = parser.parse(orFilter);
				String contentResult = nodeList.toHtml();
				if (StringUtils.isEmpty(contentResult)) {
					contentResult = pageContent;
				}
				pageContentList.add(contentResult);
			} catch (ParserException e) {
				e.printStackTrace();
			}
		}
		return pageContentList;
	}
	
	// 获取文章内容文本（清除HTML标签后的文本内容）
	public String getContentText() {
		String result = "";
		try {
			Parser parser = Parser.createParser(getStr("content"), "UTF-8");
			TextExtractingVisitor textExtractingVisitor = new TextExtractingVisitor();
			parser.visitAllNodesWith(textExtractingVisitor);
			result = textExtractingVisitor.getExtractedText();
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return result;
	}
}
