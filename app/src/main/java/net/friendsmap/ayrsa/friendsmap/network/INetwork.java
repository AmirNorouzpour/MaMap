package net.friendsmap.ayrsa.friendsmap.network;

import com.androidnetworking.error.ANError;

public interface INetwork<T> {

    void onResponse(T response) throws Exception;

    void onError(ANError anError);

}
