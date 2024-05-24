package cn.henu.typechatv2.activities;

import android.os.Bundle;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.ChatFutures;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.henu.typechatv2.databinding.ActivityChatWithGeminiBinding;


public class ChatWithGemini extends AppCompatActivity {
    private ActivityChatWithGeminiBinding binding;
    private static final String TAG = "ChatWithGemini";
    private Executor executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatWithGeminiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        executor = Executors.newSingleThreadExecutor();
        Init();
        //startChatWithAI();
    }

    private void Init() {
        // Initialize the Vertex AI service and the generative model
// Specify a model that supports your use case
// Gemini 1.5 models are versatile and can be used with all API capabilities
        GenerativeModel gm = FirebaseVertexAI.getInstance()
                .generativeModel("gemini-1.5-flash-preview-0514");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);


        Content prompt = new Content.Builder()
                .addText("Write a story about a magic backpack.")
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(prompt);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                Log.d(TAG, "onSuccess: "+resultText);
                System.out.println(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);

    }
}