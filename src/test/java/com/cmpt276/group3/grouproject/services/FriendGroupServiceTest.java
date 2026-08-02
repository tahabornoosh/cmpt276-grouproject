package com.cmpt276.group3.grouproject.services;

import com.cmpt276.group3.grouproject.enums.Availability;
import com.cmpt276.group3.grouproject.enums.Campus;
import com.cmpt276.group3.grouproject.enums.Gender;
import com.cmpt276.group3.grouproject.enums.GroupRole;
import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.GroupVibe;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.enums.MeetupStyle;
import com.cmpt276.group3.grouproject.enums.Role;
import com.cmpt276.group3.grouproject.enums.TopInterest;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.FriendGroupRepository;
import com.cmpt276.group3.grouproject.models.GroupMembership;
import com.cmpt276.group3.grouproject.models.GroupMembershipRepository;
import com.cmpt276.group3.grouproject.models.GroupPreference;
import com.cmpt276.group3.grouproject.models.GroupPreferenceRepository;
import com.cmpt276.group3.grouproject.models.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FriendGroupServiceTest {

    @Autowired
    private FriendGroupService friendGroupService;

    @MockitoBean
    private FriendGroupRepository friendGroupRepository;

    @MockitoBean
    private GroupMembershipRepository groupMembershipRepository;

    @MockitoBean
    private GroupPreferenceRepository groupPreferenceRepository;

    private User owner;
    private User outsider;
    private FriendGroup group;

    @BeforeEach
    void setUp() {
        owner = user(1L, "Owner");
        outsider = user(2L, "Outsider");

        group = new FriendGroup("Test Group", "desc", GroupVibe.GAMING_NIGHTS, MeetupStyle.ON_CAMPUS,
                Campus.BURNABY, Availability.EVENINGS, TopInterest.GAMING_TECH,
                GroupSizePreference.SMALL, owner);
        group.setId(10L);

        when(friendGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        // save() echoes back whatever it was handed, which is all these tests need.
        when(friendGroupRepository.save(any(FriendGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(groupMembershipRepository.save(any(GroupMembership.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(groupPreferenceRepository.save(any(GroupPreference.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User user(long id, String name) {
        User user = new User(name, "Test", name.toLowerCase() + "@sfu.ca", "pw",
                Role.USER, Gender.NONE, "");
        user.setId(id);
        return user;
    }

    private GroupPreference completePreference(User user) {
        GroupPreference preference = new GroupPreference(user);
        preference.setLookingForGroup(true);
        preference.setVibe(GroupVibe.GAMING_NIGHTS);
        preference.setSize_preference(GroupSizePreference.SMALL);
        preference.setMeetup_style(MeetupStyle.ON_CAMPUS);
        preference.setCampus(Campus.BURNABY);
        preference.setAvailability(Availability.EVENINGS);
        preference.setTop_interest(TopInterest.GAMING_TECH);
        preference.setHobby1(Hobby.VIDEO_GAMES);
        return preference;
    }

    private void membersAre(GroupMembership... memberships) {
        List<GroupMembership> list = new ArrayList<>(Arrays.asList(memberships));
        when(groupMembershipRepository.findByGroupOrderByJoinedAtAsc(group)).thenReturn(list);
        when(groupMembershipRepository.countByGroup(group)).thenReturn((long) list.size());

        long adminCount = list.stream().filter(m -> m.getRole() == GroupRole.ADMIN).count();
        when(groupMembershipRepository.countByGroupAndRole(group, GroupRole.ADMIN)).thenReturn(adminCount);

        for (GroupMembership membership : list) {
            when(groupMembershipRepository.findByGroupAndUser(group, membership.getUser()))
                    .thenReturn(Optional.of(membership));
            when(groupMembershipRepository.existsByGroupAndUser(group, membership.getUser())).thenReturn(true);
        }
    }

    // --- Creating a group ---

    @Test
    void createGroup_makesTheCreatorAnAdmin() {
        FriendGroup form = new FriendGroup("New Group", null, GroupVibe.CHILL_HANGOUT,
                MeetupStyle.MIXED, Campus.SURREY, Availability.WEEKENDS,
                TopInterest.FOOD_TRAVEL, GroupSizePreference.MEDIUM, owner);

        friendGroupService.createGroup(owner, form);

        // A group must never exist without someone able to manage it.
        verify(groupMembershipRepository).save(argThat((GroupMembership membership) ->
                membership.getUser().getId() == owner.getId()
                        && membership.getRole() == GroupRole.ADMIN));
    }

    @Test
    void createGroup_takesTheMemberCapFromTheChosenSize() {
        FriendGroup form = new FriendGroup("New Group", null, GroupVibe.CHILL_HANGOUT,
                MeetupStyle.MIXED, Campus.SURREY, Availability.WEEKENDS,
                TopInterest.FOOD_TRAVEL, GroupSizePreference.LARGE, owner);

        FriendGroup created = friendGroupService.createGroup(owner, form);

        assertEquals(GroupSizePreference.LARGE.getMaxSize(), created.getMaxMembers());
    }

    @Test
    void createGroup_rejectsABlankName() {
        FriendGroup form = new FriendGroup("   ", null, null, null, null, null, null, null, owner);

        assertThrows(IllegalArgumentException.class, () -> friendGroupService.createGroup(owner, form));
    }

    @Test
    void createGroup_rejectsAMissingCreator() {
        FriendGroup form = new FriendGroup("Name", null, null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> friendGroupService.createGroup(null, form));
    }

    // --- Admin-only operations ---

    @Test
    void updateGroup_isAllowedForAnAdmin() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));

        FriendGroup updated = friendGroupService.updateGroup(owner, 10L, "Renamed", "new desc", false);

        assertEquals("Renamed", updated.getName());
        assertFalse(updated.isOpenToNewMembers());
    }

    @Test
    void updateGroup_isRefusedForAPlainMember() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.updateGroup(outsider, 10L, "Hijacked", null, true));
    }

    @Test
    void updateGroup_isRefusedForSomeoneNotInTheGroup() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));
        when(groupMembershipRepository.findByGroupAndUser(group, outsider)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.updateGroup(outsider, 10L, "Hijacked", null, true));
    }

    @Test
    void deleteGroup_removesMembershipsBeforeTheGroup() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));

        friendGroupService.deleteGroup(owner, 10L);

        verify(groupMembershipRepository).deleteByGroup(group);
        verify(friendGroupRepository).delete(group);
    }

    @Test
    void deleteGroup_isRefusedForAPlainMember() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class, () -> friendGroupService.deleteGroup(outsider, 10L));
        verify(friendGroupRepository, never()).delete(any(FriendGroup.class));
    }

    @Test
    void operationsOnAMissingGroupFail() {
        when(friendGroupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> friendGroupService.updateGroup(owner, 999L, "Name", null, true));
    }

    // --- Joining ---

    @Test
    void joinGroup_addsANewMemberWithTheMemberRole() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));
        when(groupMembershipRepository.existsByGroupAndUser(group, outsider)).thenReturn(false);

        GroupMembership membership = friendGroupService.joinGroup(outsider, 10L);

        assertEquals(GroupRole.MEMBER, membership.getRole());
        assertEquals(outsider.getId(), membership.getUser().getId());
    }

    @Test
    void joinGroup_refusesToAddTheSamePersonTwice() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));

        assertThrows(IllegalStateException.class, () -> friendGroupService.joinGroup(owner, 10L));
    }

    @Test
    void joinGroup_refusesAClosedGroup() {
        group.setOpenToNewMembers(false);
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));
        when(groupMembershipRepository.existsByGroupAndUser(group, outsider)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> friendGroupService.joinGroup(outsider, 10L));
    }

    @Test
    void joinGroup_refusesAFullGroup() {
        group.setMaxMembers(1);
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));
        when(groupMembershipRepository.existsByGroupAndUser(group, outsider)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> friendGroupService.joinGroup(outsider, 10L));
    }

    // --- Leaving ---

    @Test
    void leaveGroup_promotesSomeoneElseWhenTheLastAdminLeaves() {
        GroupMembership ownerMembership = new GroupMembership(group, owner, GroupRole.ADMIN);
        GroupMembership otherMembership = new GroupMembership(group, outsider, GroupRole.MEMBER);
        membersAre(ownerMembership, otherMembership);

        // After the delete, only the remaining member is left.
        when(groupMembershipRepository.findByGroupOrderByJoinedAtAsc(group))
                .thenReturn(new ArrayList<>(Arrays.asList(ownerMembership, otherMembership)))
                .thenReturn(new ArrayList<>(Arrays.asList(otherMembership)));

        friendGroupService.leaveGroup(owner, 10L);

        // The group must not be left without an admin.
        assertEquals(GroupRole.ADMIN, otherMembership.getRole());
        verify(friendGroupRepository, never()).delete(group);
    }

    @Test
    void leaveGroup_deletesTheGroupWhenTheLastMemberLeaves() {
        GroupMembership ownerMembership = new GroupMembership(group, owner, GroupRole.ADMIN);
        membersAre(ownerMembership);

        when(groupMembershipRepository.findByGroupOrderByJoinedAtAsc(group))
                .thenReturn(new ArrayList<>(Arrays.asList(ownerMembership)))
                .thenReturn(new ArrayList<>());

        friendGroupService.leaveGroup(owner, 10L);

        verify(friendGroupRepository).delete(group);
    }

    @Test
    void leaveGroup_leavesTheOtherAdminAloneWhenOneOfTwoLeaves() {
        GroupMembership ownerMembership = new GroupMembership(group, owner, GroupRole.ADMIN);
        GroupMembership otherAdmin = new GroupMembership(group, outsider, GroupRole.ADMIN);
        membersAre(ownerMembership, otherAdmin);

        when(groupMembershipRepository.findByGroupOrderByJoinedAtAsc(group))
                .thenReturn(new ArrayList<>(Arrays.asList(ownerMembership, otherAdmin)))
                .thenReturn(new ArrayList<>(Arrays.asList(otherAdmin)));

        friendGroupService.leaveGroup(owner, 10L);

        assertEquals(GroupRole.ADMIN, otherAdmin.getRole());
        verify(friendGroupRepository, never()).delete(group);
    }

    @Test
    void leaveGroup_refusesIfYouAreNotAMember() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));
        when(groupMembershipRepository.findByGroupAndUser(group, outsider)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> friendGroupService.leaveGroup(outsider, 10L));
    }

    // --- Roles ---

    @Test
    void changeRole_promotesAMemberToAdmin() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        GroupMembership updated = friendGroupService.changeRole(owner, 10L, outsider.getId(), GroupRole.ADMIN);

        assertEquals(GroupRole.ADMIN, updated.getRole());
    }

    @Test
    void changeRole_refusesToDemoteTheOnlyAdmin() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.changeRole(owner, 10L, owner.getId(), GroupRole.MEMBER));
    }

    @Test
    void changeRole_allowsDemotionWhenAnotherAdminRemains() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.ADMIN));

        GroupMembership updated = friendGroupService.changeRole(owner, 10L, owner.getId(), GroupRole.MEMBER);

        assertEquals(GroupRole.MEMBER, updated.getRole());
    }

    @Test
    void changeRole_isRefusedForAPlainMember() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.changeRole(outsider, 10L, owner.getId(), GroupRole.MEMBER));
    }

    @Test
    void changeRole_failsForSomeoneWhoIsNotInTheGroup() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));

        assertThrows(IllegalArgumentException.class,
                () -> friendGroupService.changeRole(owner, 10L, 4242L, GroupRole.ADMIN));
    }

    // --- Removing members ---

    @Test
    void removeMember_letsAnAdminRemoveAPlainMember() {
        GroupMembership target = new GroupMembership(group, outsider, GroupRole.MEMBER);
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN), target);

        friendGroupService.removeMember(owner, 10L, outsider.getId());

        verify(groupMembershipRepository).delete(target);
    }

    @Test
    void removeMember_refusesSelfRemoval() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN));

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.removeMember(owner, 10L, owner.getId()));
    }

    @Test
    void removeMember_isRefusedForAPlainMember() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertThrows(IllegalStateException.class,
                () -> friendGroupService.removeMember(outsider, 10L, owner.getId()));
    }

    // --- Preferences ---

    @Test
    void savePreference_createsARowTheFirstTime() {
        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.empty());

        GroupPreference submitted = completePreference(owner);
        GroupPreference saved = friendGroupService.savePreference(owner, submitted);

        assertEquals(GroupVibe.GAMING_NIGHTS, saved.getVibe());
        assertEquals(owner.getId(), saved.getUser().getId());
        verify(groupPreferenceRepository).save(any(GroupPreference.class));
    }

    @Test
    void savePreference_updatesTheExistingRowInsteadOfAddingAnother() {
        GroupPreference existing = completePreference(owner);
        existing.setId(77L);
        existing.setVibe(GroupVibe.STUDY_GRIND);
        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.of(existing));

        GroupPreference submitted = completePreference(owner);
        submitted.setVibe(GroupVibe.PARTY_SOCIAL);

        GroupPreference saved = friendGroupService.savePreference(owner, submitted);

        assertEquals(77L, saved.getId().longValue(), "should have updated the existing row");
        assertEquals(GroupVibe.PARTY_SOCIAL, saved.getVibe());
    }

    @Test
    void savePreference_rejectsAMissingUser() {
        assertThrows(IllegalArgumentException.class,
                () -> friendGroupService.savePreference(null, completePreference(owner)));
    }

    // --- Auto-matching ---

    @Test
    void autoMatch_requiresACompletedQuestionnaire() {
        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> friendGroupService.autoMatchIntoGroup(owner));
    }

    @Test
    void autoMatch_requiresLookingForGroupToBeOn() {
        GroupPreference preference = completePreference(owner);
        preference.setLookingForGroup(false);
        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.of(preference));

        assertThrows(IllegalStateException.class, () -> friendGroupService.autoMatchIntoGroup(owner));
    }

    @Test
    void autoMatch_returnsNullWhenTooFewStudentsAreLooking() {
        GroupPreference preference = completePreference(owner);
        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.of(preference));
        when(groupPreferenceRepository.findByLookingForGroupTrue())
                .thenReturn(new ArrayList<>(Arrays.asList(preference)));

        assertNull(friendGroupService.autoMatchIntoGroup(owner));
        verify(friendGroupRepository, never()).save(any(FriendGroup.class));
    }

    @Test
    void autoMatch_buildsAGroupWithTheRequesterAsAdmin() {
        GroupPreference mine = completePreference(owner);
        User second = user(3L, "Second");
        User third = user(4L, "Third");

        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.of(mine));
        when(groupPreferenceRepository.findByLookingForGroupTrue()).thenReturn(new ArrayList<>(
                Arrays.asList(mine, completePreference(second), completePreference(third))));
        // Nobody in the pool is in a group yet.
        when(groupMembershipRepository.findByUserOrderByJoinedAtDesc(any(User.class)))
                .thenReturn(new ArrayList<>());

        FriendGroup created = friendGroupService.autoMatchIntoGroup(owner);

        assertNotNull(created);
        assertTrue(created.isAutoGenerated());

        verify(groupMembershipRepository).save(argThat((GroupMembership membership) ->
                membership.getUser().getId() == owner.getId()
                        && membership.getRole() == GroupRole.ADMIN));
        verify(groupMembershipRepository).save(argThat((GroupMembership membership) ->
                membership.getUser().getId() == second.getId()
                        && membership.getRole() == GroupRole.MEMBER));
        verify(groupMembershipRepository).save(argThat((GroupMembership membership) ->
                membership.getUser().getId() == third.getId()
                        && membership.getRole() == GroupRole.MEMBER));
    }

    @Test
    void autoMatch_skipsStudentsWhoAreAlreadyInAGroup() {
        GroupPreference mine = completePreference(owner);
        User second = user(3L, "Second");
        User third = user(4L, "Third");
        GroupPreference secondPreference = completePreference(second);
        GroupPreference thirdPreference = completePreference(third);

        when(groupPreferenceRepository.findByUser(owner)).thenReturn(Optional.of(mine));
        when(groupPreferenceRepository.findByLookingForGroupTrue()).thenReturn(new ArrayList<>(
                Arrays.asList(mine, secondPreference, thirdPreference)));
        when(groupMembershipRepository.findByUserOrderByJoinedAtDesc(any(User.class)))
                .thenReturn(new ArrayList<>());
        // Third is already placed, so only two students are available - below the minimum of 3.
        when(groupMembershipRepository.findByUserOrderByJoinedAtDesc(third))
                .thenReturn(new ArrayList<>(Arrays.asList(new GroupMembership(group, third, GroupRole.MEMBER))));

        assertNull(friendGroupService.autoMatchIntoGroup(owner));
    }

    // --- Read helpers ---

    @Test
    void isAdmin_isTrueOnlyForAnAdminMembership() {
        membersAre(new GroupMembership(group, owner, GroupRole.ADMIN),
                new GroupMembership(group, outsider, GroupRole.MEMBER));

        assertTrue(friendGroupService.isAdmin(group, owner));
        assertFalse(friendGroupService.isAdmin(group, outsider));
        assertTrue(friendGroupService.isMember(group, outsider));
    }

    @Test
    void readHelpers_tolerateNulls() {
        assertNull(friendGroupService.getPreference(null));
        assertNull(friendGroupService.findGroup(null));
        assertTrue(friendGroupService.getMemberships(null).isEmpty());
        assertTrue(friendGroupService.getMyMemberships(null).isEmpty());
        assertEquals(0, friendGroupService.getMemberCount(null));
        assertFalse(friendGroupService.isAdmin(null, owner));
    }
}
