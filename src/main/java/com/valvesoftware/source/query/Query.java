package com.valvesoftware.source.query;

public interface Query extends Message {
	public Integer challenge();
	public Query withChallenge(int challenge);
}
