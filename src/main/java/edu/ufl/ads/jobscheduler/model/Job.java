package edu.ufl.ads.jobscheduler.model;

public final class Job {
  private final int id;
  private final long submissionTime;
  private final long estimatedTime;
  private final long delta;
  private int priority;
  private Long startTime;
  private Long actualTime;
  private Long completionTime;
  private Status status = Status.WAITING;
  public Job(int id, long submissionTime, int priority, long estimatedTime, long delta) {
    this.id = id;
    this.submissionTime = submissionTime;
    this.priority = priority;
    this.estimatedTime = estimatedTime;
    this.delta = delta;
  }

  public int getId() {
    return id;
  }

  public long getSubmissionTime() {
    return submissionTime;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public long getEstimatedTime() {
    return estimatedTime;
  }

  public long getDelta() {
    return delta;
  }

  public Long getStartTime() {
    return startTime;
  }

  public Long getActualTime() {
    return actualTime;
  }

  public Long getCompletionTime() {
    return completionTime;
  }

  public Status getStatus() {
    return status;
  }

  public void startAt(long time) {
    startTime = time;
    actualTime = estimatedTime + delta;
    completionTime = time + actualTime;
    status = Status.RUNNING;
  }

  public void markCompleted() {
    status = Status.COMPLETED;
  }

  public void markCancelled() {
    status = Status.CANCELLED;
  }

  public enum Status {
    WAITING,
    RUNNING,
    COMPLETED,
    CANCELLED
  }
}
