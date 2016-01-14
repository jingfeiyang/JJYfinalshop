/***
 *	JFinalShop Header JavaScript
 *
 *	http://www.jfinalshop.com
 *
 *	Copyright (c) 2014 JFinalShop. All rights reserved.
 **/

$().ready( function() {
	
	// 刷新header登录、注册信息
	$.flushHeaderInfo = function () {
		var $headerShowLoginWindow = $("#headerShowLoginWindow");
		var $headerShowRegisterWindow = $("#headerShowRegisterWindow");
		var $headerLoginMemberUsername = $("#headerLoginMemberUsername");
		var $headerMemberCenter = $("#headerMemberCenter");
		var $headerLogout = $("#headerLogout");
		if($.cookie("loginMemberUsername") != null) {
			$headerLoginMemberUsername.text($.cookie("loginMemberUsername"));
			$headerMemberCenter.show();
			$headerLogout.show();
			$headerShowLoginWindow.hide();
			$headerShowRegisterWindow.hide();
		} else {
			$headerLoginMemberUsername.text("");
			$headerShowLoginWindow.show();
			$headerShowRegisterWindow.show();
			$headerMemberCenter.hide();
			$headerLogout.hide();
		}
	}
	
	$.flushHeaderInfo();
	
	// 商品搜索
	$productSearchKeyword = $("#productSearchKeyword");
	productSearchKeywordDefaultValue = $("#productSearchKeyword").val();
	$productSearchKeyword.focus( function() {
		if ($productSearchKeyword.val() == productSearchKeywordDefaultValue) {
			$productSearchKeyword.val("");
		}
	});
	
	$productSearchKeyword.blur( function() {
		if ($productSearchKeyword.val() == "") {
			$productSearchKeyword.val(productSearchKeywordDefaultValue);
		}
	});
	
	$("#productSearchForm").submit( function() {
		if ($.trim($productSearchKeyword.val()) == "" || $.trim($productSearchKeyword.val()) == productSearchKeywordDefaultValue) {
			return false;
		}
	});
	
	// 显示购物车信息
	var maxShowCartItemListCount = 30; // 最大购物车项数，为null则不限制
	var isAjaxDate = true;
	$(".showCartItemList").hover(
		function() {
			$("#cartItemListDetail").stop(true, true);
			$("#cartItemListDetail").fadeIn();
			if (isAjaxDate) {
				$.ajax({
					url: JFinalshop.base + "/shop/cartItem/ajaxList",
					dataType: "json",
					beforeSend: function() {
						$("#cartItemListDetail").html('<li><span class="loadingIcon">&nbsp;</span>正在加载...</li>');
					},
					success: function(data) {
						isAjaxDate = false;
						var cartItemInfoHtml = "";
						var cartItemListHtml = "";
						$.each(data, function(index, object) {
							if (index == 0) {
								if (object.totalQuantity == 0) {
									cartItemInfoHtml = '<li>您的购物车中暂无商品！</li>';
								} else {
									cartItemInfoHtml = '<li><span class="rightArea">共 ' + object.totalQuantity + ' 件 总计：<strong>' + object.totalPrice + '</strong></span></li>';
								}
							} else {
								cartItemListHtml = '<li><a class="leftArea" href="' + JFinalshop.base + object.htmlFilePath + '" title="' + object.name + '">' + object.name.substring(0, 12) + '</a><span class="rightArea"><strong>' + object.price + ' × ' + object.quantity + '</strong></span></li>' + cartItemListHtml;
							}
							if (maxShowCartItemListCount != null && index >= maxShowCartItemListCount) {
								return false;
							}
						});
						$("#cartItemListDetail").html(cartItemListHtml + cartItemInfoHtml);
					}
				})
			}
		},
		function() {
			$("#cartItemListDetail").fadeOut();
		}
	);
	
	$("#cartItemListDetail").hover(
		function() {
			$("#cartItemListDetail").stop(true, true);
			$("#cartItemListDetail").show();
		},
		function() {
			$("#cartItemListDetail").fadeOut();
		}
	)
	
	// 刷新购物车信息
	$.flushCartItemList = function() {
		isAjaxDate = true;
	}

});