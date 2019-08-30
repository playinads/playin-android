package com.tech.playinsdk.http;

public interface HttpListener<T> {

    void success(T result);
    void failure(HttpException e);

}
