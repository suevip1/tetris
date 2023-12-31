package com.suma.venus.alarmoprlog.service.alarm;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class InitAlarmInfoService {

	private static final Logger LOGGER = LoggerFactory.getLogger(InitAlarmInfoService.class);

	// public static final String separator = File.separator;

	@Autowired
	private AlarmInfoService alarmInfoService;

	public void initAlarmInfo() {

		ClassPathResource classPathResource = new ClassPathResource("init/alarmInfo.xlsx");

		try {
			InputStream inputStream = classPathResource.getInputStream();
			alarmInfoService.importAlarmInfoExcel(inputStream);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOGGER.error("init alarm exception");
			e1.printStackTrace();

		}

		// 初始化，导入告警基本信息excel
		/*
		 * String dir = "alarmFile" + separator;
		 * 
		 * File dirObj = new File(dir);
		 * 
		 * System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" +
		 * dirObj.getAbsolutePath()); File[] files = new File(dir).listFiles();
		 * 
		 * if (files == null || files.length == 0) { return; }
		 * 
		 * for (File file : files) { if (file.isFile() && file.exists() &&
		 * file.getName().startsWith("alarm")) {
		 * 
		 * try { InputStream is = new FileInputStream(file);
		 * 
		 * alarmInfoService.importAlarmInfoExcel(is);
		 * 
		 * } catch (FileNotFoundException e) { // TODO Auto-generated catch block } } }
		 */
	}
}