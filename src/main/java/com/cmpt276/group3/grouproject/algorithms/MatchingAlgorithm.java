package com.cmpt276.group3.grouproject.algorithms;

import java.util.ArrayList;

import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.models.MatchingProfile;

public class MatchingAlgorithm {
    private static double hobbiesScore(MatchingProfile base, MatchingProfile target) {
        ArrayList<Double> a = new ArrayList<Double>();
        ArrayList<Double> b = new ArrayList<Double>();

        for (Hobby h:Hobby.values()) {
            Double item = 0.0;
            if (base.getHobby1()==h) item+=4;
            if (base.getHobby2()==h) item+=2;
            if (base.getHobby3()==h) item+=1;
            if (base.getHobby4()==h) item+=0.5;
            if (base.getHobby5()==h) item+=0.25;
            a.add(item);

            item = 0.0;
            if (target.getHobby1()==h) item+=4;
            if (target.getHobby2()==h) item+=2;
            if (target.getHobby3()==h) item+=1;
            if (target.getHobby4()==h) item+=0.5;
            if (target.getHobby5()==h) item+=0.25;
        }

        double res = 0;

        for (int i = 0; i<a.size(); i++) {
            res+=a.get(i)*b.get(i);
        } 

        return 13*Math.log(res*3); // curved (empirically found curve - identical profile with no repeat gives ~48, full repitition gives ~61)
    } 

    public static int friendshipMatch(MatchingProfile base, MatchingProfile target) {
        // ~60% -> hobbies/interests factor (with no repitition) 
        // 60% -> linear factors 
        // maximum possible match score: ~120% (very hard to achieve 90%+ unless profiles intentionally optimized or if a hobby is heavily repeated)

        double score = hobbiesScore(base, target);
        // linear factors
        if (base.getAge()-target.getAge()<=5) score+=3*(5-(base.getAge()-target.getAge())); // 15 - age closeness
        if (base.getYear_of_study()-target.getYear_of_study()<=3) score+=5*(3-(base.getAge()-target.getAge())); // 15 - year of study closeness
        if (base.getStudy_field()==target.getStudy_field()) score+=8; // 8 - same field of study
        if (base.isRegularly_goes_to_gym()==target.isRegularly_goes_to_gym()) score+=7; // 7 - both gym-goers (or not)
        if (base.getFavourite_sport()==target.getFavourite_sport()) score+=8; // 8 - same favourite sport
        if (base.getPreferred_venue()==target.getPreferred_venue()) score+=7; // 7 - same preferred venue

        return (score<=102) ? (int) score:102; // capped at 102%
    }
}
