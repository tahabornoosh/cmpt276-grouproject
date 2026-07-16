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

    // extra friendship questionnaire questions
    @Nullable
    @Enumerated(EnumType.STRING)
    private Campus campus;
    @Nullable
    @Enumerated(EnumType.STRING)
    private Lifestyle lifestyle;
    @Nullable
    @Enumerated(EnumType.STRING)
    private TopInterest top_interests;

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

    // relationship questionnaire questions
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipGoal relationship_goal;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipPersonality relationship_personality;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipCommunicationStyle relationship_communication_style;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipTextingStyle relationship_texting_style;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipFreeTime relationship_free_time;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipValue relationship_value;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipConflictStyle relationship_conflict_style;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipLifestyle relationship_lifestyle;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipAmbitionImportance relationship_ambition_importance;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipCareStyle relationship_care_style;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipPersonalSpace relationship_personal_space;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipDateActivity relationship_date_activity;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipSocialLife relationship_social_life;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipHumorStyle relationship_humor_style;
    @Nullable
    @Enumerated(EnumType.STRING)
    private RelationshipStrength relationship_strength;

    // Study buddies
    @Nullable
    private StudyField buddy_area_of_study;
    @Nullable
    @Min(1)
    private Integer buddy_min_year_of_study;
    @Nullable
    @Min(1)
    private Integer buddy_max_year_of_study;

    // study buddy questionnaire questions
    @Nullable
    private String study_buddy_program;
    @Nullable
    private String study_buddy_courses;
    @Nullable
    @Enumerated(EnumType.STRING)
    private Gender study_buddy_gender_preference;

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


    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public Lifestyle getLifestyle() {
        return lifestyle;
    }

    public void setLifestyle(Lifestyle lifestyle) {
        this.lifestyle = lifestyle;
    }

    public TopInterest getTop_interests() {
        return top_interests;
    }

    public void setTop_interests(TopInterest top_interests) {
        this.top_interests = top_interests;
    }

    public RelationshipGoal getRelationship_goal() {
        return relationship_goal;
    }

    public void setRelationship_goal(RelationshipGoal relationship_goal) {
        this.relationship_goal = relationship_goal;
    }

    public RelationshipPersonality getRelationship_personality() {
        return relationship_personality;
    }

    public void setRelationship_personality(RelationshipPersonality relationship_personality) {
        this.relationship_personality = relationship_personality;
    }

    public RelationshipCommunicationStyle getRelationship_communication_style() {
        return relationship_communication_style;
    }

    public void setRelationship_communication_style(RelationshipCommunicationStyle relationship_communication_style) {
        this.relationship_communication_style = relationship_communication_style;
    }

    public RelationshipTextingStyle getRelationship_texting_style() {
        return relationship_texting_style;
    }

    public void setRelationship_texting_style(RelationshipTextingStyle relationship_texting_style) {
        this.relationship_texting_style = relationship_texting_style;
    }

    public RelationshipFreeTime getRelationship_free_time() {
        return relationship_free_time;
    }

    public void setRelationship_free_time(RelationshipFreeTime relationship_free_time) {
        this.relationship_free_time = relationship_free_time;
    }

    public RelationshipValue getRelationship_value() {
        return relationship_value;
    }

    public void setRelationship_value(RelationshipValue relationship_value) {
        this.relationship_value = relationship_value;
    }

    public RelationshipConflictStyle getRelationship_conflict_style() {
        return relationship_conflict_style;
    }

    public void setRelationship_conflict_style(RelationshipConflictStyle relationship_conflict_style) {
        this.relationship_conflict_style = relationship_conflict_style;
    }

    public RelationshipLifestyle getRelationship_lifestyle() {
        return relationship_lifestyle;
    }

    public void setRelationship_lifestyle(RelationshipLifestyle relationship_lifestyle) {
        this.relationship_lifestyle = relationship_lifestyle;
    }

    public RelationshipAmbitionImportance getRelationship_ambition_importance() {
        return relationship_ambition_importance;
    }

    public void setRelationship_ambition_importance(RelationshipAmbitionImportance relationship_ambition_importance) {
        this.relationship_ambition_importance = relationship_ambition_importance;
    }

    public RelationshipCareStyle getRelationship_care_style() {
        return relationship_care_style;
    }

    public void setRelationship_care_style(RelationshipCareStyle relationship_care_style) {
        this.relationship_care_style = relationship_care_style;
    }

    public RelationshipPersonalSpace getRelationship_personal_space() {
        return relationship_personal_space;
    }

    public void setRelationship_personal_space(RelationshipPersonalSpace relationship_personal_space) {
        this.relationship_personal_space = relationship_personal_space;
    }

    public RelationshipDateActivity getRelationship_date_activity() {
        return relationship_date_activity;
    }

    public void setRelationship_date_activity(RelationshipDateActivity relationship_date_activity) {
        this.relationship_date_activity = relationship_date_activity;
    }

    public RelationshipSocialLife getRelationship_social_life() {
        return relationship_social_life;
    }

    public void setRelationship_social_life(RelationshipSocialLife relationship_social_life) {
        this.relationship_social_life = relationship_social_life;
    }

    public RelationshipHumorStyle getRelationship_humor_style() {
        return relationship_humor_style;
    }

    public void setRelationship_humor_style(RelationshipHumorStyle relationship_humor_style) {
        this.relationship_humor_style = relationship_humor_style;
    }

    public RelationshipStrength getRelationship_strength() {
        return relationship_strength;
    }

    public void setRelationship_strength(RelationshipStrength relationship_strength) {
        this.relationship_strength = relationship_strength;
    }

    public String getStudy_buddy_program() {
        return study_buddy_program;
    }

    public void setStudy_buddy_program(String study_buddy_program) {
        this.study_buddy_program = study_buddy_program;
    }

    public String getStudy_buddy_courses() {
        return study_buddy_courses;
    }

    public void setStudy_buddy_courses(String study_buddy_courses) {
        this.study_buddy_courses = study_buddy_courses;
    }

    public Gender getStudy_buddy_gender_preference() {
        return study_buddy_gender_preference;
    }

    public void setStudy_buddy_gender_preference(Gender study_buddy_gender_preference) {
        this.study_buddy_gender_preference = study_buddy_gender_preference;
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
