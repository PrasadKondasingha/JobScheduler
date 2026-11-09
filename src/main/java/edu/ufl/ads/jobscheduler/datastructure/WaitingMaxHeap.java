package edu.ufl.ads.jobscheduler.datastructure;

import edu.ufl.ads.jobscheduler.model.Job;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WaitingMaxHeap {

  private final List<Job> jobs = new ArrayList<>();
  private final Map<Integer, Integer> positions = new HashMap<>();
  private boolean shortJobs;
  private int maxSize;
  private int insertions;
  private int extractions;
  private int removals;
  private int priorityUpdates;
  private int rebuilds;

  public void add(Job job) {
    Objects.requireNonNull(job, "Waiting job must not be null");
    jobs.add(job);
    int index = jobs.size() - 1;
    positions.put(job.getId(), index);
    siftUp(index);
    insertions++;
    maxSize = Math.max(maxSize, jobs.size());
  }

  /** Returns null when no job is waiting. */
  public Job extractMax() {
    if (jobs.isEmpty()) {
      return null;
    }
    extractions++;
    return removeAt(0);
  }

  public boolean remove(int jobId) {
    Integer index = positions.get(jobId);
    if (index == null) {
      return false;
    }
    removeAt(index);
    removals++;
    return true;
  }

  public boolean priorityChanged(Job job) {
    Objects.requireNonNull(job, "Updated job must not be null");
    Integer index = positions.get(job.getId());
    if (index == null) {
      return false;
    }
    repair(index);
    priorityUpdates++;
    return true;
  }

  public void useShortJobs() {
    if (shortJobs) {
      return;
    }
    shortJobs = true;
    for (int i = jobs.size() / 2 - 1; i >= 0; i--) {
      siftDown(i);
    }
    rebuildPositions();
    rebuilds++;
  }

  private Job removeAt(int index) {
    Job removed = jobs.get(index);
    int lastIndex = jobs.size() - 1;
    Job last = jobs.remove(lastIndex);
    positions.remove(removed.getId());
    if (index == lastIndex) {
      return removed;
    }
    jobs.set(index, last);
    positions.put(last.getId(), index);
    repair(index);
    return removed;
  }

  private void repair(int index) {
    int parent = (index - 1) / 2;
    if (index > 0 && higherRank(jobs.get(index), jobs.get(parent))) {
      siftUp(index);
      return;
    }
    siftDown(index);
  }

  private void siftUp(int index) {
    int current = index;
    while (current > 0) {
      int parent = (current - 1) / 2;
      if (!higherRank(jobs.get(current), jobs.get(parent))) {
        return;
      }
      swap(current, parent);
      current = parent;
    }
  }

  private void siftDown(int index) {
    int current = index;
    while (true) {
      int left = 2 * current + 1;
      if (left >= jobs.size()) {
        return;
      }
      int right = left + 1;
      int higherChild =
          right < jobs.size() && higherRank(jobs.get(right), jobs.get(left)) ? right : left;
      if (!higherRank(jobs.get(higherChild), jobs.get(current))) {
        return;
      }
      swap(current, higherChild);
      current = higherChild;
    }
  }

  private boolean higherRank(Job first, Job second) {
    if (shortJobs && first.getEstimatedTime() != second.getEstimatedTime()) {
      return first.getEstimatedTime() < second.getEstimatedTime();
    }
    if (!shortJobs && first.getPriority() != second.getPriority()) {
      return first.getPriority() > second.getPriority();
    }
    if (shortJobs && first.getPriority() != second.getPriority()) {
      return first.getPriority() > second.getPriority();
    }
    if (first.getSubmissionTime() != second.getSubmissionTime()) {
      return first.getSubmissionTime() < second.getSubmissionTime();
    }
    return first.getId() < second.getId();
  }

  private void swap(int firstIndex, int secondIndex) {
    Job first = jobs.get(firstIndex);
    Job second = jobs.get(secondIndex);
    jobs.set(firstIndex, second);
    jobs.set(secondIndex, first);
    positions.put(first.getId(), secondIndex);
    positions.put(second.getId(), firstIndex);
  }

  private void rebuildPositions() {
    positions.clear();
    for (int i = 0; i < jobs.size(); i++) {
      positions.put(jobs.get(i).getId(), i);
    }
  }

  public boolean isEmpty() {
    return jobs.isEmpty();
  }

  public int size() {
    return jobs.size();
  }

  public List<Job> arrayOrder() {
    return new ArrayList<>(jobs);
  }

  public HeapStatistics statistics() {
    return new HeapStatistics(
        size(), maxSize, insertions, extractions, removals, priorityUpdates, rebuilds);
  }
}
