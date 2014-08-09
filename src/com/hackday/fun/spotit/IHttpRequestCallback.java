package com.hackday.fun.spotit;

public interface IHttpRequestCallback {
	void onSuccess(String response);
	void onFailure();
}
