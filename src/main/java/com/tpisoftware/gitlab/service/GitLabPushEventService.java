package com.tpisoftware.gitlab.service;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GitLabPushEventService {

	private static final Logger logger = LoggerFactory.getLogger(GitLabPushEventService.class);

	@Value("${spring.gitlab.api_key}")
	private String API_KEY;

	@Autowired
	RestTemplate restTemplate;

	public void pushEventForCreateBranchViaIssues(String payload, String localApiUrl)
			throws JsonMappingException, JsonProcessingException {
		String addLabels;
		String removeLabels;
		String projectId;
		String issuesIid;
		String beforeSHACode;
//		int totalCommitsCount = 0;

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setBearerAuth(API_KEY);

		HttpEntity<String> entity = new HttpEntity<String>(headers);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(payload);

		projectId = rootNode.get("project_id").asText();
		issuesIid = rootNode.get("ref").asText().replace("refs/heads/", "").split("-")[0];
		beforeSHACode = rootNode.get("before").asText();
//		totalCommitsCount = rootNode.get("total_commits_count").asInt();

		if(beforeSHACode.equals("0000000000000000000000000000000000000000")) {
			removeLabels = "To+Do";
			addLabels = "Doing";
			
			Map<String, String> urlParams = new HashMap<>();
			urlParams.put("projectId", projectId);
			urlParams.put("issuesIid", issuesIid);

			invokeGitLabApi(localApiUrl, addLabels, removeLabels, entity, urlParams);
		}
	}

	private void invokeGitLabApi(String localApiUrl, String addLabels, String removeLabels, HttpEntity<String> entity,
			Map<String, String> urlParams) {
		UriComponentsBuilder addBuilder = UriComponentsBuilder.fromUriString(localApiUrl.toString()).queryParam("add_labels", addLabels);
		UriComponentsBuilder removeBuilder = UriComponentsBuilder.fromUriString(localApiUrl.toString()).queryParam("remove_labels", removeLabels);

		URI addUri = addBuilder.buildAndExpand(urlParams).toUri();
		ResponseEntity<String> addLabelResponse = restTemplate.exchange(addUri.toString(), HttpMethod.PUT, entity, String.class);
		logger.info("add issues labels api_url: {}, response: {}", addUri, addLabelResponse);

		URI removeUri = removeBuilder.buildAndExpand(urlParams).toUri();
		ResponseEntity<String> removeLabelResponse = restTemplate.exchange(removeUri.toString(), HttpMethod.PUT, entity, String.class);
		logger.info("remove issues labels api_url: {}, response: {}", removeUri, removeLabelResponse);
		
		
	}

}
