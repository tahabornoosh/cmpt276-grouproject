package com.cmpt276.group3.grouproject.algorithms;

import java.util.ArrayList;

import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.models.MatchingProfile;

public class MatchingAlgorithm {
    private static int abs(int x) {
        return x<0 ? -x:x;
    }

    private static double hobbiesScore(MatchingProfile base, MatchingProfile target) {
        ArrayList<Double> a = new ArrayList<Double>();
        ArrayList<Double> b = new ArrayList<Double>();

        for (Hobby h:Hobby.values()) {
            if (h==Hobby.NONE) continue; // do not count NONE's
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
            b.add(item);
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
        if (abs(base.getAge()-target.getAge())<=5) score+=(5-abs(base.getAge()-target.getAge())); // 5 - age closeness
        if (abs(base.getYear_of_study()-target.getYear_of_study())<=3) score+=(3-abs(base.getAge()-target.getAge())); // 3 - year of study closeness
        if (base.getStudy_field()==target.getStudy_field()) score+=3; 
        if (base.isRegularly_goes_to_gym()==target.isRegularly_goes_to_gym()) score+=3; 
        if (base.getFavourite_sport()==target.getFavourite_sport()) score+=3; 
        if (base.getPreferred_venue()==target.getPreferred_venue()) score+=3; 

        if (base.getKind_of_friendship()==target.getKind_of_friendship()) score+=4;
        if (base.getSocial_style()==target.getSocial_style()) score+=3;
        if (base.getHangout_frequency()==target.getHangout_frequency()) score+=4;
        if (base.getFriend_activity()==target.getFriend_activity()) score+=3;
        if (base.getPlanning_style()==target.getPlanning_style()) score+=3;
        if (base.getConversation_style()==target.getConversation_style()) score+=3;
        if (base.getCommunication_style()==target.getCommunication_style()) score+=4;
        if (base.getPersonality_trait()==target.getPersonality_trait()) score+=3;
        if (base.getFriendship_value()==target.getFriendship_value()) score+=3;
        if (base.getAvailability()==target.getAvailability()) score+=4;
        if (base.getMotivation()==target.getMotivation()) score+=3;
        if (base.getFriend_type()==target.getFriend_type()) score+=3;

        return (score<102) ? 1+((int) score):102; // capped at 102%
    }
}
