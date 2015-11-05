//处理企业名录中日期为年，没有月，日的时间类型
$().ready(function(){
	//提交表单的方法
	$("#submits").click(function(){		
		$.get("/MassPick/collect/batch/import?objName="+encodeURI(encodeURI($("input[name='objName']").val())),null,function(result){
			if(result)
			{
				if(result.objId == 0)
				{
					alert('你输入的文件不存在，请填写正确的文件路径!');
				}
				else if(result.objId == 2)
				{
					alert('你没有填写文件路径，请填写正确的文件路径!');
				}
				else
				{
					alert('导入成功!');
				}
			}
		});		
		
	});	
	
});