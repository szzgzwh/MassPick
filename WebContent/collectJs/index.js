//改文件是首页的js文件
$().ready(function(){	
	//提交表单的方法
	$("#submits").click(function(){
		//对象ID
		//var id = $("input[name='objId']").val();
		//对象name
		var name = $("input[name='objName']").val();
		//对象备注
		//var remark = $("input[name='mark']").val();
		//以上3个变量组成的JSON对象
		//var param = {"objId":id,"objName":name,"mark":remark};	
		//$("#frm").serialize();
		$.get("/MassPick/collect/save?objName="+encodeURI(encodeURI(name)),null,function(result){
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
		
		/*$.ajax({
			type:"POST", 
			url:'/MassPick/collect/save',
			data:JSON.stringify({"objId":id,"objName":name,"mark":remark}),
		    dataType:"json",
		    contentType:'application/json;charset=UTF-8',
		    success : function(data)
		    {	
		    	console.log(data);
		        alert('success');   
		   
		    },
		    error:function(e)
		    {
		    	alert("err");   
		    }   
		});*/
	});	
});