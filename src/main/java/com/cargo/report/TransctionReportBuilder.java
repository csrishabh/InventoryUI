package com.cargo.report;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.Document;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.cargo.document.Transction;
import com.cargo.utility.Config;

public class TransctionReportBuilder extends AbstractXlsxView {

	@Override
	protected void buildExcelDocument(Map<String, Object> data, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		List<Document> transctions = (List<Document>) data.get("transactions");
		response.setHeader("Content-Disposition", "attachment; filename=\"my-xlsx-file.xlsx\"");

		Sheet sheet = workbook.createSheet("Transctions");
		sheet.setDefaultColumnWidth(20);

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

		Row header = sheet.createRow(1);

		header.createCell(0).setCellValue("Date");
		header.getCell(0).setCellStyle(style);

		header.createCell(1).setCellValue("Product");
		header.getCell(1).setCellStyle(style);

		header.createCell(2).setCellValue("Quantity");
		header.getCell(2).setCellStyle(style);

		header.createCell(3).setCellValue("Type");
		header.getCell(3).setCellStyle(style);

		header.createCell(4).setCellValue("Added By");
		header.getCell(4).setCellStyle(style);
		
		header.createCell(5).setCellValue("Amount");
		header.getCell(5).setCellStyle(style);
		
		header.createCell(6).setCellValue("Last Price");
		header.getCell(6).setCellStyle(style);
		
		header.createCell(7).setCellValue("Value");
		header.getCell(7).setCellStyle(style);
		
		header.createCell(8).setCellValue("Update Date");
		header.getCell(8).setCellStyle(style);
		
		

		int rowCount = 2;

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Double in = 0.0;
		Double out = 0.0;
		for (Document trns : transctions) {
			Row aRow = sheet.createRow(rowCount++);
			aRow.createCell(0).setCellValue(formatter.format(trns.getDate("date")));
			aRow.createCell(1).setCellValue(trns.getString("prdName"));
			aRow.createCell(2).setCellValue(trns.getDouble("qty")+"-"+trns.getString("prdUnit"));
			aRow.createCell(3).setCellValue(trns.getString("type"));
			aRow.createCell(4).setCellValue(trns.getString("addBy"));
			if(trns.getString("type").equals("ADD")) {
				aRow.createCell(5).setCellValue(trns.getDouble("amount"));
				in = in+trns.getDouble("amount");
			}
			Double lastPrice = Double.parseDouble(trns.get("lastPriceBack").toString())/100;
			aRow.createCell(6).setCellValue(lastPrice);
			if(trns.getString("type").equals("DISPATCH")) {
				aRow.createCell(7).setCellValue(trns.getDouble("qty")*lastPrice);
				out = out + trns.getDouble("qty")*lastPrice;
			}
			
			if(null!=trns.getDate("lstAdtDate")) {
			aRow.createCell(8).setCellValue(formatter.format(trns.getDate("lstAdtDate")));
			}
			
		}
		sheet.createRow(rowCount++);
		Row totalRow = sheet.createRow(rowCount++);
		totalRow.createCell(0).setCellValue("Total Add Value");
		totalRow.createCell(1).setCellValue(in);
		totalRow.createCell(2).setCellValue("Total Dispatch Value");
		totalRow.createCell(3).setCellValue(out);
	}

}
