package com.darwinit.annotation.demo.domain;

import com.darwinit.annotation.builder.Builder;

@Builder
public class Person {

  private String name;
  private int age;
  private int[] friends;

  public Person(String name, int age, int[] friends) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public int[] getFriends() {
    return friends;
  }
}
