package com.cargo.controller;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.cargo.document.AppResponse;
import com.cargo.report.PaymentReportBuilder;
import com.cargo.report.TransctionReportBuilder;
import com.cargo.report.VendorReportBuilder;
import com.cargo.service.ExportReportService;


@RestController
@RequestMapping("/backend")
public class ExportReportController {
	
	@Autowired
	private ExportReportService exportReportService;
	
	@Autowired 
	private VendorReportBuilder vendorReportBuilder;
	
	@RequestMapping(value = "/inventory" ,method = RequestMethod.GET)
	public ModelAndView exportPaymentReport(HttpServletResponse response){
		
		Map<String, Object> fileData = exportReportService.exportPaymentReport();
		 response.setContentType( "application/ms-excel" );
	     response.setHeader( "Content-disposition", "attachment; filename=myfile.xls" );
		return new ModelAndView(new PaymentReportBuilder(), fileData);
		
	}
	
	@PreAuthorize ("hasAuthority('ADMIN_INV')")
	@RequestMapping(value = "/transction/download" ,method = RequestMethod.GET)
	public ModelAndView exportTranasctionReport(@RequestParam Map<String, Object> map,HttpServletResponse response){
		
		Map<String, Object> fileData = exportReportService.exportTransctionReport(map);
		response.setContentType( "application/ms-excel" );
	    response.setHeader( "Content-disposition", "attachment; filename=TransactionReport.xls" );
		return new ModelAndView(new TransctionReportBuilder(), fileData);
		
	}
	
	@RequestMapping(value = "/report/vendor" ,method = RequestMethod.GET)
	public AppResponse<String> exporVendorReport(@RequestParam Map<String, Object> filters ){
		AppResponse<String> response = new AppResponse<>();
		vendorReportBuilder.generateVendorReport(filters);
		response.setSuccess(true);
		response.setMsg(Arrays.asList("Report has been send via email"));
		return response;
	}
	

}
