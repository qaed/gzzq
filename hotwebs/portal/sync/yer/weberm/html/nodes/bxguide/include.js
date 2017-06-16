
$(document).ready(function() {
	var height = $(window).height();
	var width = $(window).width();
	$('info_table_div').css("height", height);
	$('info_table_div').css("width", width);

	$('topinfo_table_div').css("height", height);
	$('topinfo_table_div').css("width", width);
	
	insertphoto();
})
	

function insertphoto() {
	var width = $(window).width()*0.8;
	document.getElementById("insertphoto").innerHTML='<img src="images/bxguide/bxguide.jpg" width="' + width + '"/>';
}
