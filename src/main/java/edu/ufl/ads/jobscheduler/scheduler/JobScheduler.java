package edu.ufl.ads.jobscheduler.scheduler;

import edu.ufl.ads.jobscheduler.datastructure.AvlTree;
import edu.ufl.ads.jobscheduler.datastructure.AvlTreeStatistics;
import edu.ufl.ads.jobscheduler.datastructure.HeapStatistics;
import edu.ufl.ads.jobscheduler.datastructure.WaitingMaxHeap;
import edu.ufl.ads.jobscheduler.io.OutputFormat;
import edu.ufl.ads.jobscheduler.model.Job;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public final class JobScheduler {
  private final int capacity;
  private final AvlTree jobs = new AvlTree();
  private final WaitingMaxHeap waitingJobs = new WaitingMaxHeap();
  private final List<Job> cancelledJobs = new ArrayList<>();
  private final PriorityQueue<Job> runningJobs =
      new PriorityQueue<>(
          Comparator.comparing(Job::getCompletionTime).thenComparingInt(Job::getId));
  private final List<Job> executionOrder = new ArrayList<>();
  private long currentTime;

  public JobScheduler(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be positive");
    }
    this.capacity = capacity;
  }

  public void completeThrough(long time) {
    Job next = runningJobs.peek();
    // Only started jobs enter this queue, so their completion times are known.
    while (next != null && next.getCompletionTime() <= time) {
      runningJobs.remove().markCompleted();
      next = runningJobs.peek();
    }
    currentTime = Math.max(currentTime, time);
  }

  public List<String> submit(long timestamp, int id, int priority, long estimatedTime, long delta) {
    if (lookup(id) != null) {
      return List.of(OutputFormat.format(OutputFormat.JOB_ALREADY_EXISTS, id));
    }
    Job job = new Job(id, timestamp, priority, estimatedTime, delta);
    jobs.insert(job);
    waitingJobs.add(job);
    return List.of(OutputFormat.format(OutputFormat.JOB_SUBMITTED, id, timestamp));
  }

  public List<String> processNext() {
    Job job = waitingJobs.extractMax();
    if (job == null) {
      return List.of(OutputFormat.NO_WAITING_JOBS);
    }
    if (runningJobs.size() >= capacity) {
      // Positive capacity guarantees a running job here; element() is non-null.
      completeThrough(runningJobs.element().getCompletionTime());
    }
    job.startAt(currentTime);
    runningJobs.add(job);
    executionOrder.add(job);
    return List.of(OutputFormat.format(OutputFormat.JOB_STARTED, job.getId(), currentTime));
  }

  // The assignment requires AVL lookup followed by the cancellation list.
  private Job lookup(int id) {
    Job job = jobs.find(id);
    if (job != null) {
      return job;
    }
    for (Job cancelled : cancelledJobs) {
      if (cancelled.getId() == id) {
        return cancelled;
      }
    }
    return null;
  }

  public List<String> find(int id) {
    Job job = lookup(id);
    if (job == null) {
      return List.of(OutputFormat.format(OutputFormat.JOB_NOT_FOUND, id));
    }
    return List.of(
        OutputFormat.format(
            OutputFormat.JOB_DETAILS,
            id,
            job.getSubmissionTime(),
            printable(job.getStartTime()),
            printable(job.getActualTime()),
            printable(job.getCompletionTime()),
            job.getStatus()));
  }

  public List<String> jobStatus() {
    List<String> output = new ArrayList<>();
    output.add(OutputFormat.JOB_STATUS);
    output.add(OutputFormat.STATUS_HEADER);
    jobs.inorderJobs().forEach(job -> output.add(statusRow(job)));
    cancelledJobs.forEach(job -> output.add(statusRow(job)));
    output.add(OutputFormat.END_JOB_STATUS);
    return output;
  }

  public List<String> cancel(int id) {
    Job job = lookup(id);
    if (job == null) {
      return List.of(OutputFormat.format(OutputFormat.JOB_NOT_FOUND, id));
    }
    if (job.getStatus() != Job.Status.WAITING) {
      return List.of(OutputFormat.format(OutputFormat.CANNOT_CANCEL, id, job.getStatus()));
    }
    waitingJobs.remove(id);
    jobs.delete(id);
    job.markCancelled();
    cancelledJobs.add(job);
    return List.of(OutputFormat.format(OutputFormat.JOB_CANCELLED, id));
  }

  public List<String> updatePriority(int id, int priority) {
    Job job = lookup(id);
    if (job == null) {
      return List.of(OutputFormat.format(OutputFormat.JOB_NOT_FOUND, id));
    }
    if (job.getStatus() != Job.Status.WAITING) {
      return List.of(OutputFormat.format(OutputFormat.CANNOT_UPDATE_PRIORITY, id, job.getStatus()));
    }
    job.setPriority(priority);
    waitingJobs.priorityChanged(job);
    return List.of(OutputFormat.format(OutputFormat.PRIORITY_UPDATED, id, priority));
  }

  public List<String> switchToShortJobs() {
    waitingJobs.useShortJobs();
    return List.of(OutputFormat.SHORT_JOBS_ENABLED);
  }

  public List<String> finalStructureOutput() {
    List<String> output = new ArrayList<>();
    appendExecutionOrder(output);
    appendTree(output);
    output.add(OutputFormat.format(OutputFormat.INORDER, inorderIds()));
    output.add(OutputFormat.format(OutputFormat.MAX_HEAP, heapArray()));
    appendTreeStatistics(output);
    appendHeapStatistics(output);
    return output;
  }

  private void appendExecutionOrder(List<String> output) {
    output.add(OutputFormat.EXECUTION_ORDER);
    executionOrder.forEach(
        job ->
            output.add(
                OutputFormat.format(
                    OutputFormat.EXECUTION_ROW,
                    job.getId(),
                    job.getStartTime(),
                    job.getCompletionTime())));
    output.add(OutputFormat.END_EXECUTION_ORDER);
  }

  private void appendTree(List<String> output) {
    output.add(OutputFormat.TREE);
    List<List<Integer>> levels = jobs.levels();
    for (int level = 0; level < levels.size(); level++) {
      String values =
          levels.get(level).stream()
              .map(
                  key ->
                      key == null
                          ? OutputFormat.MISSING_CHILD
                          : OutputFormat.format(OutputFormat.TREE_NODE, key))
              .collect(Collectors.joining(OutputFormat.TREE_SEPARATOR));
      output.add(OutputFormat.format(OutputFormat.TREE_LEVEL, level, values));
    }
    output.add(OutputFormat.END_TREE);
  }

  private String inorderIds() {
    return jobs.inorderJobs().stream()
        .map(job -> Integer.toString(job.getId()))
        .collect(
            Collectors.joining(
                OutputFormat.LIST_SEPARATOR, OutputFormat.LIST_START, OutputFormat.LIST_END));
  }

  private String heapArray() {
    return waitingJobs.arrayOrder().stream()
        .map(
            job ->
                OutputFormat.format(
                    OutputFormat.HEAP_ENTRY,
                    job.getId(),
                    job.getPriority(),
                    job.getEstimatedTime()))
        .collect(
            Collectors.joining(
                OutputFormat.LIST_SEPARATOR, OutputFormat.LIST_START, OutputFormat.LIST_END));
  }

  private void appendTreeStatistics(List<String> output) {
    AvlTreeStatistics stats = jobs.statistics();
    output.add(OutputFormat.TREE_STATS);
    output.add(OutputFormat.format(OutputFormat.RECORDS, stats.records()));
    output.add(OutputFormat.format(OutputFormat.HEIGHT, stats.height()));
    output.add(OutputFormat.format(OutputFormat.INSERTIONS, stats.insertions()));
    output.add(OutputFormat.format(OutputFormat.DELETIONS, stats.deletions()));
    output.add(OutputFormat.format(OutputFormat.LEFT_ROTATIONS, stats.leftRotations()));
    output.add(OutputFormat.format(OutputFormat.RIGHT_ROTATIONS, stats.rightRotations()));
    output.add(OutputFormat.END_TREE_STATS);
  }

  private void appendHeapStatistics(List<String> output) {
    HeapStatistics stats = waitingJobs.statistics();
    output.add(OutputFormat.HEAP_STATS);
    output.add(OutputFormat.format(OutputFormat.SIZE, stats.size()));
    output.add(OutputFormat.format(OutputFormat.MAX_SIZE, stats.maxSize()));
    output.add(OutputFormat.format(OutputFormat.INSERTIONS, stats.insertions()));
    output.add(OutputFormat.format(OutputFormat.EXTRACTIONS, stats.extractions()));
    output.add(OutputFormat.format(OutputFormat.REMOVALS, stats.removals()));
    output.add(OutputFormat.format(OutputFormat.PRIORITY_UPDATES, stats.priorityUpdates()));
    output.add(OutputFormat.format(OutputFormat.REBUILDS, stats.rebuilds()));
    output.add(OutputFormat.END_HEAP_STATS);
  }

  private String statusRow(Job job) {
    return OutputFormat.format(
        OutputFormat.STATUS_ROW,
        job.getId(),
        job.getSubmissionTime(),
        printable(job.getStartTime()),
        printable(job.getActualTime()),
        printable(job.getCompletionTime()),
        job.getStatus());
  }

  private String printable(Long value) {
    return value == null ? OutputFormat.UNKNOWN_TIME : value.toString();
  }
}
