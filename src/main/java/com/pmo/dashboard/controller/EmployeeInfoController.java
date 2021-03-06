package com.pmo.dashboard.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pmo.dashboard.entity.CSDept;
import com.pmo.dashboard.entity.EmployeeInfo;
import com.pmo.dashboard.entity.EmployeePageCondition;
import com.pmo.dashboard.entity.User;
import com.pmo.dashboard.util.Constants;
import com.pom.dashboard.service.CSDeptService;
import com.pom.dashboard.service.EmployeeInfoService;

@Controller
@RequestMapping(value="/employeeInfo")
public class EmployeeInfoController
{
    private static Logger logger = LoggerFactory
            .getLogger(EmployeeInfoController.class);
    
    @Resource
    private EmployeeInfoService employeeInfoService;
    
    @Resource
    private CSDeptService csDeptService;

    
    @RequestMapping("/queryEmployeeList")
    @ResponseBody
    public Object queryEmployeeList(final HttpServletRequest request,
            final HttpServletResponse response)
    {
        String pageState = request.getParameter("pageState");
        
        String hsbcStaffId = request.getParameter("hsbcStaffId");
        
        String eHr = request.getParameter("eHr");
        
        String lob = request.getParameter("lob");
        
        String csDeptName = request.getParameter("csDeptName");
        
        String csSubDeptName = request.getParameter("csSubDeptName");
        
        String csBuName = request.getParameter("csBuName");
        
        String resourceStatus = request.getParameter("resourceStatus");
        
        String staffName = request.getParameter("staffName");
        
        String rmUserId = request.getParameter("rmUserId");
        
        User user = (User) request.getSession().getAttribute("loginUser");
        
        List<String>  csSubDeptNames = new ArrayList<String>();   
        
    	List<CSDept> cSDepts= csDeptService.queryCSDeptByIds(user.getCsdeptId().split(","));
    	
    	if(cSDepts != null && !cSDepts.isEmpty()){        
    		for (CSDept csDept : cSDepts) {
        		csSubDeptNames.add(csDept.getCsSubDeptName());
			}
    	}        
        
        String userType = user.getUserType();
      	
    	String[] csBuNames = null;
    	if(user.getBu()!=null&&user.getBu()!=""){
    		csBuNames = user.getBu().split(",");
    	}

        String csSubDeptId = null;
       
        if(("".equals(csSubDeptName) || csSubDeptName == null) &&
                ("".equals(csBuName) || csBuName == null)){
            
            if("1".equals(userType)|| "2".equals(userType)|| "3".equals(userType)|| "4".equals(userType)){
                csBuName = csBuNames[0];
            }
            
            if("2".equals(userType)|| "3".equals(userType)|| "4".equals(userType)){
            	csSubDeptName = cSDepts.get(0).getCsSubDeptName();               	             
            }
            
        }	
        
        int countPage = 0;
        
        String currentPage = null;
        
        EmployeePageCondition employeePageCondition = new EmployeePageCondition();
        
        if("".equals(pageState) || pageState == null){
            currentPage = "0";
            employeePageCondition.setHsbcStaffId(hsbcStaffId);
            employeePageCondition.seteHr(eHr);
            employeePageCondition.setLob(lob);
            employeePageCondition.setCsSubDeptName(csSubDeptName);
            employeePageCondition.setCsDeptName(csDeptName);
            employeePageCondition.setCsbuName(csBuName);
            employeePageCondition.setCurrentPage(currentPage);
            employeePageCondition.setStaffName(staffName);
            employeePageCondition.setResourceStatus(resourceStatus);
            employeePageCondition.setRmUserId(rmUserId);
            countPage = employeeInfoService.countEmployeeList(employeePageCondition);
            employeePageCondition.setPageCount(countPage+"");
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("frist".equals(pageState)){
            currentPage = "0";
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("next".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = Integer.parseInt(employeePageCondition.getCurrentPage()) + Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("previous".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = Integer.parseInt(employeePageCondition.getCurrentPage()) - Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("last".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = (Integer.parseInt(employeePageCondition.getPageCount()) - Constants.ONE) * Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }
        
        List<EmployeeInfo> list = employeeInfoService.queryEmployeeList(employeePageCondition);
        Map<String,Object> result = new HashMap<String,Object>();
        
       // change csSubDeptName to csSubDeptId
     	List<CSDept> allCsDepts = csDeptService.queryAllCSDept();
     	if (employeePageCondition.getCsSubDeptName() != null && employeePageCondition.getCsSubDeptName() != "") {
     		for (CSDept csDept : allCsDepts) {
     			if (employeePageCondition.getCsSubDeptName().equals(csDept.getCsSubDeptName())) {
     				csSubDeptId = csDept.getCsSubDeptId();
     				break;
     			}
     		}
     	}
     	
     	result.put("csSubDeptName", csSubDeptName);
        result.put("csSubDeptId", csSubDeptId);
	    result.put("user", user);
        result.put("data", list);
        result.put("pageInfo", request.getSession().getAttribute("employeePageCondition"));
        result.put("csSubDeptNames", csSubDeptNames);
        result.put("csBuNames", csBuNames);
        return result;
    }
    
    
    @RequestMapping("/queryBatchEmployeeList")
    @ResponseBody
    public Object queryBatchEmployeeList(final HttpServletRequest request,
            final HttpServletResponse response)
    {
        String pageState = request.getParameter("pageState");
        
        String hsbcStaffId = request.getParameter("hsbcStaffId");
        
        String eHr = request.getParameter("eHr");
        
        String lob = request.getParameter("lob");
        
        String csDeptName = request.getParameter("csDeptName");
        
        String csSubDeptName = request.getParameter("csSubDeptName");
        
        String csBuName = request.getParameter("csBuName");
        
        String resourceStatus = request.getParameter("resourceStatus");
        
        String staffName = request.getParameter("staffName");
        
        String rmUserId = request.getParameter("rmUserId");
        
        User user = (User) request.getSession().getAttribute("loginUser");
        
        List<String>  csSubDeptNames = new ArrayList<String>();   
        
        List<CSDept> cSDepts= csDeptService.queryCSDeptByIds(user.getCsdeptId().split(","));
        
        if(cSDepts != null && !cSDepts.isEmpty()){        
            for (CSDept csDept : cSDepts) {
                csSubDeptNames.add(csDept.getCsSubDeptName());
            }
        }        
        
        
        String userType = user.getUserType();
        
        String[] csBuNames = null;
    	if(user.getBu()!=null&&user.getBu()!=""){
    		csBuNames = user.getBu().split(",");
    	}
       
    	String csSubDeptId = null;
    	
        if(("".equals(csSubDeptName) || csSubDeptName == null) &&
                ("".equals(csBuName) || csBuName == null)){
            
            if("1".equals(userType)|| "2".equals(userType)|| "3".equals(userType)|| "4".equals(userType)){
            	csBuName = csBuNames[0];
            }
            
            if("2".equals(userType)|| "3".equals(userType)|| "4".equals(userType)){
                csSubDeptName = cSDepts.get(0).getCsSubDeptName();              
            }
            
        }
        
        
        
        int countPage = 0;
        
        String currentPage = null;
        
        EmployeePageCondition employeePageCondition = new EmployeePageCondition();
        
        //RM登录只能查看到自己管理的员工
        if("3".equals(userType)){
            employeePageCondition.setRmUserId(user.getUserId());
        }
        
        if("".equals(pageState) || pageState == null){
            currentPage = "0";
            employeePageCondition.setHsbcStaffId(hsbcStaffId);
            employeePageCondition.seteHr(eHr);
            employeePageCondition.setLob(lob);
            employeePageCondition.setCsSubDeptName(csSubDeptName);
            employeePageCondition.setCsDeptName(csDeptName);
            employeePageCondition.setCsbuName(csBuName);
            employeePageCondition.setCurrentPage(currentPage);
            employeePageCondition.setStaffName(staffName);
            employeePageCondition.setResourceStatus(resourceStatus);
            if(!"3".equals(userType)){
                employeePageCondition.setRmUserId(rmUserId);
            }
            countPage = employeeInfoService.countEmployeeList(employeePageCondition);
            employeePageCondition.setPageCount(countPage+"");
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("frist".equals(pageState)){
            currentPage = "0";
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("next".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = Integer.parseInt(employeePageCondition.getCurrentPage()) + Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("previous".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = Integer.parseInt(employeePageCondition.getCurrentPage()) - Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }else if("last".equals(pageState)){
            employeePageCondition = (EmployeePageCondition) request.getSession().getAttribute("employeePageCondition");
            currentPage = (Integer.parseInt(employeePageCondition.getPageCount()) - Constants.ONE) * Constants.TEN +"";
            employeePageCondition.setCurrentPage(currentPage);
            request.getSession().setAttribute("employeePageCondition", employeePageCondition);
        }
        
        List<EmployeeInfo> list = employeeInfoService.queryEmployeeList(employeePageCondition);
        Map<String,Object> result = new HashMap<String,Object>();
        
        // change csSubDeptName to csSubDeptId
     	List<CSDept> allCsDepts = csDeptService.queryAllCSDept();
     	if (employeePageCondition.getCsSubDeptName() != null && employeePageCondition.getCsSubDeptName() != "") {
     		for (CSDept csDept : allCsDepts) {
     			if (employeePageCondition.getCsSubDeptName().equals(csDept.getCsSubDeptName())) {
     				csSubDeptId = csDept.getCsSubDeptId();
     				break;
     			}
     		}
     	}
        
        result.put("csSubDeptName", csSubDeptName);
        result.put("csSubDeptId", csSubDeptId);
        result.put("user", user);
        result.put("data", list);
        result.put("pageInfo", request.getSession().getAttribute("employeePageCondition"));
        result.put("csSubDeptNames", csSubDeptNames);
        result.put("csBuNames", csBuNames);
        return result;
    }
    
    
    
    @RequestMapping("/setEmpConditon")
    @ResponseBody
    public boolean setEmpConditon(final HttpServletRequest request,
            final HttpServletResponse response)
    {
       
        String condition = request.getParameter("condition");
        
        String[] excelCondition = condition.split(",");
        
        List<String> conditionList = Arrays.asList(excelCondition);
        
        request.getSession().setAttribute("conditionList", conditionList);
        
        boolean resultFlag = true;
        
        return resultFlag;
        
    }
    
}
