package com.sagar.eventmanagement.gallery.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(SupabaseStorageProperties.class)
@ConditionalOnProperty(prefix = "app.gallery.storage", name = "provider", havingValue = "supabase")
public class SupabaseStorageConfiguration {
    @Bean(destroyMethod = "close")
    S3Client supabaseS3Client(SupabaseStorageProperties properties) {
        properties.validate();
        return S3Client.builder()
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(45))
                        .apiCallAttemptTimeout(Duration.ofSeconds(15))
                        .build())
                .build();
    }
}
