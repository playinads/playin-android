package com.tech.playinsdk.listener;

import com.tech.playinsdk.http.HttpException;

public interface HttpListener<T> {

    void success(T result);
    void failure(HttpException e);

}
