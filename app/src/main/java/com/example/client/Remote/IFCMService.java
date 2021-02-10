package com.example.client.Remote;

import com.example.client.Model.FCMResponse;
import com.example.client.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:Key=AAAApfgAkv0:APA91bFT8Y-huelShV_xeXDb9J-lGuhpK0u_9mC0_HwgtIzZI5N32t4299xjNdlzmtoK7W7Yur3ZpgasARa9Osu-Sx1Xfie_Llh8v8fCl9xU-0tUmhR2lDzz_8yFDt7XW4L3DmqScsTs"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
