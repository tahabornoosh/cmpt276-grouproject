package com.cmpt276.group3.grouproject.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.cmpt276.group3.grouproject.enums.Availability;
import com.cmpt276.group3.grouproject.enums.Campus;
import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.GroupVibe;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Lifestyle;
import com.cmpt276.group3.grouproject.enums.MeetupStyle;
import com.cmpt276.group3.grouproject.enums.TopInterest;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

// One user's answers to the group finder questionnaire. Kept separate from
// MatchingProfile so the group finder works standalone - a user can be looking for a
// friend group without having filled in the friendship/dating/study-buddy questionnaire.
@Entity
@Table(name = "group_preferences")
public class GroupPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    // Opt-in switch: only users with this set are put in the auto-matching pool.
    @Column(name = "looking_for_group", nullable = false)
    private boolean lookingForGroup = true;

    @Nullable
    @Enumerated(EnumType.STRING)
    private GroupVibe vibe;

    @Nullable
    @Enumerated(EnumType.STRING)
    private GroupSizePreference size_preference;

    @Nullable
    @Enumerated(EnumType.STRING)
    private MeetupStyle meetup_style;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Campus campus;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Availability availability;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Lifestyle lifestyle;

    @Nullable
    @Enumerated(EnumType.STRING)
    private TopInterest top_interest;

    // Up to three interests, weighted 3 / 2 / 1 by the matching algorithm.
    @Nullable
    @Enumerated(EnumType.STRING)
    private Hobby hobby1;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Hobby hobby2;

    @Nullable
    @Enumerated(EnumType.STRING)
    private Hobby hobby3;

    // Free-text blurb shown to a group before they accept the user.
    @Nullable
    @Size(max = 300)
    @Column(length = 300)
    private String blurb;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public GroupPreference() {
    }

    public GroupPreference(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLookingForGroup() {
        return lookingForGroup;
    }

    public void setLookingForGroup(boolean lookingForGroup) {
        this.lookingForGroup = lookingForGroup;
    }

    public GroupVibe getVibe() {
        return vibe;
    }

    public void setVibe(GroupVibe vibe) {
        this.vibe = vibe;
    }

    public GroupSizePreference getSize_preference() {
        return size_preference;
    }

    public void setSize_preference(GroupSizePreference size_preference) {
        this.size_preference = size_preference;
    }

    public MeetupStyle getMeetup_style() {
        return meetup_style;
    }

    public void setMeetup_style(MeetupStyle meetup_style) {
        this.meetup_style = meetup_style;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public Lifestyle getLifestyle() {
        return lifestyle;
    }

    public void setLifestyle(Lifestyle lifestyle) {
        this.lifestyle = lifestyle;
    }

    public TopInterest getTop_interest() {
        return top_interest;
    }

    public void setTop_interest(TopInterest top_interest) {
        this.top_interest = top_interest;
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

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // De-duplicated, non-empty list of picked hobbies, for display only.
    // Mirrors MatchingProfile.getHobbies() so templates behave the same way.
    public List<Hobby> getHobbies() {
        List<Hobby> hobbies = new ArrayList<>();
        for (Hobby h : new Hobby[]{hobby1, hobby2, hobby3}) {
            if (h != null && h != Hobby.NONE && !hobbies.contains(h)) {
                hobbies.add(h);
            }
        }
        return hobbies;
    }

    // True once the user has answered enough for the matcher to produce a meaningful score.
    public boolean isComplete() {
        return vibe != null && size_preference != null && meetup_style != null
                && campus != null && availability != null && top_interest != null;
    }
}
