package com.um.myapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.um.dao.ConnectionDB;
import com.um.dao.DataBaseBean;
import com.um.data.DataBaseSetting;
import com.um.model.EHealthRecord;
import com.um.util.DiagMedicineProcess;
import com.um.util.EhealthUtil;
import com.um.util.MedicineByDescription;

@Controller
public class QueryController {
	
	/**
	 * Query all record based on the batch and patient's name
	 * @param batch
	 * @param pname
	 * @return
	 */
	@RequestMapping(value="recordquery",method=RequestMethod.GET)
	public ModelAndView queryRecord(String batch, String pname){
		if( pname == "" || pname.equals("")){
			List<String> batchList = DiagMedicineProcess.getBatchString();
        	return new ModelAndView("dquery").addObject("batchList", batchList);
		}
		
		// get batch records
		List<EHealthRecord> eHealthRecordsByBatch = MedicineByDescription.getRecordsByBatch(batch);
		// get records by name
		List<EHealthRecord> ehealthList = new ArrayList<EHealthRecord>();
		for (EHealthRecord e : eHealthRecordsByBatch) {
			if (e.getPatientInfo() == null) {
				continue;
			}
			if (e.getPatientInfo().getName().equals(pname.trim())) {
				ehealthList.add(e);
			}
		}
		
		ModelAndView mv = new ModelAndView("recordQuery");
        
		if(ehealthList != null && ehealthList.size() > 0){
    	   mv.addObject("ehealthrecrods", ehealthList);
		}
		return mv;
	}
	
	/**
	 * Get the detail information of one patient
	 * @param ehealthregno
	 * @return
	 */
	@RequestMapping(value="detailRecord",method=RequestMethod.GET)
	public ModelAndView detailRecrod(String ehealthregno){
		
		if ("".equals(ehealthregno)) {
			List<String> batchList = DiagMedicineProcess.getBatchString();
        	return new ModelAndView("dquery").addObject("batchList", batchList);
		}
		
		EHealthRecord eHealthRecord = null;
        ModelAndView mv = null;
        
        // all record data
     	ApplicationContext context = new AnnotationConfigApplicationContext(DataBaseBean.class);
     	DataBaseBean dataBaseBean = (DataBaseBean)context.getBean("dataBaseBean");
     	
     	for (EHealthRecord e : dataBaseBean.geteHealthRecords()) {
     		if (e.getRegistrationno().equals(ehealthregno)) {
     			eHealthRecord = e;
     		}
     	}
     			
     	if( eHealthRecord != null){
     		mv =  new ModelAndView("detail");
                 	
//     		Map<String, HashMap<String, String>> conditionMap = DiagMedicineProcess.getConditionDescription(eHealthRecord);
            mv.addObject("ehealthrecordss",eHealthRecord);
            mv.addObject("allCnMedicines", eHealthRecord.getChineseMedicines());
            mv.addObject("allWeMedicines", eHealthRecord.getWesternMedicines());
//            mv.addObject("conditions", conditionMap);
     	}else {
     		List<String> batchList = DiagMedicineProcess.getBatchString();
     		mv = new ModelAndView("dquery");
     		mv.addObject("batchList", batchList);
     	}
     			
     	return mv;
	}
	
	
	/**
	 * Edit the record information of one patient
	 * @param ehealthregno
	 * @return
	 */
	@RequestMapping(value="editRecord",method=RequestMethod.GET)
	public ModelAndView editRecrod(String ehealthregno){
		
		EHealthRecord eHealthRecord = null;
		if("".equals(ehealthregno)){
			List<String> batchList = DiagMedicineProcess.getBatchString();
        	return new ModelAndView("dquery").addObject("batchList", batchList);
		}else{
			
			Document conditions = new Document();
			conditions.append("ehealthrecord.registrationno",ehealthregno);
			
			eHealthRecord = EhealthUtil.getOneEhealthRecordByConditions(conditions);
			
			if(eHealthRecord != null){
            	ModelAndView mv = new ModelAndView("editRecord");
            	mv.addObject("ehealthrecordss",eHealthRecord);
            	mv.addObject("allCnMedicines", eHealthRecord.getChineseMedicines());
            	mv.addObject("allWeMedicines", eHealthRecord.getWesternMedicines());
            	return mv;
            }else{
            	List<String> batchList = DiagMedicineProcess.getBatchString();
	        	return new ModelAndView("dquery").addObject("batchList", batchList);
            }
		}
	}
	
	/**
	 * save info
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value="saveEditRecord",method=RequestMethod.POST)
	public ModelAndView saveEdit(HttpServletRequest request,HttpServletResponse response) throws IOException{
		
		String regnoString = request.getParameter("regno");
		String conditondesc = request.getParameter("conditondesc");
		String westrndiagString = request.getParameter("westerndiag");
		String chinesediagString = request.getParameter("chinesediag");
		
		if("".equals(regnoString)){
			List<String> batchList = DiagMedicineProcess.getBatchString();
        	return new ModelAndView("dquery").addObject("batchList", batchList);
		}else{
			
			MongoCollection<Document> collection = ConnectionDB.getCollections(DataBaseSetting.ehealthcollection);
			
			DBObject updateCondition=new BasicDBObject();  
	         
	        //where name='fox'  
	        updateCondition.put("ehealthrecord.registrationno",regnoString);  
	          
	        DBObject updatedValue=new BasicDBObject();  
	        updatedValue.put("ehealthrecord.conditionsdescribed", conditondesc);
	        updatedValue.put("ehealthrecord.diagnostics.westerndiagnostics", westrndiagString);
	        updatedValue.put("ehealthrecord.diagnostics.chinesediagnostics", chinesediagString);
	          
	        DBObject updateSetValue=new BasicDBObject("$set",updatedValue);  
	        /** 
	         * update insert_test set headers=3 and legs=4 where name='fox' 
	         * updateCondition:更新条件 
	         * updateSetValue:设置的新值 
	         */  
	        if(collection.updateOne(updateCondition, updateSetValue) != null){
	        	response.sendRedirect("detailRecord?ehealthregno="+regnoString);
	        }
	        List<String> batchList = DiagMedicineProcess.getBatchString();
        	return new ModelAndView("dquery").addObject("batchList", batchList);
		}
	}
}
