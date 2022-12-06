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
public class GitLabMergeRequestsEventsService {
	private static final Logger logger = LoggerFactory.getLogger(GitLabMergeRequestsEventsService.class);
	
	@Value("${spring.gitlab.api_key}")
	private String API_KEY;
	
	@Autowired
	RestTemplate restTemplate;

	public void mergeRequestsEventsForObjAttr(String payload, String localApiUrl) throws JsonMappingException, JsonProcessingException {
		String addLabels;
		String removeLabels;
		String projectId;
        String issuesIid;
        String webhookAction;
        
		HttpHeaders headers=new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(API_KEY);
        
        HttpEntity<String> entity=new HttpEntity<String>(headers);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(payload);
        
        webhookAction = rootNode.path("object_attributes").path("action").asText();
        projectId = rootNode.path("object_attributes").path("source_project_id").asText();
        issuesIid = rootNode.path("object_attributes").path("source_branch").asText().split("-")[0];
        
        Map<String, String> urlParams = new HashMap<>();
		urlParams.put("projectId", projectId);
		urlParams.put("issuesIid", issuesIid);
        
        switch(webhookAction) {
        case "open":
        	addLabels = "MR+Waitting";
        	removeLabels = "Doing";
        	invokeGitLabApi(localApiUrl, addLabels, removeLabels, entity, urlParams);
        	break;
        
        case "approved":
        	addLabels = "MR+Reviewed";
        	removeLabels = "MR+Waitting";
        	invokeGitLabApi(localApiUrl, addLabels, removeLabels, entity, urlParams);
        	break;
        	
        case "unapproved":
        	addLabels = "Doing";
        	removeLabels = "MR+Waitting";
        	invokeGitLabApi(localApiUrl, addLabels, removeLabels, entity, urlParams);
        	break;
        	
        case "merge":
        	addLabels = "Env+SIT";
        	removeLabels = "MR+Reviewed";
        	invokeGitLabApi(localApiUrl, addLabels, removeLabels, entity, urlParams);
        	break;
        	
        default:
        	logger.info("Nothing change... cause Webhook Action is {}: ", webhookAction);
        	
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
