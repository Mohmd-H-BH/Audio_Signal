package com.testapp.audiosignal.repos;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.testapp.audiosignal.Model.indexWords;
import com.testapp.audiosignal.Model.metaModel;
import com.testapp.audiosignal.Model.wordModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class DataSource_JSON {

    private static final String TAG = "DataSource_JSON";
    private List<wordModel> wordModels_List = new ArrayList<>();
    private List<indexWords> indexWordModels_List = new ArrayList<>();
    private List<metaModel> speaker_List = new ArrayList<>();

    public void getDataFromJSON_File(Context context) {
        Gson gson = new Gson();
        try {
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(readJSONFromAsset(context)));

            JSONObject meta = jsonObject.getJSONObject("meta");

            JSONArray jsonArray_Sections = jsonObject.getJSONArray("sections");

            for (int i = 0; i < jsonArray_Sections.length(); i++) {

                JSONObject object_sectionsArray = jsonArray_Sections.getJSONObject(i);

                JSONObject meta_Object = object_sectionsArray.getJSONObject("meta");

                metaModel metaModel = gson.fromJson(meta_Object.toString(), metaModel.class);

                speaker_List.add(metaModel);

                JSONArray words_Array = object_sectionsArray.getJSONArray("words");

                //Timber.tag(TAG).d("object Array : %s", meta_Object.getString("speaker"));

                for (int j = 0; j < words_Array.length(); j++) {

                    JSONObject wordObject = words_Array.getJSONObject(j);

                    wordModel wordModel1 = gson.fromJson(wordObject.toString(), wordModel.class);

                    wordModels_List.add(wordModel1);

                    //Timber.tag(TAG).d("wordModel : %s", wordModel1.getText());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Timber.tag(TAG).d("JSON ERROR: %s", e.getLocalizedMessage());
        }
    }

    private String readJSONFromAsset(Context context) {

        String json;
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open("file-sample.json");

            int size = inputStream.available();

            byte[] buffer = new byte[size];

            inputStream.read(buffer);

            json = new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return json;
    }

    public String getDataFrom_StringBuilder() {
        int firstLetterIndex;
        int lastLetterIndex;
        int stringTotalLength = 0;
        int wordLength;
        String word;
        int sizeWordModelList = wordModels_List.size();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < sizeWordModelList; i++) {

            stringBuilder.append(wordModels_List.get(i).getText()).append(" ");
            wordLength = wordModels_List.get(i).getText().length();
            firstLetterIndex = stringTotalLength;
            lastLetterIndex = (wordLength - 1) + stringTotalLength;
            word = wordModels_List.get(i).getText();
            indexWords temp_indexWord = new indexWords(word, wordLength, firstLetterIndex, lastLetterIndex);
            indexWordModels_List.add(temp_indexWord);

            if (sizeWordModelList != i + 1) {
                stringTotalLength = stringTotalLength + (wordLength + 1);
            } else {
                stringTotalLength = stringTotalLength + (wordLength - 1);
            }
        }
        return stringBuilder.toString();
    }

    public List<indexWords> getIndexWordModels_List() {
        return indexWordModels_List;
    }

    public List<wordModel> getWordModels_List() {
        return wordModels_List;
    }

    public List<metaModel> getSpeaker_List() {
        return speaker_List;
    }
}


