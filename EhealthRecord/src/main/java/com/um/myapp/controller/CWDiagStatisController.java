package com.um.myapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.um.classify.CWRelationMapping;
import com.um.classify.DiagnosticsClassify;
import com.um.data.DataBaseSetting;
import com.um.data.DiagClassifyData;
import com.um.model.EHealthRecord;

@Controller
public class CWDiagStatisController {
	
	@RequestMapping(value="cwdiagstatis",method=RequestMethod.GET)
	public ModelAndView cwDiagStatis() throws IOException{
		
		CWRelationMapping cwRelationMapping = new CWRelationMapping();
		/**
		 * 1. 读取病历信息
		 */
//		List<EHealthRecord> eHealthList = cwRelationMapping.queryEhealthData();
		List<EHealthRecord> eHealthList = cwRelationMapping.queryEhealthDataByCollection(DataBaseSetting.ehealthcollection);
		int totalCount = eHealthList.size();
		
		/**
		 * 2.诊断分类
		 */
		List<DiagnosticsClassify> chineseDiagnostics = cwRelationMapping.createDiagnostics(DiagClassifyData.cnDiagClassify);
		List<DiagnosticsClassify> westernDiagnostics = cwRelationMapping.createDiagnostics(DiagClassifyData.wnDiagClassify);
		
		/**
		 * 3. 对病历进行分类处理
		 * 		3.1 中医诊断分类
		 *      3.2 西医诊断分类
		 */
		
		cwRelationMapping.chineseDiagnosticsClassify(eHealthList,chineseDiagnostics);//中医诊断分类		
		cwRelationMapping.westernDiagnosticsClassify(eHealthList, westernDiagnostics);//西医诊断分类
		
		/**
		 *  4. 对中西医诊断mapping统计
		 */
		
		List<String> classStatisList = new ArrayList<String>();
		
		int cnLen = chineseDiagnostics.size(); //中医诊断类型数量
		
		for(int i = 0; i < cnLen; i++){
			DiagnosticsClassify cndiag = chineseDiagnostics.get(i);
			if(cndiag == null || cndiag.geteHealthRecords() == null || cndiag.geteHealthRecords().size() == 0){
				continue;
			}
			int lenRecord = cndiag.geteHealthRecords().size(); // 每一个中医诊断类型的病例数
			for(int j = 0; j < lenRecord; j++){
				EHealthRecord eRecord = cndiag.geteHealthRecords().get(j); 
				int wnLen = westernDiagnostics.size(); // 西医诊断类型的数量
				for(int k = 0; k < wnLen; k++){
					DiagnosticsClassify wndiag = westernDiagnostics.get(k);
					if(wndiag == null || wndiag.geteHealthRecords() == null || wndiag.geteHealthRecords().size() == 0){
						continue;
					}
					int lenWnR = wndiag.geteHealthRecords().size(); // 西医诊断类型的病历数
					for(int m = 0; m < lenWnR; m++){
						if(eRecord.getRegistrationno() == wndiag.geteHealthRecords().get(m).getRegistrationno()){
							classStatisList.add("C" + (i+1) + "|" + "W" + (k+1)); 
						}
					}
				}
			}
		}
		
		Map<String, Integer> classMap = new HashMap<String, Integer>(); //分类统计
		
		List<String> copyOfclassStatisList = cwRelationMapping.copyList(classStatisList);
		
		classMap.put(copyOfclassStatisList.get(0).trim(), 1);
		
		for(int i = 0; i < copyOfclassStatisList.size(); i++){
			int count = 1;
			for(int j=i+1; j<copyOfclassStatisList.size();j++){
				if(copyOfclassStatisList.get(j).equals(copyOfclassStatisList.get(i))){
					//重复
					count++;
					copyOfclassStatisList.remove(j);
					j--; // 去掉之后，从当前位置继续
				}
			}
			classMap.put(copyOfclassStatisList.get(i), count);
		}
		
		ModelAndView mv = new ModelAndView("diagstatis");
		mv.addObject("chineseDiagnostics", chineseDiagnostics);
		mv.addObject("westernDiagnostics", westernDiagnostics);
		mv.addObject("classMapping", classMap);
		mv.addObject("count",totalCount);
		return mv;
	}
}
