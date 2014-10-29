package com.lucperkins.dropwizard.riak;

import com.basho.riak.client.api.cap.ConflictResolver;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.lucperkins.dropwizard.riak.dao.RiakableObject;

import java.util.List;
import java.util.Set;

public class Person extends RiakableObject {
    private String name;
    private int age;
    private Set<String> hobbies;

    public Person(String name, int age, Set<String> hobbies) {
        this.name = name; this.age = age; this.hobbies = hobbies;

        setBucket("people");
        setKey(name.toLowerCase());
        setBucketType("siblings");
    }

    public void setAge(int age) { this.age = age; }

    public String getName() { return name; }
    public int getAge() { return age; }
    public Set<String> getHobbies() { return hobbies; }

    public static class Resolver implements ConflictResolver<Person> {
        @Override
        public Person resolve(List<Person> siblings) {
            if (siblings.size() == 0) {
                return null;
            } else {
                return siblings.get(0);
            }
        }
    }

    public static class AgeByOneYear extends UpdateValue.Update<Person> {
        @Override
        public Person apply(Person original) {
            int newAge = original.getAge() + 1;
            original.setAge(newAge);
            return original;
        }
    }

    public Person() { /* Jackson JSON deserialization */ }
}
