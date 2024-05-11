package net.bteuk.network.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Role implements Comparable<Role> {

    private String id;

    private String name;

    private String prefix;

    private String colour;

    private int weight;

    @Override
    public int compareTo(Role o) {
        return o.getWeight() - weight;
    }
}
