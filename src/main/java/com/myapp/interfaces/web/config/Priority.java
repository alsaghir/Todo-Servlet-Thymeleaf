package com.myapp.interfaces.web.config;


import java.util.stream.Stream;

public enum  Priority {
  LOW(1, "LOW"),
  MEDIUM(2, "MEDIUM"),
  HIGH(3, "HIGH")
  ;

  private int numeric;
  private String textual;

  Priority(int numeric, String textual){
    this.numeric = numeric;
    this.textual = textual;
  }

  public static Priority getByNummericValue(int numeric){
    return Stream.of(Priority.values())
        .filter(v -> v.numeric == numeric)
        .findFirst().orElse(Priority.LOW);
  }

  public String textual(){return this.textual;}
  public int numeric(){return this.numeric;}
}
