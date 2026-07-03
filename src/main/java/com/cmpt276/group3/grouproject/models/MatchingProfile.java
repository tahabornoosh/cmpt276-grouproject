package com.cmpt276.group3.grouproject.models;

import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Sport;
import com.cmpt276.group3.grouproject.enums.StudyField;
import com.cmpt276.group3.grouproject.enums.Venue;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name="profiles")
public class MatchingProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user; // link to the user - to be managed by the service to prevent duplicates

    // visibility/matchability
    private boolean display_friendship_profile;
    private boolean display_dating_profile;
    private boolean display_study_buddy_profile;

    // basic matching questions - everything optional
    @Nullable
    @Min(18)
    private int age;
    @Nullable
    private StudyField study_field;
    @Min(1)
    @Nullable
    private int year_of_study;
    @Nullable
    private boolean has_job;
    @Nullable
    private boolean regularly_goes_to_gym;
    @Nullable
    private Sport favourite_sport;
    @Nullable
    private Venue preferred_venue;
    @Nullable
    private Hobby hobby1; // can be repeated for over-emphasis - priorities: 4 2 1 0.5 0.25
    @Nullable 
    private Hobby hobby2;
    @Nullable
    private Hobby hobby3;
    @Nullable
    private Hobby hobby4;
    @Nullable
    private Hobby hobby5;

    // dating - nullable but service should not allow null enteries with display_dating_profile set to true
    @Nullable
    private boolean looking_for_short_term_relationship;
    @Nullable
    @Min(18)
    private int min_partner_age;
    @Nullable
    @Min(18)
    private int max_partner_age;
    @Nullable
    private Gender partner_gender;

    // Study buddies
    @Nullable
    private StudyField buddy_area_of_study;
    @Nullable
    @Min(1)
    private int buddy_min_year_of_study;
    @Nullable
    @Min(1)
    private int buddy_max_year_of_study;
    public MatchingProfile(long id, User user, boolean display_friendship_profile, boolean display_dating_profile,
            boolean display_study_buddy_profile, @Min(18) int age, StudyField study_field, @Min(1) int year_of_study,
            boolean has_job, boolean regularly_goes_to_gym, Sport favourite_sport, Venue preferred_venue, Hobby hobby1,
            Hobby hobby2, Hobby hobby3, Hobby hobby4, Hobby hobby5, boolean looking_for_short_term_relationship,
            @Min(18) int min_partner_age, @Min(18) int max_partner_age, Gender partner_gender,
            StudyField buddy_area_of_study, @Min(1) int buddy_min_year_of_study, @Min(1) int buddy_max_year_of_study) {
        this.id = id;
        this.user = user;
        this.display_friendship_profile = display_friendship_profile;
        this.display_dating_profile = display_dating_profile;
        this.display_study_buddy_profile = display_study_buddy_profile;
        this.age = age;
        this.study_field = study_field;
        this.year_of_study = year_of_study;
        this.has_job = has_job;
        this.regularly_goes_to_gym = regularly_goes_to_gym;
        this.favourite_sport = favourite_sport;
        this.preferred_venue = preferred_venue;
        this.hobby1 = hobby1;
        this.hobby2 = hobby2;
        this.hobby3 = hobby3;
        this.hobby4 = hobby4;
        this.hobby5 = hobby5;
        this.looking_for_short_term_relationship = looking_for_short_term_relationship;
        this.min_partner_age = min_partner_age;
        this.max_partner_age = max_partner_age;
        this.partner_gender = partner_gender;
        this.buddy_area_of_study = buddy_area_of_study;
        this.buddy_min_year_of_study = buddy_min_year_of_study;
        this.buddy_max_year_of_study = buddy_max_year_of_study;
    }

    public MatchingProfile() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isDisplay_friendship_profile() {
        return display_friendship_profile;
    }

    public void setDisplay_friendship_profile(boolean display_friendship_profile) {
        this.display_friendship_profile = display_friendship_profile;
    }

    public boolean isDisplay_dating_profile() {
        return display_dating_profile;
    }

    public void setDisplay_dating_profile(boolean display_dating_profile) {
        this.display_dating_profile = display_dating_profile;
    }

    public boolean isDisplay_study_buddy_profile() {
        return display_study_buddy_profile;
    }

    public void setDisplay_study_buddy_profile(boolean display_study_buddy_profile) {
        this.display_study_buddy_profile = display_study_buddy_profile;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public StudyField getStudy_field() {
        return study_field;
    }

    public void setStudy_field(StudyField study_field) {
        this.study_field = study_field;
    }

    public int getYear_of_study() {
        return year_of_study;
    }

    public void setYear_of_study(int year_of_study) {
        this.year_of_study = year_of_study;
    }

    public boolean isHas_job() {
        return has_job;
    }

    public void setHas_job(boolean has_job) {
        this.has_job = has_job;
    }

    public boolean isRegularly_goes_to_gym() {
        return regularly_goes_to_gym;
    }

    public void setRegularly_goes_to_gym(boolean regularly_goes_to_gym) {
        this.regularly_goes_to_gym = regularly_goes_to_gym;
    }

    public Sport getFavourite_sport() {
        return favourite_sport;
    }

    public void setFavourite_sport(Sport favourite_sport) {
        this.favourite_sport = favourite_sport;
    }

    public Venue getPreferred_venue() {
        return preferred_venue;
    }

    public void setPreferred_venue(Venue preferred_venue) {
        this.preferred_venue = preferred_venue;
    }

    public Hobby getHobby1() {
        return hobby1;
    }

    public void setHobby1(Hobby hobby1) {
        this.hobby1 = hobby1;
    }

    public Hobby getHobby2() {
        return hobby2;
    }

    public void setHobby2(Hobby hobby2) {
        this.hobby2 = hobby2;
    }

    public Hobby getHobby3() {
        return hobby3;
    }

    public void setHobby3(Hobby hobby3) {
        this.hobby3 = hobby3;
    }

    public Hobby getHobby4() {
        return hobby4;
    }

    public void setHobby4(Hobby hobby4) {
        this.hobby4 = hobby4;
    }

    public Hobby getHobby5() {
        return hobby5;
    }

    public void setHobby5(Hobby hobby5) {
        this.hobby5 = hobby5;
    }

    public boolean isLooking_for_short_term_relationship() {
        return looking_for_short_term_relationship;
    }

    public void setLooking_for_short_term_relationship(boolean looking_for_short_term_relationship) {
        this.looking_for_short_term_relationship = looking_for_short_term_relationship;
    }

    public int getMin_partner_age() {
        return min_partner_age;
    }

    public void setMin_partner_age(int min_partner_age) {
        this.min_partner_age = min_partner_age;
    }

    public int getMax_partner_age() {
        return max_partner_age;
    }

    public void setMax_partner_age(int max_partner_age) {
        this.max_partner_age = max_partner_age;
    }

    public Gender getPartner_gender() {
        return partner_gender;
    }

    public void setPartner_gender(Gender partner_gender) {
        this.partner_gender = partner_gender;
    }

    public StudyField getBuddy_area_of_study() {
        return buddy_area_of_study;
    }

    public void setBuddy_area_of_study(StudyField buddy_area_of_study) {
        this.buddy_area_of_study = buddy_area_of_study;
    }

    public int getBuddy_min_year_of_study() {
        return buddy_min_year_of_study;
    }

    public void setBuddy_min_year_of_study(int buddy_min_year_of_study) {
        this.buddy_min_year_of_study = buddy_min_year_of_study;
    }

    public int getBuddy_max_year_of_study() {
        return buddy_max_year_of_study;
    }

    public void setBuddy_max_year_of_study(int buddy_max_year_of_study) {
        this.buddy_max_year_of_study = buddy_max_year_of_study;
    }

}
