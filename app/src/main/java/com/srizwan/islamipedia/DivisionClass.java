package com.srizwan.islamipedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DivisionClass {
    private String id;
    private String divisionNameEnglish;
    private String divisionNameBangla;

    @Override
    public String toString() {
        return divisionNameBangla;
    }
    public DivisionClass(String id, String divisionNameEnglish, String divisionNameBangla) {
        this.id = id;
        this.divisionNameEnglish = divisionNameEnglish;
        this.divisionNameBangla = divisionNameBangla;
    }

    public String getId() {
        return id;
    }

    public String getDivisionNameEnglish() {
        return divisionNameEnglish;
    }

    public String getDivisionNameBangla() {
        return divisionNameBangla;
    }

    public static List<DivisionClass> parseDivisionJson(String json) {
        List<DivisionClass> divisions = new ArrayList<>();

        try {
            JSONArray divisionsArray = new JSONArray(json);
            for (int i = 0; i < divisionsArray.length(); i++) {
                JSONObject divisionObject = divisionsArray.getJSONObject(i);
                String id = divisionObject.getString("id");
                String divisionNameEnglish = divisionObject.getString("division_name_english");
                String divisionNameBangla = divisionObject.getString("division_name_bangla");
                DivisionClass division = new DivisionClass(id, divisionNameEnglish, divisionNameBangla);
                divisions.add(division);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return divisions;
    }
}
