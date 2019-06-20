import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonFileCreter {
	private static final String FILE_PATH = "/Users/anirudh/Projects/JCGExamples/JavaWriteReadExcelFileExample/testReadStudents.xlsx";

	public static void main(String args[]) {

	}

	private static List readClientProperty() {
		List studentList = new ArrayList();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(FILE_PATH);

			// Using XSSF for xlsx format, for xls use HSSF
			Workbook workbook = new XSSFWorkbook(fis);

			int numberOfSheets = workbook.getNumberOfSheets();

			// looping over each workbook sheet
			for (int i = 0; i < numberOfSheets; i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Iterator rowIterator = sheet.iterator();

				// iterating over each row
				while (rowIterator.hasNext()) {

					Row row = (Row) rowIterator.next();

					Cell _json_key = row.getCell(0);
					Cell _format_type = row.getCell(1);
					Cell _key_size = row.getCell(2);
					Cell _key_mandatory = row.getCell(3);

					String keyName = _json_key.getStringCellValue();
					String keyType = _format_type.getStringCellValue();
					Integer keyLength = !_json_key.getStringCellValue().trim().equals("N/A")
							? Integer.parseInt(_json_key.getStringCellValue().trim()) : 0;
					Boolean keyMandate = _json_key.getStringCellValue().trim().equals("yes");
					
					checkMandateKey(keyName, keyMandate);
					checkLength(keyName, keyLength);
					checkFormat(keyName, keyType);
					
				}
			}

			fis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return studentList;
	}

	private static void checkFormat(String keyName, String keyType) {
		// TODO Auto-generated method stub
		
	}

	private static void checkLength(String keyName, Integer keyLength) {
		// TODO Auto-generated method stub
		
	}

	private static void checkMandateKey(String keyName, Boolean keyMandate) {
		// TODO Auto-generated method stub
		
	}

}
