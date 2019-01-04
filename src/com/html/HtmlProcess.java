package com.html;

/**
 * @author fanxn19000
 * 用于提取jacoco的覆盖率文件，根据阈值提取分支覆盖率接口，并输出到excel
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlProcess {

	static String coverageFilePath = "C:\\Users\\hspcadmin\\Downloads\\ifs-dav_jacoco_HTML_Report\\ifs-dav_jacoco_HTML_Report";
	static String IntegerFilePath = "";

	// 获得链接内容
	/**
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 * 
	 *             jacoco覆盖率文件根据<a>标签进行连接，需要根据此标签逐层解析
	 */
	public List<String> getLinks(String filepath) throws IOException {
		File url = new File(filepath);
		Document doc = Jsoup.parse(url, "UTF-8");
		Elements links = doc.select("a[href]");
		Elements tds = doc.getElementsByAttributeValue("class", "ctr2");
		List<String> listText = new ArrayList();
		for (Element link : links) {
			listText.add(link.text());
		}
		return listText;
	}

	// 获得分支覆盖率
	/**
	 * 
	 * @param filepath
	 * @return
	 * @throws IOException
	 *             提取jacoco覆盖率文件的分支覆盖率
	 */
	public List<String> checkRate(String filepath) throws IOException {
		File url = new File(filepath);
		Document doc = Jsoup.parse(url, "UTF-8");
		List<String> list = new ArrayList<>();
		Elements elements = doc.select("tbody").select("tr").select("td");
		for (Element element : elements) {
			String id = element.id();
			//"e"是分支覆盖率的列号，其他覆盖率请自行替换
			if (id.contains("e")) {
				list.add(element.text());
			}
		}
		return list;
	}

	// 末次地址信息系拼接
	/**
	 * 
	 * @param path1
	 * @param name
	 * @return jacoco逐层解析，末次地址的组成方式
	 */
	public static String pathProcessLast(String path1, String name) {
		StringBuffer buffer = new StringBuffer(coverageFilePath);
		buffer.append(File.separator).append(path1).append(File.separator).append(name).append(".html");
		return buffer.toString();
	}

	// 中间地址信息系拼接
	/**
	 * 
	 * @param path1
	 * @param name
	 * @return jacoco逐层解析，非末次地址的组成方式
	 */
	public static String pathProcess(String path) {
		StringBuffer buffer = new StringBuffer(coverageFilePath);
		if (path == null) {
			buffer.append(File.separator).append("index.html");
		} else {
			buffer.append(File.separator).append(path).append(File.separator).append("index.html");
		}
		return buffer.toString();
	}

	// 地址中间值
	/**
	 * 
	 * @param orginPath
	 * @param path
	 * @return 拼接文件地址
	 */
	public static String pathAdd(String orginPath, String path) {
		StringBuffer buffer = new StringBuffer();
		if (orginPath == null) {
			buffer.append(path);
		} else {
			buffer.append(orginPath).append(File.separator).append(path);
		}
		return buffer.toString();
	}

	// 得到接口Map
	/**
	 * 
	 * @return Map<String, String>
	 * @throws IOException
	 *             得到接口与覆盖率对应关系数据
	 */
	public Map<String, String> getFuncMap() throws IOException {
		String filePath = "";
		filePath = pathProcess(null);
		// 第一层
		List<String> listText = getLinks(filePath);
		listText = listText.subList(1, listText.size() - 1);
		List<String> listPath = new ArrayList<>();
		String pathTemp = null;
		for (String text : listText) {
			pathTemp = pathAdd(null, text);
			listPath.add(pathTemp);
		}
		// 第二层
		List<String> listText1 = null;
		List<String> listPath1 = new ArrayList<>();
		for (String text : listPath) {
			filePath = pathProcess(text);
			listText1 = getLinks(filePath);
			listText1 = listText1.subList(2, listText1.size() - 1);
			for (String path : listText1) {
				pathTemp = pathAdd(text, path);
				listPath1.add(pathTemp);

			}
		}
		// 第三层
		List<String> listText2 = null;
		List<String> listPath2 = new ArrayList<>();
		for (String text : listPath1) {
			filePath = pathProcess(text);
			listText2 = getLinks(filePath);
			listText2 = listText2.subList(4, listText2.size() - 1);
			for (String text1 : listText2) {
				pathTemp = pathAdd(text, text1);
				listPath2.add(pathTemp);
			}
		}
		// 第四层
		List<String> listText3 = null;
		List<String> listPath3 = new ArrayList<>();
		List<String> listRate = new ArrayList<>();
		String pathTemp1 = "";
		Map<String, String> map = new LinkedHashMap<>();
		for (String text : listPath2) {
			String[] nameArray = text.split("\\\\");
			String name = nameArray[nameArray.length - 1];
			if (name.contains(".") || name.contains("(") || name.contains("{")) {
				continue;
			} else {
				pathTemp1 = text.substring(0, text.length() - name.length() - 1);
				filePath = pathProcessLast(pathTemp1, name);
				listText3 = getLinks(filePath);
				listText3 = listText3.subList(4, listText3.size() - 1);
				listRate = checkRate(filePath);
				int ll = listText3.size();
				int l = listRate.size();
				for (int index = 0; index < listText3.size(); index++) {
					String text1 = listText3.get(index);
					
//					if (text1.contains("function") || text1.contains("func_") || text1.contains("func")) {
//						pathTemp = pathAdd(text, text1);
//						listPath3.add(pathTemp);
//						map.put(pathTemp, listRate.get(index));
//					}
					if (true) {
						pathTemp = pathAdd(text, text1);
						listPath3.add(pathTemp);
						map.put(pathTemp, listRate.get(index));
					}
				}

			}
		}
		// 剔除覆盖率为N/A项
		Map<String, String> maptemp = new LinkedHashMap<>();
		maptemp.putAll(map);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue().equals("n/a")) {
				maptemp.remove(entry.getKey());
			}
		}
		return maptemp;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * 
	 *             获得所有再覆盖率文件中的接口名称
	 */
	public Map<String, String> getFuncList() throws IOException {
		List<String> listData = new ArrayList<>();
		Map<String, String> maptemp = getFuncMap();
		Map<String, String> mapData = new HashMap<>();
		for (Map.Entry<String, String> entry : maptemp.entrySet()) {
			String[] arr = entry.getKey().split("\\\\");
			arr = arr[arr.length - 1].split("\\(");
			mapData.put(arr[0], entry.getKey());
		}
		return mapData;
	}

	// 得到0覆盖率
	public Map<String, String> getZeroCover(Map<String, String> map) {
		Map<String, String> mapZero = new LinkedHashMap<>();
		mapZero.putAll(map);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (!entry.getValue().equals("0%")) {
				mapZero.remove(entry.getKey());
			}
		}
		return mapZero;
	}

	// 得到阈值覆盖率
	public Map<String, String> getLevelCover(Map<String, String> map, float value) {
		Map<String, String> mapSelect = new LinkedHashMap<>();
		mapSelect.putAll(map);
		for (Map.Entry<String, String> entry : map.entrySet()) {
			float tempValue = Float.parseFloat(entry.getValue().split("%")[0]);
			if (tempValue >= value) {
				mapSelect.remove(entry.getKey());
			}
		}
		return mapSelect;
	}

	// 覆盖率结果写入excel文件
	private void export(Map<String, String> map, String name) {
		HSSFWorkbook wb = new HSSFWorkbook();
		String excelHeader[] = { "接口路径", "分支覆盖率" };
		HSSFSheet sheet = wb.createSheet("接口列表");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		for (int i = 0; i < excelHeader.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(excelHeader[i]);
			cell.setCellStyle(style);
			sheet.autoSizeColumn(i);
		}
		int index = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
			row = sheet.createRow(index + 1);
			row.createCell(0).setCellValue(entry.getKey());
			row.createCell(1).setCellValue(entry.getValue());
			index++;
		}
		File file = new File(name + ".xls");
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			wb.write(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void exportAllCoverage() throws IOException {
		Map<String, String> totalData = getFuncMap();
		export(totalData, "所有接口分支覆盖率数据");
	}

	public void exportZeroCoverage() throws IOException {
		Map<String, String> totalData = getFuncMap();
		Map<String, String> data = getZeroCover(totalData);
		export(data, "分支覆盖率为0%的接口");
	}

	public void exportLevelCoverage(float value) throws IOException {
		Map<String, String> totalData = getFuncMap();
		Map<String, String> data = getLevelCover(totalData, value);
		export(data, "分支覆盖率低于" + value + "%的接口");
	}

	/**
	 * 
	 * 
	 * 解析接口文件，得到所有接口文档中的接口名称
	 * 
	 * @throws IOException
	 */
	public Map<String, String> getInterfaceName() throws IOException {
		List<String> list = new ArrayList<>();
		InputStream is = new FileInputStream(IntegerFilePath);
		if (IntegerFilePath.contains(".xlsx")) {
			XSSFWorkbook wb = new XSSFWorkbook(is);
			XSSFSheet sheet = null;
			boolean flag = false;
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				sheet = wb.getSheetAt(i);
				if (sheet.getSheetName().contains("接口列表")) {
					flag = true;
					break;
				}
			}
			if (flag == false) {
				System.out.println("没有在接口文档中找到'接口列表'sheet页，无法提取接口列表");
				return null;
			}
			XSSFRow xzzFRow = sheet.getRow(0);
			int index = 0;
			for (Cell cell : xzzFRow) {
				if (cell.getStringCellValue().contains("OpenAPI功能号")) {
					index = cell.getColumnIndex();
				}
			}
			int rowNum = sheet.getLastRowNum();
			for (int i = 1; i < rowNum; i++) {
				Cell cell = sheet.getRow(i).getCell(index);
				if (cell != null) {
					String value = cell.getStringCellValue();
					if (value != null && value != "" && value != " ") {
						list.add(value);
					}
				} else {
					break;
				}
			}
			is.close();
			wb.close();
		} else {
			HSSFWorkbook wb = new HSSFWorkbook(is);
			HSSFSheet sheet = null;
			boolean flag = false;
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				sheet = wb.getSheetAt(i);
				if (sheet.getSheetName().contains("接口列表")) {
					flag = true;
					break;
				}
			}
			if (flag == false) {
				System.out.println("没有在接口文档中找到'接口列表'sheet页，无法提取接口列表");
				return null;
			}
			HSSFRow hssfRow = sheet.getRow(0);
			int index = 0;
			for (Cell cell : hssfRow) {
				if (cell.getStringCellValue().contains("OpenAPI功能号")) {
					index = cell.getColumnIndex();
				}
			}
			int rowNum = sheet.getLastRowNum();
			for (int i = 1; i < rowNum; i++) {
				Cell cell = sheet.getRow(i).getCell(index);
				if (cell != null) {
					String value = cell.getStringCellValue();
					if (value != null && value != "" && value != " ") {
						list.add(value);
					}
				} else {
					break;
				}
			}
			is.close();
			wb.close();
		}
		// 去除接口文档中包含路径的情况
		Map<String, String> map = new HashMap<>();
		String value = null;
		for (String key : list) {
			if (key.contains("/")) {
				key = key.split("/")[1];
			}
			value = combainName(key);
			map.put(key, value);
		}
		return map;
	}

	/**
	 * 
	 * @param key
	 * @return String 名称组合
	 */
	private String combainName(String key) {
		String[] arr = key.split("_");
		String value = "";
		for (int i = 0; i < arr.length; i++) {
			String temp = arr[i];
			if (i == 0) {
				value += temp;
			} else {
				value += temp.substring(0, 1).toUpperCase() + temp.substring(1);
			}
		}
		return value;
	}

	/**
	 * 
	 * @param mapInterface
	 * @param mapCoverage
	 * @return List[]
	 * 
	 *         获得接口文档和代码中接口为差别 list[0] 为接口文档中存在代码中不存在的接口
	 *         list[1]为代码中存在但是接口文档中不存在的接口路径
	 */

	private List<String>[] getDiffData(Map<String, String> mapInterface, Map<String, String> mapCoverage) {
		List[] listArr = new List[2];
		listArr[0] = new ArrayList<String>();
		listArr[1] = new ArrayList<String>();
		for (Map.Entry<String, String> entry : mapInterface.entrySet()) {
			boolean interfaceFlag = false;
			if (mapCoverage.containsKey(entry.getKey()) || mapCoverage.containsKey(entry.getValue())) {
				interfaceFlag = true;
			}
			if (interfaceFlag == false) {
				listArr[0].add(entry.getKey());
			}
		}
		for (Map.Entry<String, String> entry : mapCoverage.entrySet()) {
			boolean covarageFlag = false;
			if (mapInterface.containsKey(entry.getKey()) || mapInterface.containsValue(entry.getKey())) {
				covarageFlag = true;
			}
			if (covarageFlag == false) {
				listArr[1].add(entry.getValue());
			}
		}
		return listArr;

	}

	public void exportDiff() throws IOException {
		List<String>[] list = getDiffData(getInterfaceName(), getFuncList());
		HSSFWorkbook wb = new HSSFWorkbook();
		String excelHeader[] = { "接口文档中存在，未在覆盖率文件中找到的接口列表" };
		HSSFSheet sheet = wb.createSheet("代码中缺失的接口");
		HSSFRow row = sheet.createRow((int) 0);
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		for (int i = 0; i < excelHeader.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(excelHeader[i]);
			cell.setCellStyle(style);
			sheet.autoSizeColumn(i);
		}
		List<String> list1 = list[0];
		for (int i = 0; i < list1.size(); i++) {
			row = sheet.createRow(i + 1);
			row.createCell(0).setCellValue(list1.get(i));
		}
		String excelHeader1[] = { "覆盖率文件中存在，接口文档中不存在的接口" };
		sheet = wb.createSheet("接口文档中缺失的接口");
		row = sheet.createRow((int) 0);
		for (int i = 0; i < excelHeader1.length; i++) {
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(excelHeader1[i]);
			cell.setCellStyle(style);
			sheet.autoSizeColumn(i);
		}
		List<String> list2 = list[1];
		for (int i = 0; i < list2.size(); i++) {
			row = sheet.createRow(i + 1);
			row.createCell(0).setCellValue(list2.get(i));
		}
		File file = new File("接口文档和覆盖率文件的差异.xls");
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			wb.write(outputStream);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws IOException {
		
//		HtmlProcess htmlProcess = new HtmlProcess();
//		// 差分参数
//		if (!(args.length >= 1)) {
//			System.out.println("请按照说明传入相应参数！");
//			return;
//		} else {
//			String parameter = args[0];
//			String[] paraArr = parameter.split(",");
//			// 覆盖率文件路径
//			htmlProcess.coverageFilePath = paraArr[0];
//			if (htmlProcess.coverageFilePath == null || "".equals(htmlProcess.coverageFilePath)) {
//				System.out.println("覆盖率文件路径不能为空");
//				return;
//			}
//			// 覆盖率的阈值
//			boolean flag = false;
//			if (paraArr.length >= 2) {
//				flag = true;
//				String level = paraArr[1].trim();
//				float coverageLevel = 0.0F;
//				if (level == null || "".equals(level)) {
//					htmlProcess.exportZeroCoverage();
//					System.out.println("生成覆盖率小于等于0的文件成功！");
//				} else {
//					coverageLevel = Float.parseFloat(level);
//					htmlProcess.exportLevelCoverage(coverageLevel);
//					System.out.println("生成覆盖率小于等于" + coverageLevel + "的文件成功！");
//				}
//			}
//
//			// 默认生成覆盖率为0的文件
//			if (!flag) {
//				htmlProcess.exportZeroCoverage();
//				System.out.println("生成覆盖率小于等于0的文件成功！");
//			}
//			// 接口文档文件路径
//			if (paraArr.length >= 3) {
//				htmlProcess.IntegerFilePath = paraArr[2];
//				if (htmlProcess.IntegerFilePath == null || "".equals(htmlProcess.IntegerFilePath)) {
//					// 不生成差异文件
//				} else {
//					htmlProcess.exportDiff();
//					System.out.println("生成差异文件成功！");
//				}
//			}
//		}
//		System.out.println("程序运行结束！");
		HtmlProcess htmlProcess = new HtmlProcess();
		htmlProcess.exportLevelCoverage(20F);
	}

}
