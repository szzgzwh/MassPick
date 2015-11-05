package cn.com.szgao.text;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import cn.com.szgao.dto.ArchivesVO;
import cn.com.szgao.service.api.ICollectDataService;
import cn.com.szgao.service.impl.CollectDataService;

public class SingleUpload {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File filepor=new File("E:\\Company_File\\log4j\\batchImport.log");
		if(filepor.exists()){
			filepor.delete();			
		}
		PropertyConfigurator.configure("F:\\WorkSpace_Eclipse\\MassPick\\WebContent\\WEB-INF\\log4j.properties"); 
		
		ICollectDataService service=new CollectDataService();
		List<ArchivesVO> archList=new ArrayList<ArchivesVO>();
		//UUID
		String uuid="488dd772-6413-570d-82c2-5701da2ade0e";
		//标题
		String title="中国人民财产保险股份有限公司赣县支公司与丁振优保险合同纠纷案";
		//法院名称
		String courtName="江西省赣州市中级人民法院";
		//文书类型
		String suitType="民事判决书";
		//案号
		String caseNum="（2006）赣中民二终字第145号";
		//原告
		String plaintiff="中国人民财产保险股份有限公司赣县支公司、卢明飞、张鹏飞、梁向阳";
		//被告
		String defendant="丁振优、张鹏飞";
		//审批结论
		String approval="驳回上诉，维持原判。二审案件受理费1770元由上诉人承担。";
		
		//审结日期
		String approvalDate="二○○六年十一月二十二日";
		//案由
		String caseCause="中国人民财产保险股份有限公司赣县支公司与丁振优保险合同纠纷案";
		
		
		ArchivesVO arch=new ArchivesVO();
		arch.setUuid(uuid);
		arch.setPlaintiff(plaintiff);
		arch.setDefendant(defendant);
		arch.setApproval(approval);
		arch.setSuitType(suitType);
		arch.setApprovalDate(approvalDate);
		arch.setCaseCause(caseCause);
		arch.setTitle(title);
		arch.setCourtName(courtName);
		arch.setCaseNum(caseNum);
		archList.add(arch);
				
		service.updateJsonSingleData(archList);

	}

}