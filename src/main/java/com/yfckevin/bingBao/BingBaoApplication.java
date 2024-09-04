package com.yfckevin.bingBao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BingBaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BingBaoApplication.class, args);
	}


	@Bean(name = "sdf")
	public SimpleDateFormat sdf () {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
		return sdf;
	}
	@Bean(name = "ssf")
	public SimpleDateFormat ssf () {
		SimpleDateFormat ssf = new SimpleDateFormat("yyyyMMdd");
		ssf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
		return ssf;
	}
	@Bean(name = "picSuffix")
	public SimpleDateFormat picSuffix() {
		SimpleDateFormat picSuffix = new SimpleDateFormat("yyyyMMddHHmmss");
		picSuffix.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
		return picSuffix;
	}
	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper();
	}
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
