package edu.ufl.ads.jobscheduler.datastructure;

public record HeapStatistics(
    int size,
    int maxSize,
    int insertions,
    int extractions,
    int removals,
    int priorityUpdates,
    int rebuilds) {}
