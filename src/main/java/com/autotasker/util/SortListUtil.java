package com.autotasker.util;

import com.autotasker.model.Department;
import com.autotasker.model.User;

import java.util.ArrayList;
import java.util.Comparator;

public class SortListUtil {
    public static <T> void sortList(ArrayList<T> list, Class<T> clazz) {
        if (clazz == User.class) {
            list.sort(Comparator.comparing(u -> ((User) u).getUsername().toLowerCase()));
        } else if (clazz == Department.class) {
            list.sort(Comparator.comparing(d -> ((Department) d).getDepartmentName().toLowerCase()));
        }
    }
}
