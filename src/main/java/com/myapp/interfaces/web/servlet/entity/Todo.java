package com.myapp.interfaces.web.servlet.entity;

import com.myapp.interfaces.web.config.Priority;
import java.time.LocalDate;

public class Todo {

  private long id;
  private String title;
  private boolean done;
  private Priority priority;
  private LocalDate dueDate;
  private long userId;

  public Todo() {
    priority = Priority.LOW;
  }

  public Todo(long id, String title, boolean done, Priority priority, LocalDate dueDate, long userId) {
    this.id = id;
    this.userId = userId;
    this.title = title;
    this.done = done;
    this.priority = priority;
    this.dueDate = dueDate;
  }

  public Todo(String title, boolean done, Priority priority, LocalDate dueDate, long userId) {
    this(0, title, done, priority, dueDate, userId);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean isDone() {
    return done;
  }

  public void setDone(boolean done) {
    this.done = done;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }
}
