package com.um.myapp.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.um.model.EHealthRecord;
import com.um.util.DiagMedicineProcess;
import com.um.util.MedicineByDescription;

@Controller
public class CnMedicineStatisController {

	@RequestMapping(value="medicineProba",method=RequestMethod.GET)
	public ModelAndView cnMedicineStatis(String batch,String medicines){
		ModelAndView mv = new ModelAndView("cnmedicproba");
		if("".equals(medicines)){
			return mv;
		}
		// 1.1 读取数据库种病例数据
		// 1.2 选取批次
		List<EHealthRecord> eHealthRecordsByBatch = null; // 符合某一批次的全部病历
		
		if(batch.equals("null")){
			eHealthRecordsByBatch = MedicineByDescription.getAllRecords(); // 全部病历，不区分批次
		}else{
			eHealthRecordsByBatch = MedicineByDescription.getRecordsByBatch(batch);
		}
		
		if(eHealthRecordsByBatch == null || eHealthRecordsByBatch.size() == 0){
			return mv;
		}
		
		// 1.3 统计中药
		Map<String, String> resultMap = DiagMedicineProcess.statisMedicProbability(medicines,eHealthRecordsByBatch);
		
		if(resultMap == null || resultMap.size() == 0){
			
			return mv;
		}
		
		Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		
		Set<String> keySet = resultMap.keySet();
		
		if(resultMap.size() == 1){
			// 一组中药（单一或者两种）
			for(String s : keySet){
				String valueString = resultMap.get(s);
				ArrayList<String> valueList = new ArrayList<String>();
				if(valueString.contains("%")){
					//两味中药
					String[] vStrings = valueString.split("%");
					String[] unionStrings = vStrings[0].split("\\|");
					String[] mixStrings = vStrings[1].split("\\|");
					valueList.add(unionStrings[0]); //并集
					valueList.add(unionStrings[1]);//并集百分比
					valueList.add(mixStrings[0]);//交集
					valueList.add(mixStrings[1]);//交集百分比
				}else{
					//一味中药
					String[] vs = valueString.split("\\|"); // 区分交集和并集
					valueList.add(vs[0]); // 并集
					valueList.add(vs[1]); // 并集百分比
//					descriptionList = DiagMedicineProcess.getDescriptionByMedicine(medicines, eHealthRecordsByBatch);
				}
				result.put(s, valueList);
			}
		}else{
			//多味中药
			for(String s : keySet){
				String valueString = resultMap.get(s);
				ArrayList<String> valueList = new ArrayList<String>();
				String[] vs = valueString.split("%"); // 区分交集和并集
				valueList.add(vs[0].split("\\|")[0]); // 并集
				valueList.add(vs[0].split("\\|")[1]); // 并集百分比
				valueList.add(vs[1].split("\\|")[0]); // 交集
				valueList.add(vs[1].split("\\|")[1]); //交集百分比
				
				result.put(s, valueList);
			}
		}
		
		//中药交集的症状描述统计
		List<EHealthRecord> eHealthRecordsWithSameMedicines = DiagMedicineProcess.getEhealthRecordsByCMNames(medicines, eHealthRecordsByBatch);
		List<String> descriptionWithSameMedicines = DiagMedicineProcess.getDescriptionWithSameMedicines(eHealthRecordsWithSameMedicines);
		
		
		mv.addObject("results", result);
		mv.addObject("medicines", medicines);
		mv.addObject("descriptionlist", descriptionWithSameMedicines);
		return mv;
	}
}
