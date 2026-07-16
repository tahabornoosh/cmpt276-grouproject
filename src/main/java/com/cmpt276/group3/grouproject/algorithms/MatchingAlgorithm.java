package com.cmpt276.group3.grouproject.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.models.MatchingProfile;

public class MatchingAlgorithm {
    private static int abs(int x) {
        return x < 0 ? -x : x;
    }

    private static double hobbiesScore(MatchingProfile base, MatchingProfile target) {
        ArrayList<Double> a = new ArrayList<Double>();
        ArrayList<Double> b = new ArrayList<Double>();

        for (Hobby h : Hobby.values()) {
            if (h == Hobby.NONE)
                continue; // do not count NONE's
            Double item = 0.0;
            if (base.getHobby1() == h)
                item += 4;
            if (base.getHobby2() == h)
                item += 2;
            if (base.getHobby3() == h)
                item += 1;
            if (base.getHobby4() == h)
                item += 0.5;
            if (base.getHobby5() == h)
                item += 0.25;
            a.add(item);

            item = 0.0;
            if (target.getHobby1() == h)
                item += 4;
            if (target.getHobby2() == h)
                item += 2;
            if (target.getHobby3() == h)
                item += 1;
            if (target.getHobby4() == h)
                item += 0.5;
            if (target.getHobby5() == h)
                item += 0.25;
            b.add(item);
        }

        double res = 0;

        for (int i = 0; i < a.size(); i++) {
            res += a.get(i) * b.get(i);
        }

        return 13 * Math.log((res + 1) * 3); // curved (empirically found curve - identical profile with no repeat gives
                                             // ~48,
        // full repitition gives ~61)
    }

    public static int friendshipMatch(MatchingProfile base, MatchingProfile target) {
        // ~60% -> hobbies/interests factor (with no repitition)
        // 60% -> linear factors
        // maximum possible match score: ~120% (very hard to achieve 90%+ unless
        // profiles intentionally optimized or if a hobby is heavily repeated)

        double score = hobbiesScore(base, target);
        // linear factors
        if (abs(base.getAge() - target.getAge()) <= 5)
            score += (5 - abs(base.getAge() - target.getAge())); // 5 - age closeness
        if (abs(base.getYear_of_study() - target.getYear_of_study()) <= 3)
            score += (3 - abs(base.getAge() - target.getAge())); // 3 - year of study closeness
        if (base.getStudy_field() == target.getStudy_field())
            score += 3;
        if (base.isRegularly_goes_to_gym() == target.isRegularly_goes_to_gym())
            score += 3;
        if (base.getFavourite_sport() == target.getFavourite_sport())
            score += 3;
        if (base.getPreferred_venue() == target.getPreferred_venue())
            score += 3;

        if (base.getKind_of_friendship() == target.getKind_of_friendship())
            score += 4;
        if (base.getSocial_style() == target.getSocial_style())
            score += 3;
        if (base.getHangout_frequency() == target.getHangout_frequency())
            score += 4;
        if (base.getFriend_activity() == target.getFriend_activity())
            score += 3;
        if (base.getPlanning_style() == target.getPlanning_style())
            score += 3;
        if (base.getConversation_style() == target.getConversation_style())
            score += 3;
        if (base.getCommunication_style() == target.getCommunication_style())
            score += 4;
        if (base.getPersonality_trait() == target.getPersonality_trait())
            score += 3;
        if (base.getFriendship_value() == target.getFriendship_value())
            score += 3;
        if (base.getAvailability() == target.getAvailability())
            score += 4;
        if (base.getMotivation() == target.getMotivation())
            score += 3;
        if (base.getFriend_type() == target.getFriend_type())
            score += 3;

        return (score < 102) ? 1 + ((int) score) : 102; // capped at 102%
    }

    public static int relationshipMatch(MatchingProfile base, MatchingProfile target) {
        double s = hobbiesScore(base, target) * 0.5; // up to ~30%

        // Exclusionary questions + 30%
        if (base.getRelationship_goal() != target.getRelationship_goal()) {
            return -1;
        }

        if (base.getPartner_gender() != Gender.NONE
                && base.getPartner_gender() != target.getUser().getGender()) {
            return -1;
        }

        if (target.getPartner_gender() != Gender.NONE
                && target.getPartner_gender() != base.getUser().getGender()) {
            return -1;
        }

        if (base.getMin_partner_age() > target.getAge()
                || base.getMax_partner_age() < target.getAge()) {
            return -1;
        }

        if (target.getMin_partner_age() > base.getAge()
                || target.getMax_partner_age() < base.getAge()) {
            return -1;
        }

        if (base.isLooking_for_short_term_relationship() != target.isLooking_for_short_term_relationship()) {
            return -1;
        }

        s += 30;

        // Relationship questionnaire: 15 factors × 4% = 60%
        if (base.getRelationship_goal() == target.getRelationship_goal()) {
            s += 4;
        }

        if (base.getRelationship_personality() == target.getRelationship_personality()) {
            s += 4;
        }

        if (base.getRelationship_communication_style() == target.getRelationship_communication_style()) {
            s += 4;
        }

        if (base.getRelationship_texting_style() == target.getRelationship_texting_style()) {
            s += 4;
        }

        if (base.getRelationship_free_time() == target.getRelationship_free_time()) {
            s += 4;
        }

        if (base.getRelationship_value() == target.getRelationship_value()) {
            s += 4;
        }

        if (base.getRelationship_conflict_style() == target.getRelationship_conflict_style()) {
            s += 4;
        }

        if (base.getRelationship_lifestyle() == target.getRelationship_lifestyle()) {
            s += 4;
        }

        if (base.getRelationship_ambition_importance() == target.getRelationship_ambition_importance()) {
            s += 4;
        }

        if (base.getRelationship_care_style() == target.getRelationship_care_style()) {
            s += 4;
        }

        if (base.getRelationship_personal_space() == target.getRelationship_personal_space()) {
            s += 4;
        }

        if (base.getRelationship_date_activity() == target.getRelationship_date_activity()) {
            s += 4;
        }

        if (base.getRelationship_social_life() == target.getRelationship_social_life()) {
            s += 4;
        }

        if (base.getRelationship_humor_style() == target.getRelationship_humor_style()) {
            s += 4;
        }

        if (base.getRelationship_strength() == target.getRelationship_strength()) {
            s += 4;
        }

        return (s < 102) ? 1 + ((int) s) : 102; // capped at 102%
    }

    public static int studyBuddyMatch(MatchingProfile base, MatchingProfile target) {
        // exclusions
        if (base.getStudy_buddy_gender_preference() != Gender.NONE
                && base.getStudy_buddy_gender_preference() != target.getUser().getGender())
            return -1;
        if (target.getStudy_buddy_gender_preference() != Gender.NONE
                && target.getStudy_buddy_gender_preference() != base.getUser().getGender())
            return -1;
        if (target.getYear_of_study() > base.getBuddy_max_year_of_study()
                || base.getYear_of_study() > target.getBuddy_max_year_of_study())
            return -1;
        if (target.getYear_of_study() < base.getBuddy_min_year_of_study()
                || base.getYear_of_study() < target.getBuddy_min_year_of_study())
            return -1;
        int s = 20; // 20% for exclusions passing
        // dicipline - 40%
        if (base.getStudy_field() == target.getStudy_field())
            s += 10; // same field
        if (base.getBuddy_area_of_study() == target.getStudy_field())
            s += 10; // target matching base preference
        if (target.getBuddy_area_of_study() == base.getStudy_field())
            s += 10;
        if (target.getStudy_buddy_program().toLowerCase().equals(base.getStudy_buddy_program().toLowerCase()))
            s += 10;
        // Courses - 60%
        List<String> b = getStudyBuddyCourseList(base.getStudy_buddy_courses());
        List<String> c = getStudyBuddyCourseList(target.getStudy_buddy_courses());
        int p = 0;
        for (String a:b) {
            for (String d:c) {
                if (a.equals(d)) p+=1;
            }
        } 

        if (p!=0) {
            int change = 30;
            if (p>5) p = 5; // at most 5 courses considered
            for (int i = 0; i<p; i++) {
                s+=change;
                if (change==15) change = 5;
                if (change!=5) change = change/2;
            } // matching course scores (1 matching, 2, 3, 4, 5+): 30, 45, 50, 55, 60 
        }

        return s>102 ? 102:s;
    }

    private static List<String> getStudyBuddyCourseList(String study_buddy_courses) {
        if (study_buddy_courses == null || study_buddy_courses.isBlank()) {
            return new ArrayList<>();
        }

        return Arrays.stream(study_buddy_courses.split(","))
                .map(String::trim)
                .filter(course -> !course.isBlank())
                .toList();
    }
}
