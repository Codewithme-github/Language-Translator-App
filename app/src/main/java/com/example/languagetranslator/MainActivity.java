package com.example.languagetranslator;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognizerResultsIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText sourcedt;
    private TextView Tvtranslated;
    private MaterialButton translatedbtn;
    private Spinner fromspinner, tospinner;
    private ImageView micimg;

    String[] fromlanguages = {"From", "Arabic", "Bengali", "Bulgarian", "Chinese", "Danish", "English", "french", "german",
            "Gujarati","Hindi", "Italian", "Japanese", "Korean","Latvin","Malay","Marathi", "Polish", "Portuguese", "Romanian",
            "Russian", "Tamil", "Telugu", "Urdu"};
    String[] tolanguages = {"To", "Arabic", "Bengali", "Bulgarian", "Chinese", "Danish", "English", "french", "german",
            "Gujarati","Hindi", "Italian", "Japanese", "Korean","Latvin","Malay","Marathi", "Polish", "Portuguese", "Romanian",
            "Russian", "Tamil", "Telugu", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languagecode, fromlanguagecode, tolanguagecode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sourcedt = findViewById(R.id.edttext);
        Tvtranslated = findViewById(R.id.tvtranslated);
        translatedbtn = findViewById(R.id.btntranslate);
        fromspinner = findViewById(R.id.fromspinner);
        tospinner = findViewById(R.id.tospinner);
        micimg = findViewById(R.id.idmic);
        fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromlanguagecode = getLanguagecode(fromlanguages[position]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromlanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromAdapter);

        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                tolanguagecode = getLanguagecode(tolanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,tolanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toAdapter);

        translatedbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Tvtranslated.setText("");
                if(sourcedt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter your text to translate",Toast.LENGTH_SHORT).show();
                }else if(fromlanguagecode == 0){
                    Toast.makeText(MainActivity.this,"Please select your Language",Toast.LENGTH_SHORT).show();
                }else if(tolanguagecode==0){
                    Toast.makeText(MainActivity.this,"Please selecet the language to make a translation",Toast.LENGTH_SHORT).show();
                }else{
                    transalatedText(fromlanguagecode,tolanguagecode,sourcedt.getText().toString());

                }
            }
        });
        micimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to Convert into Text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void StartActivityForResult(Intent i, int requestPermissionCode) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE){
            if(resultCode == RESULT_OK & data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourcedt.setText(result.get(0));
            }
        }

    }

    private void transalatedText(int fromlanguagecode, int tolanguagecode, String source){
        Tvtranslated.setText("Downloading Model....");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguagecode)
                .setTargetLanguage(tolanguagecode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Tvtranslated.setText("Translating.....");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        Tvtranslated.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Fail to translate :"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"fail to download a language model "+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


    }

    public int getLanguagecode(String language){
        int languagecode = 0;
        switch (language){
            case "Arabic":
                languagecode = FirebaseTranslateLanguage.AR;
                break;
            case "Bengali":
                languagecode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languagecode = FirebaseTranslateLanguage.BG;
                break;
            case "Chinese":
                languagecode = FirebaseTranslateLanguage.CA;
                break;
            case "Danish":
                languagecode = FirebaseTranslateLanguage.DA;
                break;
            case "English":
                languagecode = FirebaseTranslateLanguage.EN;
                break;
            case "french":
                languagecode = FirebaseTranslateLanguage.FR;
                break;
            case "german":
                languagecode = FirebaseTranslateLanguage.DE;
                break;
            case "Gujarati":
                languagecode = FirebaseTranslateLanguage.GU;
                break;
            case "Hindi":
                languagecode = FirebaseTranslateLanguage.HI;
                break;
            case "Italian":
                languagecode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languagecode = FirebaseTranslateLanguage.JA;
                break;
            case "Korean":
                languagecode = FirebaseTranslateLanguage.KO;
                break;
            case "Latvin":
                languagecode = FirebaseTranslateLanguage.LV;
                break;
            case "Malay":
                languagecode = FirebaseTranslateLanguage.MS;
                break;
            case "Marathi":
                languagecode = FirebaseTranslateLanguage.MR;
                break;
            case "Polish":
                languagecode = FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languagecode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languagecode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languagecode = FirebaseTranslateLanguage.RU;
                break;
            case "Tamil":
                languagecode = FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languagecode = FirebaseTranslateLanguage.TE;
                break;
            case "Urdu":
                languagecode = FirebaseTranslateLanguage.UR;
                break;
            default:
                languagecode = 0;

        }
        return languagecode;
    }

}