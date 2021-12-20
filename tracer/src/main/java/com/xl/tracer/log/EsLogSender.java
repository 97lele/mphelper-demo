package com.xl.tracer.log;

import com.alibaba.fastjson.JSON;
import com.xl.tracer.Entrance;
import com.xl.tracer.trace.TracingContext;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tanjl11
 * @date 2021/12/20 9:59
 */
public class EsLogSender implements LogSender {
    private static RestClient client = null;
    private static Set<String> EXISTS_INDEX = new HashSet<>();
    private static final String ALIAS = "trace_log";
    private static final String TRACE_LOG_INDEX = "{\"settings\":{\"index\":{\"number_of_shards\":1,\"number_of_replicas\":0}},\"aliases\":{\"%s\":{}},\"mappings\":{\"properties\":{\"className\":{\"type\":\"keyword\"},\"endDate\":{\"type\":\"date\"},\"handleMills\":{\"type\":\"keyword\"},\"level\":{\"type\":\"integer\"},\"logInfo\":{\"type\":\"text\"},\"methodName\":{\"type\":\"keyword\"},\"parentSpanId\":{\"type\":\"keyword\"},\"root\":{\"type\":\"boolean\"},\"spanId\":{\"type\":\"keyword\"},\"startDate\":{\"type\":\"date\"},\"traceId\":{\"type\":\"keyword\"},\"type\":{\"type\":\"keyword\"}}}}";
    public static final String NAME = "es";

    static {
        client = RestClient.builder(new HttpHost(Entrance.ES_HOST, 9200)).build();
    }

    @Override
    public void batchSend(Collection<TraceLog> logs) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(new Date());
        String indexName = ALIAS + "_" + format;
        createIfNotExistsIndex(indexName);
        Request buckRequest = new Request("POST", "/" + indexName + "/_doc/_bulk");
        StringBuilder param = new StringBuilder();
        for (TraceLog traceLog : logs) {
            traceLog.setStartDate(new Date(traceLog.getStartTime()));
            traceLog.setEndDate(new Date(traceLog.getEndTime()));
            String source = JSON.toJSONString(traceLog);
            param.append("{ \"index\":{}}\n");
            param.append(source);
            param.append("\n");
            TracingContext.logger.trace("内容:{}", source);
        }
        buckRequest.setJsonEntity(param.toString());
        try {
            client.performRequest(buckRequest);
        } catch (IOException e) {
            TracingContext.logger.error("es日志发送失败", e);
        }
    }

    @Override
    public void singleSend(TraceLog log) {
        batchSend(Collections.singleton(log));
    }

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     */
    public static boolean indexExists(String index) {
        Request request = new Request("HEAD", "/" + index);
        Response response = null;
        try {
            response = client.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            return statusCode != 404;
        } catch (IOException e) {
            TracingContext.logger.error("判断索引是否存在失败", e);
        }
        return false;
    }

    private static void createIfNotExistsIndex(String indexName) {
        if (!EXISTS_INDEX.contains(indexName)) {
            try {
                boolean exist = indexExists(indexName);
                if (!exist) {
                    Request createIndex = new Request("PUT", "/" + indexName);
                    createIndex.setJsonEntity(String.format(TRACE_LOG_INDEX, ALIAS));
                    Response response = client.performRequest(createIndex);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        TracingContext.logger.debug("trace-log索引创建成功");
                    }
                }
                EXISTS_INDEX.add(indexName);
            } catch (IOException e) {
                TracingContext.logger.error("创建索引失败", e);
            }
        }
    }

    @Override
    public String name() {
        return NAME;
    }
}
