package com.example.housemateapp.utilities;

import com.example.housemateapp.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArrayListUtils {
    public static ArrayList<User> filterUsers(ArrayList<User> list, double range, int days, String statusType) {
        ArrayList<User> filteredUsers = new ArrayList<>();

        for (User user : list) {
            if (range != 0 && user.rangeInKilometers >= range) continue;
            if (days != 0 && user.willStayForDays >= days) continue;
            if (!statusType.equalsIgnoreCase("Hepsi") &&
                !user.statusType.equalsIgnoreCase(statusType)) continue;

            filteredUsers.add(user);
        }

        return filteredUsers;
    }

    public static void sortUsersBy(ArrayList<User> list, String sortBy) {
        Comparator<User> comparator;

        if (sortBy.equalsIgnoreCase("Kampüse Uzaklık (Artan)")) {
            comparator = (user1, user2) -> (int) ((user1.rangeInKilometers - user2.rangeInKilometers) * 10);
        } else if (sortBy.equalsIgnoreCase("Kampüse Uzaklık (Azalan)")) {
            comparator = (user1, user2) -> (int) ((user2.rangeInKilometers - user1.rangeInKilometers) * 10);
        } else if (sortBy.equalsIgnoreCase("Konaklama Süresi (Artan)")) {
            comparator = (user1, user2) -> user1.willStayForDays - user2.willStayForDays;
        } else if (sortBy.equalsIgnoreCase("Konaklama Süresi (Azalan)")) {
            comparator = (user1, user2) -> user2.willStayForDays - user1.willStayForDays;
        } else {
            return;
        }

        Collections.sort(list, comparator);
    }
}
