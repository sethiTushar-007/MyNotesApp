package com.example.mynotesapp;

import java.util.Comparator;

public class DateSorter implements Comparator<Notes> {
    @Override
    public int compare(Notes o1, Notes o2) {
        return o1.getDate().compareTo(o2.getDate());
    }
}
