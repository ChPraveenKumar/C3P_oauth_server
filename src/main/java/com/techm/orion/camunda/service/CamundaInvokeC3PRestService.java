package com.techm.orion.camunda.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.techm.orion.camunda.common.DBUrlSingleton;
import com.techm.orion.camunda.common.LoggerUtil;

public class CamundaInvokeC3PRestService {
	private static String endpointUrl = DBUrlSingleton.getInstance().getEndpointUrl();
	private static final Logger logger = LoggerUtil.getApplicationLogger(CamundaInvokeC3PRestService.class);

	private static final String PERFORM_REACHABILTY_TEST = endpointUrl
			+ "/C3P/DeviceReachabilityAndPreValidationTest/performReachabiltyTest";
	private static final String DELIVER_CONFIGURATION_TEST = endpointUrl
			+ "/C3P/DeliverConfigurationAndBackupTest/deliverConfigurationTest";
	private static final String NETWORK_COMMAND_TEST = endpointUrl + "/C3P/NetworkTestValidation/networkCommandTest";
	private static final String HEALTHCHECK_COMMAND_TEST = endpointUrl
			+ "/C3P/HealthCheckTestValidation/healthcheckCommandTest";
	private static final String OTHERSCHECK_COMMAND_TEST = endpointUrl
			+ "/C3P/OthersCheckTestValidation/otherscheckCommandTest";
	private static final String NETWORK_AUDIT_COMMAND_TEST = endpointUrl
			+ "/C3P/NetworkAuditTest/networkAuditCommandTest";
	private static final String FINAL_REPORT_CREATION = endpointUrl + "/C3P/FinalReportForTTUTest/finalReportCreation";
	private static final String HEALTH_CHECK = endpointUrl + "/C3P/OsUpgrade/HealthCheck";
	private static final String SELECT_REQUEST_IN_DB = endpointUrl
			+ "/C3P/CreateScheduleReqDBService/selectRequestInDB";
	private static final String INSERT_REQUEST_IN_DB = endpointUrl
			+ "/C3P/CreateScheduleReqDBService/insertRequestInDB";
	private static final String UPDATE_REQUEST_IN_DB = endpointUrl + "/C3P/CreateScheduleReqDBService/updateTaskIDInDB";
	private static final String PERFORM_PREVALIDATE_TEST = endpointUrl
			+ "/C3P/DeviceReachabilityAndPreValidationTest/performPrevalidateTest";
	private static final String PREVALIDATE_ODL = endpointUrl + "/C3P/vnfservices/prevalidateODL";

	public String checkDeviceReachability(String businessKey, String version) {
		logger.info("Inside checkDeviceReachability method");
		return executeBpmProcess(PERFORM_REACHABILTY_TEST, businessKey, version);
	}

	public String checkRequestType(String businessKey) {	
		String outputVar = "false";
		logger.info("Inside CheckRequestType method");
		if(businessKey !=null && businessKey.contains("SLGC-")) {
        	outputVar="true";
        }       
		return outputVar;
	}

	public String deliverConfiguration(String businessKey, String version) {
		logger.info("Inside deliverConfiguration method");
		String reqType = null;
		if (businessKey != null && businessKey.length() > 3) {
			reqType = businessKey.substring(0, Math.min(businessKey.length(), 4));
		} else {
			reqType = "SLGC";
		}
		return executeBpmProcess(DELIVER_CONFIGURATION_TEST, businessKey, version, reqType);
	}

	public String validateNetworkTest(String businessKey, String version) {
		logger.info("Inside validateNetworkTest method");
		return executeBpmProcess(NETWORK_COMMAND_TEST, businessKey, version);
	}

	public String validateHealthCheckTest(String businessKey, String version) {
		logger.info("Inside validateHealthCheckTest method");
		return executeBpmProcess(HEALTHCHECK_COMMAND_TEST, businessKey, version);
	}

	public String validateOthersCheckTest(String businessKey, String version) {
		logger.info("Inside validateOthersCheckTest method");
		return executeBpmProcess(OTHERSCHECK_COMMAND_TEST, businessKey, version);
	}

	public String validateNetworkAudit(String businessKey, String version) {
		logger.info("Inside validateNetworkAudit method");
		return executeBpmProcess(NETWORK_AUDIT_COMMAND_TEST, businessKey, version);
	}

	public String certifyReportforTTUTest(String businessKey, String version) {
		logger.info("Inside certifyReportforTTUTest method");
		return executeBpmProcess(FINAL_REPORT_CREATION, businessKey, version);
	}

	public String postUpgradeHealthCheck(String businessKey, String version) {
		logger.info("Inside PostUpgradeHealthCheck method");
		return executeBpmProcess(HEALTH_CHECK, businessKey, version);
	}

	public String selectRequestInDB(String businessKey, String version) {
		logger.info("Inside selectRequestInDB method");
		return executeBpmProcess(SELECT_REQUEST_IN_DB, businessKey, version);
	}

	@SuppressWarnings("unchecked")
	public void insertRequestInDB(String businessKey, String version, String processId, String user) {
		logger.info("Inside insertRequestInDB method");
		logger.info("businessKey " + businessKey);
		logger.info("version " + version);
		logger.info("requestType " + processId);
		logger.info("user " + user);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("requestId", businessKey);
		jsonObj.put("version", version);
		jsonObj.put("processId", processId);
		jsonObj.put("user", user);

		HttpURLConnection httpConnection = openHttpConnection(INSERT_REQUEST_IN_DB);
		if (httpConnection != null) {
			writeOutputStream(httpConnection, jsonObj);
			jsonOutput(httpConnection);
		}
	}

	@SuppressWarnings("unchecked")
	public void updateTaskIDInDB(String processId, String taskId) {
		logger.info("Inside updateTaskIDInDB method");
		logger.info("processId " + processId);
		logger.info("taskId " + taskId);
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("processId", processId);
		jsonObj.put("taskId", taskId);

		HttpURLConnection httpConnection = openHttpConnection(UPDATE_REQUEST_IN_DB);
		if (httpConnection != null) {
			writeOutputStream(httpConnection, jsonObj);
			jsonOutput(httpConnection);
		}
	}

	public String performPreValidationTest(String businessKey, String version) {
		logger.info("Inside performPreValidationTest method");
		String url = null;
		if (businessKey != null && businessKey.length() > 3
				&& "SNRC".equalsIgnoreCase(businessKey.substring(0, Math.min(businessKey.length(), 4)))) {
			url = PREVALIDATE_ODL;
		}else {
			url = PERFORM_PREVALIDATE_TEST;
		}

		return executeBpmProcess(url, businessKey, version);
	}

	private HttpURLConnection openHttpConnection(String endpointUrl) {
		HttpURLConnection httpConnection = null;
		try {
			URL url = new URL(endpointUrl);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setConnectTimeout(5000);
			httpConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestMethod("POST");
		} catch (MalformedURLException urlExe) {
			logger.error("URL Exception ->" + urlExe.getMessage());
		} catch (ProtocolException proExe) {
			logger.error("Protocol Exception ->" + proExe.getMessage());
		} catch (IOException ioExe) {
			logger.error("IO Exception ->" + ioExe.getMessage());
		}

		return httpConnection;
	}

	private void writeOutputStream(HttpURLConnection httpConnection, JSONObject jsonObject) {
		try (OutputStream outputStream = httpConnection.getOutputStream()) {
			outputStream.write(jsonObject.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException ioExe) {
			logger.error("writeOutputStream - IO Exception ->" + ioExe.getMessage());
		}
	}

	private String getInputStream(HttpURLConnection httpConnection) {
		String result = null;
		try (InputStream inputStream = new BufferedInputStream(httpConnection.getInputStream())) {
			result = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
			logger.info("getInputStream - " + result);
		} catch (IOException ioExe) {
			logger.error("getInputStream - IO Exception ->" + ioExe.getMessage());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getJsonObject(String businessKey, String version, String requestType) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("requestId", businessKey);
		jsonObject.put("version", version);
		if (requestType != null) {
			jsonObject.put("requestType", requestType);
		}
		return jsonObject;
	}

	private String jsonOutput(HttpURLConnection httpConnection) {
		String output = null;
		String input = null;
		try {
			JSONParser parser = new JSONParser();
			input = getInputStream(httpConnection);
			if(input !=null && input.trim().length()>0) {
				JSONObject json = (JSONObject) parser.parse(input);
				output = json.get("output").toString();
			}
		} catch (ParseException exe) {
			logger.error("Json Parse Exception ->" + exe.getMessage());
		}

		logger.info("JSON Output after processing the HttpURLConnection With endpoint URL ->" + output);
		return output;
	}

	private String executeBpmProcess(String endpointUrl, String businessKey, String version) {
		return executeBpmProcess(endpointUrl, businessKey, version, null);
	}

	private String executeBpmProcess(String endpointUrl, String businessKey, String version, String requestType) {
		String outputVar = null;
		logger.info("endpointUrl " + endpointUrl);
		logger.info("businessKey " + businessKey);
		logger.info("version " + version);
		HttpURLConnection httpConnection = openHttpConnection(endpointUrl);
		if (httpConnection != null) {
			writeOutputStream(httpConnection, getJsonObject(businessKey, version, requestType));
			outputVar = jsonOutput(httpConnection);
		}
		logger.info("outputVar " + outputVar);
		return outputVar;
	}

}