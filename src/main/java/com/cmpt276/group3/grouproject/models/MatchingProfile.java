package com.cmpt276.group3.grouproject.models;

import com.cmpt276.group3.grouproject.enums.*;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

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
    private Integer age;
    @Nullable
    private StudyField study_field;
    @Min(1)
    @Nullable
    private Integer year_of_study;
    @Nullable
    private Boolean has_job;
    @Nullable
    private Boolean regularly_goes_to_gym;
    @Nullable
    private Sport favourite_sport;
    @Nullable
    private Venue preferred_venue;
    @Nullable
    private Hobby hobby1; // a hobby can be repeated for over-emphasis - priorities: 4 2 1 0.5 0.25
    @Nullable 
    private Hobby hobby2;
    @Nullable
    private Hobby hobby3;
    @Nullable
    private Hobby hobby4;
    @Nullable
    private Hobby hobby5;

    // friendship-specific questions
    @Nullable
    private FriendshipStyle kind_of_friendship;
    @Nullable
    private SocialStyle social_style;
    @Nullable
    private HangoutFrequency hangout_frequency;
    @Nullable
    private FriendActivity friend_activity;
    @Nullable
    private PlanningStyle planning_style;
    @Nullable
    private ConversationStyle conversation_style;
    @Nullable
    private CommunicationStyle communication_style;
    @Nullable
    private PersonalityTrait personality_trait;
    @Nullable
    private FriendshipValue friendship_value;
    @Nullable
    private Availability availability;
    @Nullable
    private Motivation motivation;
    @Nullable
    private FriendType friend_type;

    // dating - nullable but service should not allow null enteries with display_dating_profile set to true
    @Nullable
    private Boolean looking_for_short_term_relationship;
    @Nullable
    @Min(18)
    private Integer min_partner_age;
    @Nullable
    @Min(18)
    private Integer max_partner_age;
    @Nullable
    private Gender partner_gender;

    // Study buddies
    @Nullable
    private StudyField buddy_area_of_study;
    @Nullable
    @Min(1)
    private Integer buddy_min_year_of_study;
    @Nullable
    @Min(1)
    private Integer buddy_max_year_of_study;

    public MatchingProfile(User user, boolean display_friendship_profile, boolean display_dating_profile,
            boolean display_study_buddy_profile, @Min(18) Integer age, StudyField study_field, @Min(1) Integer year_of_study,
            Boolean has_job, boolean regularly_goes_to_gym, Sport favourite_sport, Venue preferred_venue, Hobby hobby1,
            Hobby hobby2, Hobby hobby3, Hobby hobby4, Hobby hobby5, FriendshipStyle kind_of_friendship,
            SocialStyle social_style, HangoutFrequency hangout_frequency, FriendActivity friend_activity,
            PlanningStyle planning_style, ConversationStyle conversation_style, CommunicationStyle communication_style,
            PersonalityTrait personality_trait, FriendshipValue friendship_value, Availability availability,
            Motivation motivation, FriendType friend_type, Boolean looking_for_short_term_relationship,
            @Min(18) Integer min_partner_age, @Min(18) Integer max_partner_age, Gender partner_gender,
            StudyField buddy_area_of_study, @Min(1) Integer buddy_min_year_of_study, @Min(1) Integer buddy_max_year_of_study) {
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
        this.kind_of_friendship = kind_of_friendship;
        this.social_style = social_style;
        this.hangout_frequency = hangout_frequency;
        this.friend_activity = friend_activity;
        this.planning_style = planning_style;
        this.conversation_style = conversation_style;
        this.communication_style = communication_style;
        this.personality_trait = personality_trait;
        this.friendship_value = friendship_value;
        this.availability = availability;
        this.motivation = motivation;
        this.friend_type = friend_type;
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public StudyField getStudy_field() {
        return study_field;
    }

    public void setStudy_field(StudyField study_field) {
        this.study_field = study_field;
    }

    public Integer getYear_of_study() {
        return year_of_study;
    }

    public void setYear_of_study(int year_of_study) {
        this.year_of_study = year_of_study;
    }

    public Boolean isHas_job() {
        return has_job;
    }

    public void setHas_job(Boolean has_job) {
        this.has_job = has_job;
    }

    public Boolean isRegularly_goes_to_gym() {
        return regularly_goes_to_gym;
    }

    public void setRegularly_goes_to_gym(Boolean regularly_goes_to_gym) {
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

    public FriendshipStyle getKind_of_friendship() {
        return kind_of_friendship;
    }

    public void setKind_of_friendship(FriendshipStyle kind_of_friendship) {
        this.kind_of_friendship = kind_of_friendship;
    }

    public SocialStyle getSocial_style() {
        return social_style;
    }

    public void setSocial_style(SocialStyle social_style) {
        this.social_style = social_style;
    }

    public HangoutFrequency getHangout_frequency() {
        return hangout_frequency;
    }

    public void setHangout_frequency(HangoutFrequency hangout_frequency) {
        this.hangout_frequency = hangout_frequency;
    }

    public FriendActivity getFriend_activity() {
        return friend_activity;
    }

    public void setFriend_activity(FriendActivity friend_activity) {
        this.friend_activity = friend_activity;
    }

    public PlanningStyle getPlanning_style() {
        return planning_style;
    }

    public void setPlanning_style(PlanningStyle planning_style) {
        this.planning_style = planning_style;
    }

    public ConversationStyle getConversation_style() {
        return conversation_style;
    }

    public void setConversation_style(ConversationStyle conversation_style) {
        this.conversation_style = conversation_style;
    }

    public CommunicationStyle getCommunication_style() {
        return communication_style;
    }

    public void setCommunication_style(CommunicationStyle communication_style) {
        this.communication_style = communication_style;
    }

    public PersonalityTrait getPersonality_trait() {
        return personality_trait;
    }

    public void setPersonality_trait(PersonalityTrait personality_trait) {
        this.personality_trait = personality_trait;
    }

    public FriendshipValue getFriendship_value() {
        return friendship_value;
    }

    public void setFriendship_value(FriendshipValue friendship_value) {
        this.friendship_value = friendship_value;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public Motivation getMotivation() {
        return motivation;
    }

    public void setMotivation(Motivation motivation) {
        this.motivation = motivation;
    }

    public FriendType getFriend_type() {
        return friend_type;
    }

    public void setFriend_type(FriendType friend_type) {
        this.friend_type = friend_type;
    }

    public Boolean isLooking_for_short_term_relationship() {
        return looking_for_short_term_relationship;
    }

    public void setLooking_for_short_term_relationship(Boolean looking_for_short_term_relationship) {
        this.looking_for_short_term_relationship = looking_for_short_term_relationship;
    }

    public Integer getMin_partner_age() {
        return min_partner_age;
    }

    public void setMin_partner_age(Integer min_partner_age) {
        this.min_partner_age = min_partner_age;
    }

    public Integer getMax_partner_age() {
        return max_partner_age;
    }

    public void setMax_partner_age(Integer max_partner_age) {
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

    public Integer getBuddy_min_year_of_study() {
        return buddy_min_year_of_study;
    }

    public void setBuddy_min_year_of_study(Integer buddy_min_year_of_study) {
        this.buddy_min_year_of_study = buddy_min_year_of_study;
    }

    public Integer getBuddy_max_year_of_study() {
        return buddy_max_year_of_study;
    }

    public void setBuddy_max_year_of_study(Integer buddy_max_year_of_study) {
        this.buddy_max_year_of_study = buddy_max_year_of_study;
    }

    // Convenience accessor for display purposes - the individual hobby1..hobby5
    // fields keep their priority weighting for MatchingAlgorithm, but views just
    // want a de-duplicated, non-empty list of the hobbies actually picked.
    public List<Hobby> getHobbies() {
        List<Hobby> hobbies = new ArrayList<>();
        for (Hobby h : new Hobby[]{hobby1, hobby2, hobby3, hobby4, hobby5}) {
            if (h != null && h != Hobby.NONE && !hobbies.contains(h)) {
                hobbies.add(h);
            }
        }
        return hobbies;
    }

}
