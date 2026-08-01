package com.cmpt276.group3.grouproject.algorithms;

import com.cmpt276.group3.grouproject.enums.Availability;
import com.cmpt276.group3.grouproject.enums.Campus;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.GroupVibe;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.Lifestyle;
import com.cmpt276.group3.grouproject.enums.MeetupStyle;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.enums.TopInterest;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupPreference;
import com.cmpt276.group3.grouproject.models.User;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Pure unit tests - no Spring context needed, same as the rest of the matching logic.
public class GroupMatchingAlgorithmTest {

    private User user(long id) {
        User user = new User("Student" + id, "Test", "student" + id + "@sfu.ca", "pw",
                Role.USER, Gender.NONE, "");
        user.setId(id);
        return user;
    }

    // A fully-answered preference; every test tweaks a copy of this.
    private GroupPreference preference(long id) {
        GroupPreference preference = new GroupPreference(user(id));
        preference.setLookingForGroup(true);
        preference.setVibe(GroupVibe.GAMING_NIGHTS);
        preference.setSize_preference(GroupSizePreference.SMALL);
        preference.setMeetup_style(MeetupStyle.ON_CAMPUS);
        preference.setCampus(Campus.BURNABY);
        preference.setAvailability(Availability.EVENINGS);
        preference.setLifestyle(Lifestyle.BALANCED);
        preference.setTop_interest(TopInterest.GAMING_TECH);
        preference.setHobby1(Hobby.VIDEO_GAMES);
        preference.setHobby2(Hobby.BOARD_GAMES);
        preference.setHobby3(Hobby.CHESS);
        return preference;
    }

    // Somebody who answered the opposite way on every question.
    private GroupPreference opposite(long id) {
        GroupPreference preference = new GroupPreference(user(id));
        preference.setLookingForGroup(true);
        preference.setVibe(GroupVibe.OUTDOORS_SPORTS);
        preference.setSize_preference(GroupSizePreference.LARGE);
        preference.setMeetup_style(MeetupStyle.OFF_CAMPUS);
        preference.setCampus(Campus.SURREY);
        preference.setAvailability(Availability.MORNINGS);
        preference.setLifestyle(Lifestyle.VERY_ACTIVE);
        preference.setTop_interest(TopInterest.SPORTS_FITNESS);
        preference.setHobby1(Hobby.CAMPING);
        preference.setHobby2(Hobby.GARDENING);
        preference.setHobby3(Hobby.WRITING);
        return preference;
    }

    // --- compatibility ---

    @Test
    void compatibility_identicalAnswersScoreTheMaximum() {
        assertEquals(100, GroupMatchingAlgorithm.compatibility(preference(1), preference(2)));
    }

    @Test
    void compatibility_completelyDifferentAnswersScoreZero() {
        assertEquals(0, GroupMatchingAlgorithm.compatibility(preference(1), opposite(2)));
    }

    @Test
    void compatibility_isSymmetric() {
        GroupPreference a = preference(1);
        GroupPreference b = opposite(2);
        b.setVibe(GroupVibe.GAMING_NIGHTS);
        b.setHobby1(Hobby.VIDEO_GAMES);

        assertEquals(
                GroupMatchingAlgorithm.compatibility(a, b),
                GroupMatchingAlgorithm.compatibility(b, a));
    }

    @Test
    void compatibility_sharedVibeAloneIsWorth25() {
        GroupPreference a = preference(1);
        GroupPreference b = opposite(2);
        b.setVibe(GroupVibe.GAMING_NIGHTS); // the only thing they agree on

        assertEquals(25, GroupMatchingAlgorithm.compatibility(a, b));
    }

    // Two unanswered questions are not agreement - a blank shouldn't earn points.
    @Test
    void compatibility_twoBlankAnswersDoNotCountAsAMatch() {
        GroupPreference a = new GroupPreference(user(1));
        GroupPreference b = new GroupPreference(user(2));

        assertEquals(0, GroupMatchingAlgorithm.compatibility(a, b));
    }

    @Test
    void compatibility_handlesNullsWithoutThrowing() {
        assertEquals(0, GroupMatchingAlgorithm.compatibility(null, preference(1)));
        assertEquals(0, GroupMatchingAlgorithm.compatibility(preference(1), null));
        assertEquals(0, GroupMatchingAlgorithm.compatibility(null, null));
    }

    @Test
    void compatibility_neverExceeds100() {
        for (long i = 1; i <= 5; i++) {
            int score = GroupMatchingAlgorithm.compatibility(preference(i), preference(i + 10));
            assertTrue(score >= 0 && score <= 100, "score out of range: " + score);
        }
    }

    // --- attributeFit / groupFit ---

    private FriendGroup group(GroupVibe vibe, Campus campus) {
        FriendGroup group = new FriendGroup("Test Group", "desc", vibe, MeetupStyle.ON_CAMPUS,
                campus, Availability.EVENINGS, TopInterest.GAMING_TECH,
                GroupSizePreference.SMALL, user(99));
        group.setId(500L);
        return group;
    }

    @Test
    void attributeFit_perfectMatchScores100() {
        assertEquals(100,
                GroupMatchingAlgorithm.attributeFit(preference(1), group(GroupVibe.GAMING_NIGHTS, Campus.BURNABY)));
    }

    @Test
    void attributeFit_wrongVibeLosesTheHeaviestWeight() {
        assertEquals(65,
                GroupMatchingAlgorithm.attributeFit(preference(1), group(GroupVibe.STUDY_GRIND, Campus.BURNABY)));
    }

    // With nobody in it yet, all we can judge a group on is what it advertises.
    @Test
    void groupFit_fallsBackToAttributesForAnEmptyGroup() {
        FriendGroup empty = group(GroupVibe.GAMING_NIGHTS, Campus.BURNABY);

        assertEquals(100, GroupMatchingAlgorithm.groupFit(preference(1), empty, new ArrayList<>()));
        assertEquals(100, GroupMatchingAlgorithm.groupFit(preference(1), empty, null));
    }

    @Test
    void groupFit_blendsGroupAttributesWithTheExistingMembers() {
        FriendGroup match = group(GroupVibe.GAMING_NIGHTS, Campus.BURNABY);
        List<GroupPreference> members = Arrays.asList(preference(2), preference(3));

        // attributes 100, members 100 -> 0.4*100 + 0.6*100
        assertEquals(100, GroupMatchingAlgorithm.groupFit(preference(1), match, members));
    }

    @Test
    void groupFit_incompatibleMembersDragTheScoreDown() {
        FriendGroup match = group(GroupVibe.GAMING_NIGHTS, Campus.BURNABY);
        List<GroupPreference> members = Arrays.asList(opposite(2), opposite(3));

        // attributes 100, members 0 -> 0.4*100 + 0.6*0
        assertEquals(40, GroupMatchingAlgorithm.groupFit(preference(1), match, members));
    }

    @Test
    void groupFit_doesNotScoreAUserAgainstThemselves() {
        FriendGroup match = group(GroupVibe.GAMING_NIGHTS, Campus.BURNABY);
        GroupPreference me = preference(1);

        // Only the self-entry is present, so there's nobody real to compare against
        // and the score should fall back to the group's own attributes.
        assertEquals(100, GroupMatchingAlgorithm.groupFit(me, match, Arrays.asList(me)));
    }

    // --- clustering ---

    @Test
    void formGroupAround_buildsAGroupUpToTheMaximumSize() {
        GroupPreference seed = preference(1);
        List<GroupPreference> pool = Arrays.asList(preference(2), preference(3), preference(4), preference(5));

        List<GroupPreference> cluster = GroupMatchingAlgorithm.formGroupAround(seed, pool, 3, 4);

        assertEquals(4, cluster.size());
        assertSame(seed, cluster.get(0), "the requesting student should always seed their own group");
    }

    @Test
    void formGroupAround_returnsNothingWhenThePoolIsTooSmall() {
        GroupPreference seed = preference(1);
        List<GroupPreference> pool = Arrays.asList(preference(2));

        assertTrue(GroupMatchingAlgorithm.formGroupAround(seed, pool, 3, 4).isEmpty());
    }

    @Test
    void formGroupAround_stopsPullingInPoorFitsOnceMinimumSizeIsMet() {
        GroupPreference seed = preference(1);
        // Two great fits get it to the minimum of 3, then only bad fits are left.
        List<GroupPreference> pool = Arrays.asList(
                preference(2), preference(3), opposite(4), opposite(5));

        List<GroupPreference> cluster = GroupMatchingAlgorithm.formGroupAround(seed, pool, 3, 5);

        assertEquals(3, cluster.size());
    }

    @Test
    void formGroupAround_isDeterministicAcrossRuns() {
        GroupPreference seed = preference(1);
        List<GroupPreference> pool = Arrays.asList(preference(4), preference(2), preference(3));

        List<GroupPreference> first = GroupMatchingAlgorithm.formGroupAround(seed, pool, 3, 4);
        List<GroupPreference> second = GroupMatchingAlgorithm.formGroupAround(seed, pool, 3, 4);

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(first.get(i).getUser().getId(), second.get(i).getUser().getId());
        }
    }

    @Test
    void formGroupAround_rejectsNonsenseSizes() {
        assertTrue(GroupMatchingAlgorithm.formGroupAround(preference(1), new ArrayList<>(), 1, 4).isEmpty());
        assertTrue(GroupMatchingAlgorithm.formGroupAround(preference(1), new ArrayList<>(), 5, 3).isEmpty());
        assertTrue(GroupMatchingAlgorithm.formGroupAround(null, new ArrayList<>(), 3, 4).isEmpty());
    }

    @Test
    void formGroups_splitsAPoolIntoSeveralGroups() {
        List<GroupPreference> pool = new ArrayList<>();
        for (long i = 1; i <= 6; i++) {
            pool.add(preference(i));
        }

        List<List<GroupPreference>> groups = GroupMatchingAlgorithm.formGroups(pool, 3, 3);

        assertEquals(2, groups.size());
        assertEquals(3, groups.get(0).size());
        assertEquals(3, groups.get(1).size());
    }

    @Test
    void formGroups_neverPlacesAStudentInTwoGroups() {
        List<GroupPreference> pool = new ArrayList<>();
        for (long i = 1; i <= 8; i++) {
            pool.add(preference(i));
        }

        List<List<GroupPreference>> groups = GroupMatchingAlgorithm.formGroups(pool, 3, 4);

        List<Long> seen = new ArrayList<>();
        for (List<GroupPreference> cluster : groups) {
            for (GroupPreference member : cluster) {
                long id = member.getUser().getId();
                assertFalse(seen.contains(id), "student " + id + " ended up in two groups");
                seen.add(id);
            }
        }
    }

    @Test
    void formGroups_handlesAnEmptyOrNullPool() {
        assertTrue(GroupMatchingAlgorithm.formGroups(new ArrayList<>(), 3, 4).isEmpty());
        assertTrue(GroupMatchingAlgorithm.formGroups(null, 3, 4).isEmpty());
    }

    // --- naming ---

    @Test
    void suggestName_buildsAReadableNameFromTheSeed() {
        String name = GroupMatchingAlgorithm.suggestName(Arrays.asList(preference(1), preference(2)));

        assertTrue(name.contains("Burnaby"), "expected the campus in: " + name);
        assertTrue(name.endsWith("Crew"), "expected a Crew suffix in: " + name);
        assertTrue(name.length() <= 60);
    }

    @Test
    void suggestName_hasAFallbackForAnEmptyCluster() {
        assertEquals("New Friend Group", GroupMatchingAlgorithm.suggestName(new ArrayList<>()));
        assertEquals("New Friend Group", GroupMatchingAlgorithm.suggestName(null));
    }
}
