package com.srizwan.islamipedia;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DistrictClass {
    private String id;
    private String divisionId;
    private String districtNameEnglish;
    private String districtNameBangla;
    private String sahriDifference;
    private String iftarDifference;
    private String latitude;
    private String longitude;

    @Override
    public String toString() {
        return districtNameBangla;
    }
    public DistrictClass(String id, String divisionId, String districtNameEnglish, String districtNameBangla,
                    String sahriDifference, String iftarDifference, String latitude, String longitude) {
        this.id = id;
        this.divisionId = divisionId;
        this.districtNameEnglish = districtNameEnglish;
        this.districtNameBangla = districtNameBangla;
        this.sahriDifference = sahriDifference;
        this.iftarDifference = iftarDifference;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public String getDistrictNameEnglish() {
        return districtNameEnglish;
    }

    public String getDistrictNameBangla() {
        return districtNameBangla;
    }

    public String getSahriDifference() {
        return sahriDifference;
    }

    public String getIftarDifference() {
        return iftarDifference;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public static List<DistrictClass> parseDistrictJson(String json, String divisionId) {
        List<DistrictClass> districts = new ArrayList<>();

        try {
            JSONArray districtsArray = new JSONArray(json);
            for (int i = 0; i < districtsArray.length(); i++) {
                JSONObject districtObject = districtsArray.getJSONObject(i);
                String districtDivisionId = districtObject.getString("division_id");
                if (districtDivisionId.equals(divisionId)) {
                    String id = districtObject.getString("id");
                    String districtNameEnglish = districtObject.getString("district_name_english");
                    String districtNameBangla = districtObject.getString("district_name_bangla");
                    String sahriDifference = districtObject.getString("sahri_difference");
                    String iftarDifference = districtObject.getString("iftar_difference");
                    String latitude = districtObject.getString("latitude");
                    String longitude = districtObject.getString("longitude");

                    DistrictClass district = new DistrictClass(id, districtDivisionId, districtNameEnglish, districtNameBangla,
                            sahriDifference, iftarDifference, latitude, longitude);
                    districts.add(district);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return districts;
    }
}

