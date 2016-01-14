/***
 *	JFinalShop Friend Link JavaScript
 *
 *	http://www.jfinalshop.com
 *
 *	Copyright (c) 2014 JFinalShop. All rights reserved.
 **/

$().ready( function() {

	$(".pictureFriendLink .scrollable").scrollable({
		loop: true,
		speed: 1000
	});
	
	$(".pictureFriendLink .scrollable a").hover(
		function() {
			$(this).stop().animate({"opacity": 1});
		}, function() {
			$(this).stop().animate({"opacity": 0.5});
		}
	).animate({"opacity": 0.5 });

});