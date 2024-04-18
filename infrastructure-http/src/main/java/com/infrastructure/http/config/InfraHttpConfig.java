package com.infrastructure.http.config;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.internal.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@EnableFeignClients(basePackages = {"com"})
@Configuration
public class InfraHttpConfig {

    @Value("${infra.http.connect-timeout:2000}")
    private Integer connectTimeout;

    @Value("${infra.http.read-timeout:50000}")
    private Integer readTimeout;

    @Value("${infra.http.write-timeout:10000}")
    private Integer writeTimeout;

    @Value("${infra.http.max-idle-connections:20}")
    private Integer maxIdleConnections;//最大空闲连接数

    @Value("${infra.http.keep-alive-duration:300000}")
    private Long keepAliveDuration;//连接空闲时保留连接时长

    @Value("${infra.http.max-requests:200}")
    private Integer maxRequests;//最大请求数 框架默认 64

    @Value("${infra.http.max-requests-per-host:1}")
    private Integer maxRequestsPerHost;//最大并发数 框架默认 4

    @Value("${infra.http.dispatcher.max-pool-size:"+Integer.MAX_VALUE+"}")
    private Integer dispatcherMaxPoolSize;//最大线程数 Integer.MAX_VALUE
    @Value("${infra.http.dispatcher.keep-alive-second:60}")
    private Integer dispatcherKeepAliveTime;//分发最大等待时间 60秒
    @Bean
    public ConnectionPool connectionPool() {
        ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.MILLISECONDS);
        return connectionPool;
    }

    @Bean
    public Dispatcher okHttpDispatcher() {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(maxIdleConnections, dispatcherMaxPoolSize, dispatcherKeepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher", false));
        return new Dispatcher(poolExecutor);
    }

    @Bean
    public OkHttpClient okHttpClient(ConnectionPool connectionPool,Dispatcher okHttpDispatcher){
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectionPool(connectionPool)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .hostnameVerifier((hostname, session) -> true)
                // 拦截器
//                .addInterceptor()
                .dispatcher(okHttpDispatcher)
                .build();
        okHttpClient.dispatcher().setMaxRequests(maxRequests);
        okHttpClient.dispatcher().setMaxRequestsPerHost(maxRequestsPerHost);
        return okHttpClient;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(OkHttpClient okHttpClient) {
        OkHttp3ClientHttpRequestFactory clientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(okHttpClient);
        return clientHttpRequestFactory;
    }


}
