package ru.runa.gpd.connector.wfe.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import ru.runa.gpd.util.IOUtils;

public class ApiClient {
    private final CloseableHttpClient closeableHttpClient;
    private final String apiPath;
    private final String protocol;
    private final String host;
    private final int port;
    private String token;

    public ApiClient(String protocol, String host, int port) {
        closeableHttpClient = HttpClients.createDefault();
        apiPath = "/restapi";
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public byte[] get(URI uri) {
        return sendRequest(new HttpGet(uri));
    }

    public byte[] post(URI uri, HttpEntity entity) {
        HttpPost httppost = new HttpPost(uri);
        httppost.setEntity(entity);
        return sendRequest(httppost);
    }

    public byte[] put(URI uri, HttpEntity entity) {
        HttpPut httpput = new HttpPut(uri);
        httpput.setEntity(entity);
        return sendRequest(httpput);
    }

    public byte[] patch(URI uri, HttpEntity entity) {
        HttpPatch httppatch = new HttpPatch(uri);
        httppatch.setEntity(entity);
        return sendRequest(httppatch);
    }

    public URI buildURI(String path, NameValuePair... params) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(protocol).setHost(host).setPort(port).setPath(apiPath + path).setParameters(params);
            return builder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] sendRequest(HttpRequestBase request) {
        addHeaders(request);
        try (CloseableHttpResponse httpresponse = closeableHttpClient.execute(request)) {
            byte[] content = IOUtils.readStreamAsBytes(httpresponse.getEntity().getContent());
            if (httpresponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException(new String(content));
            }
            return content;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addHeaders(HttpRequestBase request) {
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
    }

}
