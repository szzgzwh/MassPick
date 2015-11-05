package cn.com.szgao.action;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import cn.com.szgao.dto.ObjectVO;
import cn.com.szgao.service.api.ICollectDataService;

/**
 * @author xcy
 * 搜集数据控制层
 * @version v1.0  RequestBody    @ModelAttribute
 * @since 2014-12-16
 */

@Controller
@RequestMapping("/collect") 
public class CollectionDataAction {	
	/**
	 * 注入服务层对象
	 * 作用：注入服务层对象
	 * 2015-2-11
	 * @author xiongchangyi
	 */
	@Resource(name="collectionServiceProxy")
	private ICollectDataService iCollectDataService;	

	@ResponseBody
	@RequestMapping(value="/save",method= RequestMethod.GET)
	public ObjectVO save(ObjectVO objVO)throws Exception{		
		if(null != objVO)
		{
			return iCollectDataService.createDocument(objVO);
		}	
		return null;
	}
	
	/**
	 * 处理企业名录的日期
	 * @param vo 文档路径
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/updateDate",method = RequestMethod.GET)
	public ObjectVO updateDate(ObjectVO vo)throws Exception{
		if(null != vo)
		{
			return iCollectDataService.updateDate(vo);
		}	
		return null;
	}
	/**
	 * 批量处理Excel文件导入购置工商数据
	 * @author xiongchangyi
	 * @since 2015-3-13
	 * throws Exception
	 * @param objVO
	 * @return 
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/batch/import",method = RequestMethod.GET)
	public ObjectVO batchImportExcelFile(ObjectVO objVO)throws Exception{
		if(null != objVO)
		{
			return iCollectDataService.batchImportExcelFile(objVO);
		}
		return null;
	}
	/**
	 * 批量导入地方法院数据
	 * @param objVO
	 * @return
	 * @throws Exception
	 */
	public ObjectVO batchImportCourtExcelFile(ObjectVO objVO)throws Exception{
		
		
		
		return null;
	}
}
