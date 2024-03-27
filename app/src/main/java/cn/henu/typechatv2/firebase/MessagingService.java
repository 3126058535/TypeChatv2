package cn.henu.typechatv2.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MessagingService extends FirebaseMessagingService {
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Log.d("FCM","FCM Token: " + token);
    }
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Log.d("FCM","messsage" + Objects.requireNonNull(remoteMessage.getNotification()).getBody());
    }
}
