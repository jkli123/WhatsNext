package com.snowdragon.whatsnext.model;

import java.util.Comparator;
import java.util.Date;

public class TaskComparatorFactory {

    private TaskComparatorFactory() {}

    public static TaskComparatorFactory get() {
        return new TaskComparatorFactory();
    }

    public Comparator<Task> getNameComparator() {
        return new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                String n1 = o1.getName();
                String n2 = o2.getName();
                return n1.compareTo(n2);
            }
        };
    }

    public Comparator<Task> getCategoryComparator() {
        return new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                String n1 = o1.getCategory();
                String n2 = o2.getCategory();
                return n1.compareTo(n2);
            }
        };

    }

    public Comparator<Task> getDeadlineComparator() {
        return new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                Date n1 = o1.getDeadline();
                Date n2 = o2.getDeadline();
                if (n1.equals(n2)) {
                    return 0;
                } else if (n1.before(n2)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        };
    }

    public Comparator<Task> getStatusComparator() {
        return new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                int n1 = o1.getStatus();
                int n2 = o2.getStatus();
                if (n1 == n2) {
                    return 0;
                } else if (n1 < n2){
                    return -1;
                } else {
                    return 1;
                }
            }
        };
    }
}
