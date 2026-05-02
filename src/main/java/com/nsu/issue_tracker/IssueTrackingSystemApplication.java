package com.nsu.issue_tracker;

import com.nsu.issue_tracker.authorization.security.JwtProperties;
import com.nsu.issue_tracker.config.FileUploadProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ JwtProperties.class, FileUploadProperties.class })
public class IssueTrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueTrackingSystemApplication.class, args);
	}

}
