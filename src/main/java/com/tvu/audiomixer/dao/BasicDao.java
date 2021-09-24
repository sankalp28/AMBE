package com.tvu.audiomixer.dao;

import java.util.Objects;


import com.amazon.dax.client.dynamodbv2.AmazonDaxClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

public class BasicDao {
	private static AmazonDynamoDB amazonDynamoDB;
	private static DynamoDBMapper dynamoDBMapper;
   
    public BasicDao() {
	   init();
    }
    
    @SuppressWarnings("deprecation")
	private static void init() {
		System.out.println("init method called");
	   if (Objects.isNull(amazonDynamoDB)) {
		   System.out.println("WITHOUT DAX IMPLEMENTATION");
           //amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
           amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().build();

           //dynamoDB = new DynamoDB(amazonDynamoDB);
           dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
	   }
    }

	public static DynamoDBMapper getDynamoDBMapper() {
		return dynamoDBMapper;
	}

	public static void setDynamoDBMapper(DynamoDBMapper dynamoDBMapper) {
		BasicDao.dynamoDBMapper = dynamoDBMapper;
	}
}