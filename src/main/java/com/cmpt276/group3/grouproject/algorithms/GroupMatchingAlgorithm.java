package com.cmpt276.group3.grouproject.algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cmpt276.group3.grouproject.enums.GroupSizePreference;
import com.cmpt276.group3.grouproject.enums.Hobby;
import com.cmpt276.group3.grouproject.models.FriendGroup;
import com.cmpt276.group3.grouproject.models.GroupPreference;

// Scoring + clustering for the friend group finder.
// Written as static methods with no Spring dependencies, exactly like MatchingAlgorithm,
// so it can be unit tested on its own without booting the application context.
public class GroupMatchingAlgorithm {

    // Weights for person-to-person compatibility. Sum is 110, capped at 100 below,
    // so a couple of mismatched answers still leave room for a strong score.
    private static final int W_VIBE = 25;
    private static final int W_TOP_INTEREST = 15;
    private static final int W_MEETUP = 12;
    private static final int W_CAMPUS = 12;
    private static final int W_AVAILABILITY = 10;
    private static final int W_LIFESTYLE = 8;
    private static final int W_SIZE = 8;
    private static final int W_HOBBIES = 20;

    // Weights for person-to-group fit against the group's own advertised answers. Sum is 100.
    private static final int GW_VIBE = 35;
    private static final int GW_TOP_INTEREST = 20;
    private static final int GW_MEETUP = 15;
    private static final int GW_CAMPUS = 15;
    private static final int GW_AVAILABILITY = 15;

    // How much of a group fit score comes from the existing members vs the group's own answers.
    private static final double MEMBER_SHARE = 0.6;
    private static final double ATTRIBUTE_SHARE = 0.4;

    // A candidate scoring below this against a forming cluster is not pulled in.
    private static final int CLUSTER_THRESHOLD = 35;

    private GroupMatchingAlgorithm() {
        // static-only utility class
    }

    // Highest weighted-dot-product two people can score on hobbies (3*3 + 2*2 + 1*1).
    private static final double MAX_HOBBY_DOT = 14.0;

    // Weighted overlap of the two users' three hobby picks, scaled to W_HOBBIES.
    // Same idea as MatchingAlgorithm.hobbiesScore but with 3 slots instead of 5.
    private static double hobbiesScore(GroupPreference base, GroupPreference target) {
        if (base == null || target == null) {
            return 0;
        }

        double dot = 0;

        for (Hobby h : Hobby.values()) {
            if (h == Hobby.NONE) {
                continue; // NONE is "no answer", not a shared interest
            }

            double a = 0;
            if (base.getHobby1() == h) a += 3;
            if (base.getHobby2() == h) a += 2;
            if (base.getHobby3() == h) a += 1;

            double b = 0;
            if (target.getHobby1() == h) b += 3;
            if (target.getHobby2() == h) b += 2;
            if (target.getHobby3() == h) b += 1;

            dot += a * b;
        }

        double scaled = W_HOBBIES * (dot / MAX_HOBBY_DOT);
        return scaled > W_HOBBIES ? W_HOBBIES : scaled;
    }

    // Two nulls are not a match - an unanswered question earns nothing either way.
    private static boolean same(Object a, Object b) {
        return a != null && a.equals(b);
    }

    // How well two students would get on in the same group, 0-100.
    public static int compatibility(GroupPreference base, GroupPreference target) {
        if (base == null || target == null) {
            return 0;
        }

        double score = hobbiesScore(base, target);

        if (same(base.getVibe(), target.getVibe())) score += W_VIBE;
        if (same(base.getTop_interest(), target.getTop_interest())) score += W_TOP_INTEREST;
        if (same(base.getMeetup_style(), target.getMeetup_style())) score += W_MEETUP;
        if (same(base.getCampus(), target.getCampus())) score += W_CAMPUS;
        if (same(base.getAvailability(), target.getAvailability())) score += W_AVAILABILITY;
        if (same(base.getLifestyle(), target.getLifestyle())) score += W_LIFESTYLE;
        if (same(base.getSize_preference(), target.getSize_preference())) score += W_SIZE;

        int rounded = (int) Math.round(score);
        return rounded > 100 ? 100 : rounded;
    }

    // How well a candidate matches a group's own advertised answers, 0-100.
    public static int attributeFit(GroupPreference candidate, FriendGroup group) {
        if (candidate == null || group == null) {
            return 0;
        }

        int score = 0;

        if (same(candidate.getVibe(), group.getVibe())) score += GW_VIBE;
        if (same(candidate.getTop_interest(), group.getTop_interest())) score += GW_TOP_INTEREST;
        if (same(candidate.getMeetup_style(), group.getMeetup_style())) score += GW_MEETUP;
        if (same(candidate.getCampus(), group.getCampus())) score += GW_CAMPUS;
        if (same(candidate.getAvailability(), group.getAvailability())) score += GW_AVAILABILITY;

        return score;
    }

    // Overall fit of a candidate for an existing group, 0-100.
    // Blends the group's advertised answers with how the candidate scores against
    // the people already in it, so a group's real character outweighs its description.
    public static int groupFit(GroupPreference candidate, FriendGroup group, List<GroupPreference> memberPreferences) {
        if (candidate == null || group == null) {
            return 0;
        }

        int attribute = attributeFit(candidate, group);

        if (memberPreferences == null || memberPreferences.isEmpty()) {
            return attribute; // brand new group - all we have to go on is what it advertises
        }

        double total = 0;
        int counted = 0;

        for (GroupPreference member : memberPreferences) {
            if (member == null || member.getUser() == null) {
                continue;
            }
            if (candidate.getUser() != null
                    && member.getUser().getId() == candidate.getUser().getId()) {
                continue; // never score someone against themselves
            }
            total += compatibility(candidate, member);
            counted++;
        }

        if (counted == 0) {
            return attribute;
        }

        double memberAverage = total / counted;
        return (int) Math.round(ATTRIBUTE_SHARE * attribute + MEMBER_SHARE * memberAverage);
    }

    // Average compatibility of a candidate against everyone currently in a cluster.
    public static int clusterFit(GroupPreference candidate, List<GroupPreference> cluster) {
        if (candidate == null || cluster == null || cluster.isEmpty()) {
            return 0;
        }

        double total = 0;
        for (GroupPreference member : cluster) {
            total += compatibility(candidate, member);
        }

        return (int) Math.round(total / cluster.size());
    }

    // Sorted, non-null copy of a pool. Sorting by user id keeps repeated runs deterministic,
    // which matters both for tests and for not shuffling people around between matcher runs.
    private static List<GroupPreference> cleanPool(List<GroupPreference> pool) {
        List<GroupPreference> cleaned = new ArrayList<>();

        if (pool == null) {
            return cleaned;
        }

        for (GroupPreference preference : pool) {
            if (preference != null && preference.getUser() != null) {
                cleaned.add(preference);
            }
        }

        cleaned.sort(Comparator.comparingLong(preference -> preference.getUser().getId()));
        return cleaned;
    }

    // Builds one group around a specific student: starts from the seed, then repeatedly adds
    // whoever in the pool fits the cluster best, stopping at maxSize or when nobody left scores
    // above CLUSTER_THRESHOLD. Returns an empty list if the group never reaches minSize - a
    // "group" of one isn't useful, and those students stay in the pool for the next run.
    //
    // This is the entry point used by "Find me a group", so the requesting student is always
    // the seed and always ends up in the group they asked for.
    public static List<GroupPreference> formGroupAround(GroupPreference seed, List<GroupPreference> pool,
            int minSize, int maxSize) {
        List<GroupPreference> cluster = new ArrayList<>();

        if (seed == null || seed.getUser() == null || minSize < 2 || maxSize < minSize) {
            return cluster;
        }

        List<GroupPreference> remaining = cleanPool(pool);
        Set<Long> taken = new HashSet<>();

        cluster.add(seed);
        taken.add(seed.getUser().getId());

        while (cluster.size() < maxSize) {
            GroupPreference best = null;
            int bestScore = -1;

            for (GroupPreference candidate : remaining) {
                if (taken.contains(candidate.getUser().getId())) {
                    continue;
                }

                int score = clusterFit(candidate, cluster);
                if (score > bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }

            if (best == null) {
                break; // nobody left in the pool
            }

            // Below the threshold we'd rather leave the group small than force a bad fit,
            // but we keep pulling people in until the group at least reaches minSize.
            if (bestScore < CLUSTER_THRESHOLD && cluster.size() >= minSize) {
                break;
            }

            cluster.add(best);
            taken.add(best.getUser().getId());
        }

        return cluster.size() >= minSize ? cluster : new ArrayList<>();
    }

    // Clusters a whole pool of students into new friend groups by seeding repeatedly with the
    // lowest-id student who hasn't been placed yet. Used for bulk/admin-side matching.
    public static List<List<GroupPreference>> formGroups(List<GroupPreference> pool, int minSize, int maxSize) {
        List<List<GroupPreference>> groups = new ArrayList<>();

        if (minSize < 2 || maxSize < minSize) {
            return groups;
        }

        List<GroupPreference> remaining = cleanPool(pool);
        Set<Long> assigned = new HashSet<>();

        for (GroupPreference seed : remaining) {
            if (assigned.contains(seed.getUser().getId())) {
                continue;
            }

            // Only offer the matcher people who haven't been placed in a group yet this run.
            List<GroupPreference> available = new ArrayList<>();
            for (GroupPreference preference : remaining) {
                if (!assigned.contains(preference.getUser().getId())) {
                    available.add(preference);
                }
            }

            List<GroupPreference> cluster = formGroupAround(seed, available, minSize, maxSize);

            // Mark the seed either way, so a student who can't be grouped isn't retried forever.
            assigned.add(seed.getUser().getId());

            if (!cluster.isEmpty()) {
                for (GroupPreference member : cluster) {
                    assigned.add(member.getUser().getId());
                }
                groups.add(cluster);
            }
        }

        return groups;
    }

    // Convenience overload using the seed's own preferred group size.
    public static List<List<GroupPreference>> formGroups(List<GroupPreference> pool) {
        return formGroups(pool, GroupSizePreference.SMALL.getMinSize(), GroupSizePreference.MEDIUM.getMaxSize());
    }

    // Builds a readable name for an auto-generated group, e.g. "Burnaby Gaming Nights Crew".
    public static String suggestName(List<GroupPreference> members) {
        if (members == null || members.isEmpty()) {
            return "New Friend Group";
        }

        GroupPreference seed = members.get(0);
        StringBuilder name = new StringBuilder();

        if (seed.getCampus() != null) {
            name.append(seed.getCampus().getDisplayName()).append(" ");
        }

        if (seed.getVibe() != null) {
            name.append(seed.getVibe().getDisplayName());
        } else if (seed.getTop_interest() != null) {
            name.append(seed.getTop_interest().getDisplayName());
        } else {
            name.append("Friend");
        }

        name.append(" Crew");

        String result = name.toString().trim();
        return result.length() > 60 ? result.substring(0, 60) : result;
    }
}
