package com.eun.noa;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.kakao.sdk.newtoneapi.SpeechRecognizeListener;
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient;
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity implements TextToSpeechListener, SpeechRecognizeListener {
    private TextToSpeechClient ttsClient;
    private SpeechRecognizerClient client;
    private SpeechRecognizerClient.Builder builder;
    private Button button;

    private static final String TAG = "TextToSpeechActivity";
    private static final int REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE = 0;
    private String speech_text;
    private String state_text = null;
    private static final String DEFINITION = "DEFINITION";
    private static final String SEARCH = "SEARCH";
    private static final String REASK = "REASK";
    private static final String YES = "YES";
    private static final String FIND = "FIND";
    private static final String NAVIGATE = "NAVIGATE";
    private static final String NO = "NO";
    private static final String REASK_ASWER = "REASK_ANSWER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_AUDIO_AND_WRITE_EXTERNAL_STORAGE);
        }


        // 음성합성 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this);
        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());

        // 음성인식 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this);


        builder = new SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB);
        button = findViewById(R.id.bt);


        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                .setListener(MainActivity.this)
                .build();

        if (state_text == null) {
            speech_text = getString(R.string.str_start);
            ttsClient.play(speech_text);
            state_text = new String(DEFINITION);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient = new TextToSpeechClient.Builder()
                        .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
                        .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                        .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                        .setListener(MainActivity.this)
                        .build();

                if (state_text.equals(DEFINITION)) {   // 목적지 묻기
                    speech_text = getString(R.string.str_definition);
                    ttsClient.play(speech_text);
                    state_text = SEARCH;

                } else if (state_text.equals(SEARCH)) {    // 검색하기
                    client = builder.build();

                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);

                } else if (state_text.equals(REASK)) {
                    speech_text = button.getText().toString();
                    ttsClient.play("목적지가 " + speech_text + "입니까?");

                    state_text = REASK_ASWER;

                } else if (state_text.equals("REASK_ANSWER")) {
                    client = builder.build();

                    client.setSpeechRecognizeListener(MainActivity.this);
                    client.startRecording(true);
                    Toast.makeText(MainActivity.this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();

                } else if (state_text.equals(NO)) {
                    speech_text = getString(R.string.str_no);
                    ttsClient.play(speech_text);
                    state_text = SEARCH;
                } else if (state_text.equals(YES)) {
                    speech_text = getString(R.string.str_nevigate);
                    ttsClient.play(speech_text);
                    state_text = NAVIGATE;
                }
            }
        });


    }

    private void handleError(int errorCode) {
        String errorText;
        switch (errorCode) {
            case TextToSpeechClient.ERROR_NETWORK:
                errorText = "네트워크 오류";
                break;
            case TextToSpeechClient.ERROR_NETWORK_TIMEOUT:
                errorText = "네트워크 지연";
                break;
            case TextToSpeechClient.ERROR_CLIENT_INETRNAL:
                errorText = "음성합성 클라이언트 내부 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_INTERNAL:
                errorText = "음성합성 서버 내부 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_TIMEOUT:
                errorText = "음성합성 서버 최대 접속시간 초과";
                break;
            case TextToSpeechClient.ERROR_SERVER_AUTHENTICATION:
                errorText = "음성합성 인증 실패";
                break;
            case TextToSpeechClient.ERROR_SERVER_SPEECH_TEXT_BAD:
                errorText = "음성합성 텍스트 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_SPEECH_TEXT_EXCESS:
                errorText = "음성합성 텍스트 허용 길이 초과";
                break;
            case TextToSpeechClient.ERROR_SERVER_UNSUPPORTED_SERVICE:
                errorText = "음성합성 서비스 모드 오류";
                break;
            case TextToSpeechClient.ERROR_SERVER_ALLOWED_REQUESTS_EXCESS:
                errorText = "허용 횟수 초과";
                break;
            default:
                errorText = "정의하지 않은 오류";
                break;
        }

        final String statusMessage = errorText + " (" + errorCode + ")";

        Log.i(TAG, statusMessage);
    }

    @Override
    public void onReady() {//모든 하드웨어및 오디오 서비스가 모두 준비 된 다음 호출
        Log.d("MainActivity", "모든 준비가 완료 되었습니다.");
    }

    @Override
    public void onBeginningOfSpeech() { //사용자가 말하기 시작하는 순간 호출
        Log.d("MainActivity", "말하기 시작 했습니다.");
    }

    @Override
    public void onEndOfSpeech() {//사용자가 말하기를 끝냈다고 판단되면 호출
        Log.d("MainActivity", "말하기가 끝났습니다.");
    }

    @Override
    public void onError(int errorCode, String errorMsg) {

    }

    @Override
    public void onPartialResult(String partialResult) {//인식된 음성 데이터를 문자열로 알려 준다.

    }


    @Override
    public void onResults(Bundle results) { //음성 입력이 종료된것으로 판단하고 서버에 질의를 모두 마치고 나면 호출
        final StringBuilder builder = new StringBuilder();

        final ArrayList<String> texts = results.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS);
        ArrayList<Integer> confs = results.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES);

        Log.d("MainActivity", "Result: " + texts);

        for (int i = 0; i < texts.size(); i++) {
            builder.append(texts.get(i));
            builder.append(" (");
            builder.append(confs.get(i).intValue());
            builder.append(")\n");
        }

/*
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("result", texts.get(0));
*/

        //모든 콜백함수들은 백그라운드에서 돌고 있기 때문에 메인 UI를 변경할려면 runOnUiThread를 사용해야 한다.
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) return;

                Button button = findViewById(R.id.bt);
                button.setText(texts.get(0));
                if (state_text.equals(SEARCH))
                    state_text = REASK;
                else if (button.getText().equals("응") || button.getText().equals("네") || button.getText().equals("예"))
                    state_text = YES;
                else state_text = NO;
            }
        });
    }

    @Override
    public void onAudioLevel(float audioLevel) {

    }

    @Override
    public void onFinished() {
        int intSentSize = ttsClient.getSentDataSize();
        int intRecvSize = ttsClient.getReceivedDataSize();

        final String strInacctiveText = "onFinished() SentSize : " + intSentSize + " RecvSize : " + intRecvSize;

        Log.i(TAG, strInacctiveText);

        ttsClient = null;
    }
}
