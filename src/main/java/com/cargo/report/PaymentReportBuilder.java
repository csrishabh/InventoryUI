package com.cargo.report;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.cargo.document.Product;
import com.cargo.utility.Config;

public class PaymentReportBuilder extends AbstractXlsxView{
	
	
	

	@Override
	protected void buildExcelDocument(Map<String, Object> data, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		List<Product> products = (List<Product>) data.get("products");
		response.setHeader("Content-Disposition", "attachment; filename=\"my-xlsx-file.xlsx\"");
		// create a new Excel sheet
        Sheet sheet = workbook.createSheet("Inventory Sheet");
        sheet.setDefaultColumnWidth(20);
        
        //for cell formatting
        CreationHelper createHelper = workbook.getCreationHelper();
        
     // create style for header cells
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(HSSFColor.BLUE.index);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);
        
        
        CellStyle redRow = workbook.createCellStyle();
        redRow.setFillPattern(CellStyle.SOLID_FOREGROUND);
        redRow.setFillForegroundColor(HSSFColor.RED.index);
        redRow.setBorderRight((short) 1);
        redRow.setRightBorderColor(IndexedColors.BLACK.getIndex());
        redRow.setBorderLeft((short) 1);
        redRow.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        redRow.setBorderTop((short) 1);
        redRow.setTopBorderColor(IndexedColors.BLACK.getIndex());
        redRow.setBorderBottom((short) 1);
        redRow.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        
        // create header row
        Row header = sheet.createRow(1);
         
        header.createCell(0).setCellValue("Name");
        header.getCell(0).setCellStyle(style);
         
        header.createCell(1).setCellValue("Quantity");
        header.getCell(1).setCellStyle(style);
         
        header.createCell(2).setCellValue("Min Quantity");
        header.getCell(2).setCellStyle(style);
           
        // create data rows
        int rowCount = 2;
        
        for (Product product : products) {
            Row aRow = sheet.createRow(rowCount++);
            aRow.createCell(0).setCellValue(product.getName().toUpperCase());
            aRow.createCell(1).setCellValue(Config.format(product.getQtyAblBack(), Config.QTY_FORMATTER));
            aRow.createCell(2).setCellValue(Config.format(product.getAlertBack(), Config.QTY_FORMATTER));
            if(product.getQtyAblBack()< product.getAlertBack()) {
            	aRow.getCell(0).setCellStyle(redRow);
            	aRow.getCell(1).setCellStyle(redRow);
            	aRow.getCell(2).setCellStyle(redRow);
            }
        }
    }

}
