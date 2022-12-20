package com.tpisoftware.gitlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.tpisoftware.gitlab.service.GitLabPushEventService;
import com.tpisoftware.gitlab.service.GitLabMergeRequestsEventsService;

@RestController
@RequestMapping("/gitlab-webhooks")
public class GitlabLabelsChangerController {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GitlabLabelsChangerController.class);

	@Value("${spring.gitlab.url}")
	private String URL;
	
	@Autowired
	GitLabPushEventService gitLabPushEventService;
	
	@Autowired
	GitLabMergeRequestsEventsService gitLabMergeRequestsEventsService;

	@RequestMapping(value = "/labelsActionPush", method = RequestMethod.POST)
	public Object labelsActionPush(@RequestBody String payload) {

		StringBuffer localApiUrl = 
				new StringBuffer(URL).append("projects/{projectId}/issues/{issuesIid}");
		try {
			gitLabPushEventService.pushEventForCreateBranchViaIssues(payload, localApiUrl.toString());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook format parse fail! \n" + e.toString());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Move issues card by \"Push Hook\" success!!");
		
	}
	
	@RequestMapping(value = "/labelsActionMR", method = RequestMethod.POST)
	public Object labelsActionMR(@RequestBody String payload) {

		StringBuffer localApiUrl = 
				new StringBuffer(URL).append("projects/{projectId}/issues/{issuesIid}");
		try {
			gitLabMergeRequestsEventsService.mergeRequestsEventsForObjAttr(payload, localApiUrl.toString());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook format parse fail! \n" + e.toString());
		}
		return ResponseEntity.status(HttpStatus.OK).body("Move issues {} card by \"Merge Reqeust Hook\" success!!");
	}

}
