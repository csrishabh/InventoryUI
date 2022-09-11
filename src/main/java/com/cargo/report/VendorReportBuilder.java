package com.cargo.report;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.CaseSearchResult;
import com.cargo.document.CaseStatus;
import com.cargo.document.CrownDetail;
import com.cargo.document.CrownMapping;
import com.cargo.document.User;
import com.cargo.repo.UserRepository;
import com.cargo.service.CrownMappingService;
import com.cargo.service.EmailService;
import com.cargo.service.ExportReportService;
import com.cargo.utility.Config;

@Service
public class VendorReportBuilder {

	@Autowired
	private ExportReportService exportReportService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CrownMappingService crownMappingService;

	public void generateVendorReport(Map<String, Object> filters) {

		VendorReportSender sender = new VendorReportSender(filters);
		sender.start();
	}

	class VendorReportSender extends Thread {

		Map<String, Object> filters;

		public VendorReportSender(Map<String, Object> filters) {
			this.filters = filters;
		}

		@Override
		public void run() {
			String vendorId = (String) filters.get("vender");
			User vendor = userRepository.findById(vendorId).get();
			List<CaseSearchResult> results = exportReportService.exportVendorReport(filters);
			Workbook workbook = buildExcelDocument(results, vendor);
			try {
				emailService.sendVendorReport(workbook, vendor);
			} catch (MessagingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private Workbook buildExcelDocument(List<CaseSearchResult> results, User vendor) {

			Workbook workbook = new HSSFWorkbook();
			List<CaseSearchResult> cases = results;

			Sheet sheet = workbook.createSheet("Vendor Report");
			sheet.setDefaultColumnWidth(25);

			// create style for case cells
			CellStyle style = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setFontName("Arial");
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
			style.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setColor(HSSFColor.WHITE.index);
			style.setFont(font);

			// create style for cancel case cells
			CellStyle canceledStyle = workbook.createCellStyle();
			Font cancedFont = workbook.createFont();
			cancedFont.setFontName("Arial");
			canceledStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			canceledStyle.setFillForegroundColor(HSSFColor.BROWN.index);
			cancedFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cancedFont.setColor(HSSFColor.WHITE.index);
			canceledStyle.setFont(cancedFont);

			// create style for alrady Paid case cells
			CellStyle paidStyle = workbook.createCellStyle();
			Font paidFont = workbook.createFont();
			paidFont.setFontName("Arial");
			paidStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			paidStyle.setFillForegroundColor(HSSFColor.GOLD.index);
			paidFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			paidFont.setColor(HSSFColor.WHITE.index);
			paidStyle.setFont(paidFont);

			// create style for header cells
			CellStyle headerStyle = workbook.createCellStyle();
			Font headerFont = workbook.createFont();
			headerFont.setFontName("Arial");
			headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headerStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
			headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			headerFont.setColor(HSSFColor.WHITE.index);
			headerStyle.setFont(headerFont);

			int rowCount = 0;

			Row report = sheet.createRow(rowCount++);
			report.createCell(0).setCellValue(vendor.getFullname());
			report.createCell(1).setCellValue("");
			report.createCell(2).setCellValue((String) filters.get("updateDate1"));
			report.createCell(3).setCellValue((String) filters.get("updateDate2"));

			Row header = sheet.createRow(rowCount++);
			header.createCell(0).setCellValue("Booking Date");
			header.createCell(1).setCellValue("OPD NO.");
			header.createCell(2).setCellValue("Patient Name");
			header.createCell(3).setCellValue("Status");
			header.createCell(4).setCellValue("Sub Status");
			header.createCell(5).setCellValue("Remark");
			header.getCell(0).setCellStyle(headerStyle);
			header.getCell(1).setCellStyle(headerStyle);
			header.getCell(2).setCellStyle(headerStyle);
			header.getCell(3).setCellStyle(headerStyle);
			header.getCell(4).setCellStyle(headerStyle);
			header.getCell(5).setCellStyle(headerStyle);

			rowCount++;
			
			double total = 0;
			for (CaseSearchResult result : cases) {
				if (!result.getStatus().equals(CaseStatus.BOOKED) && !result.getStatus().equals(CaseStatus.INPROCESS)) {
					Row aRow = sheet.createRow(rowCount++);
					aRow.createCell(0).setCellValue(result.getBookingDate());
					aRow.createCell(1).setCellValue(result.getId());
					aRow.createCell(2).setCellValue(result.getPatientName());
					aRow.createCell(3).setCellValue(result.getStatus());
					aRow.createCell(4).setCellValue(result.getSubStatus());
					if (result.getStatus().equals(CaseStatus.CANCELED)) {
						aRow.getCell(0).setCellStyle(canceledStyle);
						aRow.getCell(1).setCellStyle(canceledStyle);
						aRow.getCell(2).setCellStyle(canceledStyle);
						aRow.getCell(3).setCellStyle(canceledStyle);
						aRow.getCell(4).setCellStyle(canceledStyle);
						aRow.createCell(5).setCellValue("CANCELED");
						aRow.getCell(5).setCellStyle(canceledStyle);
					} else if (result.isAlreadyPaid()) {
						aRow.getCell(0).setCellStyle(paidStyle);
						aRow.getCell(1).setCellStyle(paidStyle);
						aRow.getCell(2).setCellStyle(paidStyle);
						aRow.getCell(3).setCellStyle(paidStyle);
						aRow.getCell(4).setCellStyle(paidStyle);
						aRow.createCell(5).setCellValue("Already Paid");
						aRow.getCell(5).setCellStyle(paidStyle);
					} else {
						aRow.getCell(0).setCellStyle(style);
						aRow.getCell(1).setCellStyle(style);
						aRow.getCell(2).setCellStyle(style);
						aRow.getCell(3).setCellStyle(style);
						aRow.getCell(4).setCellStyle(style);
					}
					for (CrownDetail cd : result.getCrown().getDetails()) {
						Row cRow = sheet.createRow(rowCount++);
						cRow.createCell(0).setCellValue(cd.getType().getName());
						cRow.createCell(1).setCellValue(cd.getCrownNo());
						cRow.createCell(2).setCellValue(cd.getShade());
						CrownMapping mapping = crownMappingService.getCrownMapping(cd.getType(),vendor.getId(), cd.getVerison());
						int count = cd.getCrownNo().trim().split(",").length;
						double price = 0;
						if(mapping!=null) {
							price = Config.format(mapping.getPrice(),Config.PRICE_FORMATTER);
						}
						cRow.createCell(3).setCellValue(price);
						cRow.createCell(4).setCellValue(count);
						if(result.getStatus().equals(CaseStatus.CANCELED) || result.isAlreadyPaid()) {
							cRow.createCell(5).setCellValue(0);
						}
						else {
						cRow.createCell(5).setCellValue(price*count);
						total = total + (price*count);
						}
					}
					rowCount++;

				}
			}
			Row totalRow = sheet.createRow(rowCount++);
			totalRow.createCell(0).setCellValue("");
			totalRow.createCell(1).setCellValue("");
			totalRow.createCell(2).setCellValue("");
			totalRow.createCell(3).setCellValue("");
			totalRow.createCell(4).setCellValue("Total");
			totalRow.createCell(5).setCellValue(total);
			
			return workbook;
		}

	}

}
