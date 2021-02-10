package com.example.client.Common;

import com.example.client.Remote.FCMClient;
import com.example.client.Remote.IFCMService;

public class Common {

   public static final String driver_tb1 ="Drivers";
    public static final String user_driver_tb1 ="DriversInformation";
    public static final String user_client_tb1 ="clientsInformation";
    public static final String pickup_request_tb1 ="PickupRequest";
    public static final String token_tb1 ="Tokens";

    public static final String fcmURL="https://fcm.googleapis.com/";


    public static IFCMService getFCMService(){

        return FCMClient.getClient(fcmURL).create(IFCMService.class);

    }

}
