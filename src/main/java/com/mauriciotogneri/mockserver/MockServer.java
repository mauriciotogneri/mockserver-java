package com.mauriciotogneri.mockserver;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class MockServer extends Dispatcher
{
    private final Integer port;
    private final MockWebServer server;
    private final List<EndPoint> endPoints;

    public MockServer(int port, List<EndPoint> endPoints)
    {
        this.port = port;
        this.endPoints = endPoints;
        this.server = new MockWebServer();
        this.server.setDispatcher(this);
    }

    public void start() throws IOException
    {
        server.start(port);
    }

    public void stop() throws IOException
    {
        server.shutdown();
    }

    @NotNull
    @Override
    public MockResponse dispatch(@NotNull RecordedRequest recordedRequest)
    {
        HttpRequest httpRequest = new HttpRequest(recordedRequest);

        try
        {
            EndPoint endPoint = endPoint(httpRequest);

            if (endPoint != null)
            {
                return endPoint.process(httpRequest).response();
            }
            else
            {
                return onNotFound(httpRequest).response();
            }
        }
        catch (Exception e)
        {
            return onError(httpRequest).response();
        }
    }

    private EndPoint endPoint(HttpRequest httpRequest)
    {
        for (EndPoint endPoint : endPoints)
        {
            if (endPoint.matches(httpRequest))
            {
                return endPoint;
            }
        }

        return null;
    }

    protected HttpResponse onNotFound(HttpRequest httpRequest)
    {
        return new HttpResponse.Builder(HttpResponseCode.NOT_FOUND).build();
    }

    protected HttpResponse onError(HttpRequest httpRequest)
    {
        return new HttpResponse.Builder(HttpResponseCode.INTERNAL_SERVER_ERROR).build();
    }
}